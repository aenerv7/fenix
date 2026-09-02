# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

param(
    [ValidateSet("arm64-v8a", "armeabi-v7a", "x86_64")]
    [string[]] $Abi = @("arm64-v8a", "armeabi-v7a", "x86_64"),
    [string] $VersionName,
    [string] $KeyStorePath,
    [string] $KeyAlias = "magi-opensource",
    [string] $PropertiesPath,
    [switch] $SkipBuild,
    [switch] $ReuseGecko
)

$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$mach = Join-Path $PSScriptRoot "mach-local.ps1"
$sign = Join-Path $PSScriptRoot "sign-release-local.ps1"
$powershell = Join-Path $PSHOME "pwsh.exe"
$localeFile = Join-Path $root "mobile\android\locales\all-locales"
$locales = @(Get-Content -LiteralPath $localeFile | ForEach-Object { $_.Trim() } | Where-Object { $_ })
$configurations = @(
    [pscustomobject] @{
        Abi = "arm64-v8a"
        Target = "aarch64-linux-android"
        ObjDir = "obj-firefox-android-aarch64"
    }
    [pscustomobject] @{
        Abi = "armeabi-v7a"
        Target = "arm-linux-androideabi"
        ObjDir = "obj-firefox-android-arm"
    }
    [pscustomobject] @{
        Abi = "x86_64"
        Target = "x86_64-linux-android"
        ObjDir = "obj-firefox-android-x86_64"
    }
)
$selectedConfigurations = @($configurations | Where-Object { $_.Abi -in $Abi })
$unsignedDirectory = Join-Path $root "artifacts\fenix-release\unsigned"

function Get-ReleaseVersionName {
    param([Parameter(Mandatory)][string] $Baseline)

    if ($Baseline -notmatch "^\d+\.\d+(\.\d+)?$") {
        throw "Invalid Fenix baseline version: $Baseline"
    }

    $escapedBaseline = [regex]::Escape($Baseline)
    $tagGlob = "fenix-$Baseline-r*"
    $tagPattern = "^fenix-$escapedBaseline-r([1-9]\d*)$"
    $headTags = @(& git -C $root tag --points-at HEAD --list $tagGlob)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to inspect release tags on the current commit"
    }

    $headRevisions = @($headTags | ForEach-Object {
        if ($_ -match $tagPattern) {
            [int] $matches[1]
        }
    })
    if ($headRevisions) {
        $revision = ($headRevisions | Measure-Object -Maximum).Maximum
        return "$Baseline-r$revision"
    }

    $allTags = @(& git -C $root tag --list $tagGlob)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to inspect local release tags"
    }
    $remoteTags = @(& git -C $root ls-remote --tags origin "refs/tags/$tagGlob")
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to inspect origin release tags"
    }

    $revisions = @($allTags | ForEach-Object {
        if ($_ -match $tagPattern) {
            [int] $matches[1]
        }
    })
    $revisions += @($remoteTags | ForEach-Object {
        $tag = ($_ -split "`t", 2)[-1] -replace "^refs/tags/", "" -replace "\^\{\}$", ""
        if ($tag -match $tagPattern) {
            [int] $matches[1]
        }
    })
    $revision = if ($revisions) {
        [int] (($revisions | Measure-Object -Maximum).Maximum) + 1
    }
    else {
        1
    }
    return "$Baseline-r$revision"
}

$baseline = (Get-Content (Join-Path $root "FENIX_UPSTREAM_RELEASE") -Raw).Trim()
if (-not $VersionName) {
    $VersionName = Get-ReleaseVersionName -Baseline $baseline
}
elseif ($VersionName -notmatch "^$([regex]::Escape($baseline))-r[1-9]\d*$") {
    throw "VersionName must match the current baseline: $baseline-rN"
}
Write-Output "Release version: $VersionName"
$versionRevision = [int]($VersionName -replace ".*-r", "")

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

function Assert-ApkGeckoLibraries {
    param(
        [Parameter(Mandatory)][string] $ApkPath,
        [Parameter(Mandatory)][string] $Abi
    )

    $apkStream = [IO.File]::OpenRead($ApkPath)
    try {
        $apk = [IO.Compression.ZipArchive]::new(
            $apkStream,
            [IO.Compression.ZipArchiveMode]::Read,
            $false
        )
        try {
            foreach ($library in @("libmozglue.so", "libxul.so")) {
                $entry = "lib/$Abi/$library"
                if (-not $apk.GetEntry($entry)) {
                    throw "$ApkPath is missing $entry"
                }
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

function Assert-CachedGeckoPackage {
    param(
        [Parameter(Mandatory)][string] $ObjDir,
        [Parameter(Mandatory)][string] $Abi
    )

    $geckoviewDirectory = Join-Path $root "$ObjDir\dist\geckoview"
    $requiredFiles = @(
        (Join-Path $geckoviewDirectory "assets\omni.ja"),
        (Join-Path $geckoviewDirectory "lib\$Abi\libmozglue.so"),
        (Join-Path $geckoviewDirectory "lib\$Abi\libxul.so")
    )
    $missingFiles = @($requiredFiles | Where-Object { -not (Test-Path -LiteralPath $_) })
    if ($missingFiles) {
        throw "Cached Gecko package is incomplete for $Abi. Missing: $($missingFiles -join ', ')"
    }
}

Add-Type -AssemblyName System.IO.Compression
$expectedLocales = @("en-US") + $locales
$previousTarget = $env:FENIX_ANDROID_TARGET
$previousObjDir = $env:FENIX_ANDROID_OBJDIR
$previousMozObjDir = $env:MOZ_OBJDIR
$previousMultilocale = $env:MOZ_CHROME_MULTILOCALE

New-Item -ItemType Directory -Force -Path $unsignedDirectory | Out-Null
foreach ($configuration in $selectedConfigurations) {
    Remove-Item -LiteralPath (
        Join-Path $unsignedDirectory "fenix-$($configuration.Abi)-release.apk"
    ) -Force -ErrorAction SilentlyContinue
}

try {
    foreach ($configuration in $selectedConfigurations) {
        $env:FENIX_ANDROID_TARGET = $configuration.Target
        $env:FENIX_ANDROID_OBJDIR = $configuration.ObjDir
        $env:MOZ_OBJDIR = Join-Path $root $configuration.ObjDir
        Remove-Item Env:MOZ_CHROME_MULTILOCALE -ErrorAction SilentlyContinue

        if ($ReuseGecko) {
            Assert-CachedGeckoPackage -ObjDir $configuration.ObjDir -Abi $configuration.Abi
            Write-Output "Reusing cached Gecko package for $($configuration.Abi)"
        }
        else {
            if (-not $SkipBuild) {
                Invoke-LocalMach -Arguments @("build")
                Invoke-LocalMach -Arguments @("package")
            }

            Invoke-LocalMach -Arguments (@("package-multi-locale", "--locales") + $locales)
        }

        $env:MOZ_CHROME_MULTILOCALE = $locales -join " "
        Invoke-LocalMach -Arguments @(
            "gradle",
            "-Pfenix.releaseRevision=$versionRevision",
            "fenix:assembleRelease"
        )

        $releaseDirectory = Join-Path $root `
            "$($configuration.ObjDir)\gradle\build\mobile\android\fenix\app\outputs\apk\release"
        $source = Join-Path $releaseDirectory "fenix-$($configuration.Abi)-release.apk"
        if (-not (Test-Path -LiteralPath $source)) {
            throw "Missing $($configuration.Abi) APK: $source"
        }

        $actualLocales = @(Get-ApkLocales -ApkPath $source)
        $missingLocales = @($expectedLocales | Where-Object { $_ -notin $actualLocales })
        $unexpectedLocales = @($actualLocales | Where-Object { $_ -notin $expectedLocales })
        if ($missingLocales -or $unexpectedLocales) {
            throw "$source has an invalid Gecko locale set. " +
                "Missing: $($missingLocales -join ', '); unexpected: $($unexpectedLocales -join ', ')"
        }

        Assert-ApkGeckoLibraries -ApkPath $source -Abi $configuration.Abi
        Copy-Item -LiteralPath $source -Destination $unsignedDirectory -Force
        Write-Output (
            "Verified $($actualLocales.Count) Gecko locales and native libraries in " +
            $([IO.Path]::GetFileName($source))
        )
    }
}
finally {
    if ($null -eq $previousTarget) {
        Remove-Item Env:FENIX_ANDROID_TARGET -ErrorAction SilentlyContinue
    }
    else {
        $env:FENIX_ANDROID_TARGET = $previousTarget
    }
    if ($null -eq $previousObjDir) {
        Remove-Item Env:FENIX_ANDROID_OBJDIR -ErrorAction SilentlyContinue
    }
    else {
        $env:FENIX_ANDROID_OBJDIR = $previousObjDir
    }
    if ($null -eq $previousMozObjDir) {
        Remove-Item Env:MOZ_OBJDIR -ErrorAction SilentlyContinue
    }
    else {
        $env:MOZ_OBJDIR = $previousMozObjDir
    }
    if ($null -eq $previousMultilocale) {
        Remove-Item Env:MOZ_CHROME_MULTILOCALE -ErrorAction SilentlyContinue
    }
    else {
        $env:MOZ_CHROME_MULTILOCALE = $previousMultilocale
    }
}

$signParameters = @{
    Abi = @($selectedConfigurations.Abi)
    VersionName = $VersionName
    KeyAlias = $KeyAlias
}
if ($KeyStorePath) {
    $signParameters.KeyStorePath = $KeyStorePath
}
if ($PropertiesPath) {
    $signParameters.PropertiesPath = $PropertiesPath
}
& $sign @signParameters
