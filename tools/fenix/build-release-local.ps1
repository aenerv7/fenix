# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

param(
    [switch] $SkipBuild
)

$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$mach = Join-Path $PSScriptRoot "mach-local.ps1"
$powershell = Join-Path $PSHOME "pwsh.exe"
$localeFile = Join-Path $root "mobile\android\locales\all-locales"
$locales = @(Get-Content -LiteralPath $localeFile | ForEach-Object { $_.Trim() } | Where-Object { $_ })

if (-not $locales -or -not $locales.Contains("zh-CN")) {
    throw "The official Android locale list is empty or does not contain zh-CN: $localeFile"
}

function Invoke-LocalMach {
    param([Parameter(Mandatory)][string[]] $Arguments)

    & $powershell -NoProfile -File $mach @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "mach $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

function Get-ApkLocales {
    param([Parameter(Mandatory)][string] $ApkPath)

    $apkStream = [IO.File]::OpenRead($ApkPath)
    try {
        $apk = [IO.Compression.ZipArchive]::new(
            $apkStream,
            [IO.Compression.ZipArchiveMode]::Read,
            $false
        )
        try {
            $omniEntry = $apk.GetEntry("assets/omni.ja")
            if (-not $omniEntry) {
                throw "Missing assets/omni.ja in $ApkPath"
            }

            $omniStream = [IO.MemoryStream]::new()
            try {
                $entryStream = $omniEntry.Open()
                try {
                    $entryStream.CopyTo($omniStream)
                }
                finally {
                    $entryStream.Dispose()
                }

                $omniStream.Position = 0
                $omni = [IO.Compression.ZipArchive]::new(
                    $omniStream,
                    [IO.Compression.ZipArchiveMode]::Read,
                    $true
                )
                try {
                    $multilocaleEntry = $omni.GetEntry("res/multilocale.txt")
                    if (-not $multilocaleEntry) {
                        throw "Missing res/multilocale.txt in $ApkPath assets/omni.ja"
                    }

                    $reader = [IO.StreamReader]::new($multilocaleEntry.Open())
                    try {
                        return @($reader.ReadToEnd() -split "[,\r\n]+" | Where-Object { $_ })
                    }
                    finally {
                        $reader.Dispose()
                    }
                }
                finally {
                    $omni.Dispose()
                }
            }
            finally {
                $omniStream.Dispose()
            }
        }
        finally {
            $apk.Dispose()
        }
    }
    finally {
        $apkStream.Dispose()
    }
}

Add-Type -AssemblyName System.IO.Compression

if (-not $SkipBuild) {
    Invoke-LocalMach -Arguments @("build")
    Invoke-LocalMach -Arguments @("package")
}

Invoke-LocalMach -Arguments (@("package-multi-locale", "--locales") + $locales)

$previousMultilocale = $env:MOZ_CHROME_MULTILOCALE
try {
    $env:MOZ_CHROME_MULTILOCALE = $locales -join " "
    Invoke-LocalMach -Arguments @("gradle", "fenix:assembleRelease")
}
finally {
    if ($null -eq $previousMultilocale) {
        Remove-Item Env:MOZ_CHROME_MULTILOCALE -ErrorAction SilentlyContinue
    }
    else {
        $env:MOZ_CHROME_MULTILOCALE = $previousMultilocale
    }
}

$expectedLocales = @("en-US") + $locales
$releaseDirectory = Join-Path $root `
    "obj-firefox-android-aarch64\gradle\build\mobile\android\fenix\app\outputs\apk\release"
$apks = @(Get-ChildItem -LiteralPath $releaseDirectory -File | Where-Object {
    $_.Name -cmatch "^fenix-(arm64-v8a|armeabi-v7a|x86_64)-release\.apk$"
})

if ($apks.Count -eq 0) {
    throw "No release APKs found under $releaseDirectory"
}

foreach ($apk in $apks) {
    $actualLocales = @(Get-ApkLocales -ApkPath $apk.FullName)
    $missingLocales = @($expectedLocales | Where-Object { $_ -notin $actualLocales })
    $unexpectedLocales = @($actualLocales | Where-Object { $_ -notin $expectedLocales })

    if ($missingLocales -or $unexpectedLocales) {
        throw "$($apk.Name) has an invalid Gecko locale set. " +
            "Missing: $($missingLocales -join ', '); unexpected: $($unexpectedLocales -join ', ')"
    }

    Write-Output "Verified $($actualLocales.Count) Gecko locales in $($apk.Name)"
}
