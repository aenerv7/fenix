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
$versionCodeFile = Join-Path $root "FENIX_UPSTREAM_VERSION_CODES.json"
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
$versionCodeManifest = Get-Content -LiteralPath $versionCodeFile -Raw | ConvertFrom-Json
$baselineVersionCodes = $versionCodeManifest.PSObject.Properties[$baseline]
if (-not $baselineVersionCodes) {
    throw "Missing upstream versionCode entries for baseline $baseline in $versionCodeFile"
}
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
        [Parameter(Mandatory)][string] $Abi,
        [Parameter(Mandatory)][string[]] $ExpectedLocales
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

    $actualLocales = @(Get-GeckoPackageLocales -GeckoViewDirectory $geckoviewDirectory)
    $missingLocales = @($ExpectedLocales | Where-Object { $_ -notin $actualLocales })
    $unexpectedLocales = @($actualLocales | Where-Object { $_ -notin $ExpectedLocales })
    if ($missingLocales -or $unexpectedLocales) {
        throw "Cached Gecko package has an invalid locale set for $Abi. " +
            "Missing: $($missingLocales -join ', '); unexpected: $($unexpectedLocales -join ', ')"
    }
}

function Get-GeckoPackageLocales {
    param([Parameter(Mandatory)][string] $GeckoViewDirectory)

    $omniPath = Join-Path $GeckoViewDirectory "assets\omni.ja"
    $omniStream = [IO.File]::OpenRead($omniPath)
    try {
        $omni = [IO.Compression.ZipArchive]::new(
            $omniStream,
            [IO.Compression.ZipArchiveMode]::Read,
            $false
        )
        try {
            $multilocaleEntry = $omni.GetEntry("res/multilocale.txt")
            if (-not $multilocaleEntry) {
                throw "Missing res/multilocale.txt in $omniPath"
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

function Get-UpstreamVersionCode {
    param([Parameter(Mandatory)][string] $Abi)

    $entry = $baselineVersionCodes.Value.PSObject.Properties[$Abi]
    if (-not $entry) {
        throw "Missing upstream versionCode for $baseline/$Abi in $versionCodeFile"
    }
    $versionCode = [int]$entry.Value
    if ($versionCode -le 0) {
        throw "Invalid upstream versionCode for $baseline/${Abi}: $versionCode"
    }
    return $versionCode
}

function Assert-ApkVersionCode {
    param(
        [Parameter(Mandatory)][string] $ApkPath,
        [Parameter(Mandatory)][int] $ExpectedVersionCode
    )

    $buildTools = Join-Path $root ".mozbuild\android-sdk-windows\build-tools"
    $aapt = Get-ChildItem -LiteralPath $buildTools -Recurse -File -Filter "aapt.exe" |
        Sort-Object FullName -Descending | Select-Object -First 1
    if (-not $aapt) {
        throw "Unable to find aapt.exe under $buildTools"
    }
    $badging = & $aapt.FullName dump badging $ApkPath
    if ($LASTEXITCODE -ne 0 -or -not (($badging -join "`n") -match "versionCode='$ExpectedVersionCode'")) {
        throw "$ApkPath does not contain upstream versionCode $ExpectedVersionCode"
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
        $upstreamVersionCode = Get-UpstreamVersionCode -Abi $configuration.Abi
        $env:FENIX_ANDROID_TARGET = $configuration.Target
        $env:FENIX_ANDROID_OBJDIR = $configuration.ObjDir
        $env:MOZ_OBJDIR = Join-Path $root $configuration.ObjDir
        Remove-Item Env:MOZ_CHROME_MULTILOCALE -ErrorAction SilentlyContinue

        if ($ReuseGecko) {
            try {
                Assert-CachedGeckoPackage `
                    -ObjDir $configuration.ObjDir `
                    -Abi $configuration.Abi `
                    -ExpectedLocales $expectedLocales
                Write-Output "Reusing cached Gecko package for $($configuration.Abi)"
            }
            catch {
                if ($SkipBuild) {
                    throw "Cached Gecko package for $($configuration.Abi) is invalid and -SkipBuild prevents repair: $($_.Exception.Message)"
                }

                Write-Output "Cached Gecko package for $($configuration.Abi) is invalid; rebuilding its multi-locale package"
                Invoke-LocalMach -Arguments @("build")
                Invoke-LocalMach -Arguments @("package")
                Invoke-LocalMach -Arguments (@("package-multi-locale", "--locales") + $locales)
                Assert-CachedGeckoPackage `
                    -ObjDir $configuration.ObjDir `
                    -Abi $configuration.Abi `
                    -ExpectedLocales $expectedLocales
            }
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
            "-Pfenix.upstreamVersionCode=$upstreamVersionCode",
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
        Assert-ApkVersionCode -ApkPath $source -ExpectedVersionCode $upstreamVersionCode
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
