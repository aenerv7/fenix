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
    [switch] $ReuseGecko,
    [switch] $UseUpstreamGecko,
    [string] $UpstreamGeckoApk
)

$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$mach = Join-Path $PSScriptRoot "mach-local.ps1"
$sign = Join-Path $PSScriptRoot "sign-release-local.ps1"
$powershell = Join-Path $PSHOME "pwsh.exe"
$localeFile = Join-Path $root "mobile\android\locales\all-locales"
$versionCodeFile = Join-Path $root "FENIX_UPSTREAM_VERSION_CODES.json"
$upstreamGeckoFile = Join-Path $root "FENIX_UPSTREAM_GECKOVIEW.json"
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
$upstreamGeckoManifest = Get-Content -LiteralPath $upstreamGeckoFile -Raw | ConvertFrom-Json
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

if ($UseUpstreamGecko -and $ReuseGecko) {
    throw "-UseUpstreamGecko and -ReuseGecko are mutually exclusive"
}
if ($UseUpstreamGecko -and $UpstreamGeckoApk -and $selectedConfigurations.Count -ne 1) {
    throw "-UpstreamGeckoApk can only be used when exactly one ABI is selected"
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

function Get-ApkVersionName {
    param([Parameter(Mandatory)][string] $ApkPath)

    $buildTools = Join-Path $root ".mozbuild\android-sdk-windows\build-tools"
    $aapt = Get-ChildItem -LiteralPath $buildTools -Recurse -File -Filter "aapt.exe" |
        Sort-Object FullName -Descending | Select-Object -First 1
    if (-not $aapt) {
        throw "Unable to find aapt.exe under $buildTools"
    }
    $badging = & $aapt.FullName dump badging $ApkPath
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to inspect APK metadata: $ApkPath"
    }
    $match = [regex]::Match(($badging -join "`n"), "versionName='([^']+)'")
    if (-not $match.Success) {
        throw "APK has no versionName metadata: $ApkPath"
    }
    return $match.Groups[1].Value
}

function Get-UpstreamGeckoApk {
    param(
        [Parameter(Mandatory)][string] $Baseline,
        [Parameter(Mandatory)][string] $Abi,
        [Parameter(Mandatory)][string] $Url,
        [Parameter(Mandatory)][string] $ExpectedSha256,
        [string] $ProvidedPath
    )

    if ($ProvidedPath) {
        $resolvedPath = (Resolve-Path -LiteralPath $ProvidedPath -ErrorAction Stop).Path
    }
    else {
        $filename = "fenix-$Baseline.multi.android-$Abi.apk"
        $cacheDirectory = Join-Path $root ".tmp\upstream-geckoview\$Baseline\$Abi"
        $resolvedPath = Join-Path $cacheDirectory $filename
        if (-not (Test-Path -LiteralPath $resolvedPath)) {
            New-Item -ItemType Directory -Force -Path $cacheDirectory | Out-Null
            Write-Output "Downloading upstream GeckoView source APK: $Url"
            Invoke-WebRequest -UseBasicParsing -Uri $Url -OutFile $resolvedPath
        }
    }

    $actualSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $resolvedPath).Hash
    if ($actualSha256 -ne $ExpectedSha256) {
        throw "Upstream APK SHA-256 $actualSha256 does not match pinned digest $ExpectedSha256"
    }
    return (Resolve-Path -LiteralPath $resolvedPath).Path
}

function Assert-UpstreamApkSignature {
    param(
        [Parameter(Mandatory)][string] $ApkPath,
        [Parameter(Mandatory)][string] $ExpectedCertificateSha256
    )

    $buildToolsRoot = Join-Path $root ".mozbuild\android-sdk-windows\build-tools"
    $buildTools = Get-ChildItem -LiteralPath $buildToolsRoot -Directory |
        Sort-Object { [version] $_.Name } -Descending | Select-Object -First 1
    if (-not $buildTools) {
        throw "No Android build-tools installation found under $buildToolsRoot"
    }
    $signer = Join-Path $buildTools.FullName "apksigner.bat"
    $certificateOutput = & $signer verify --print-certs $ApkPath
    if ($LASTEXITCODE -ne 0) {
        throw "Upstream APK signature verification failed: $ApkPath"
    }
    $normalizedOutput = ($certificateOutput -join "`n").Replace(":", "").ToUpperInvariant()
    if ($normalizedOutput -notmatch [regex]::Escape($ExpectedCertificateSha256.ToUpperInvariant())) {
        throw "Upstream APK is not signed by the pinned Mozilla certificate"
    }
}

function Get-ZipEntrySha256 {
    param([Parameter(Mandatory)] $Entry)

    $entryStream = $Entry.Open()
    try {
        $sha256 = [Security.Cryptography.SHA256]::Create()
        try {
            return ([BitConverter]::ToString($sha256.ComputeHash($entryStream))).Replace("-", "")
        }
        finally { $sha256.Dispose() }
    }
    finally { $entryStream.Dispose() }
}

function Import-UpstreamGeckoPackage {
    param(
        [Parameter(Mandatory)][string] $ObjDir,
        [Parameter(Mandatory)][string] $Abi,
        [Parameter(Mandatory)][string] $ApkPath,
        [Parameter(Mandatory)][string[]] $ExpectedLocales,
        [Parameter(Mandatory)][string] $ExpectedBaseline,
        [Parameter(Mandatory)][int] $ExpectedVersionCode,
        [Parameter(Mandatory)] $Provenance
    )

    $actualVersionName = Get-ApkVersionName -ApkPath $ApkPath
    if ($actualVersionName -ne $ExpectedBaseline) {
        throw "Upstream APK versionName $actualVersionName does not match baseline $ExpectedBaseline"
    }
    Assert-ApkVersionCode -ApkPath $ApkPath -ExpectedVersionCode $ExpectedVersionCode
    Assert-UpstreamApkSignature `
        -ApkPath $ApkPath `
        -ExpectedCertificateSha256 $Provenance.certificateSha256
    $actualLocales = @(Get-ApkLocales -ApkPath $ApkPath)
    $missingLocales = @($ExpectedLocales | Where-Object { $_ -notin $actualLocales })
    $unexpectedLocales = @($actualLocales | Where-Object { $_ -notin $ExpectedLocales })
    if ($missingLocales -or $unexpectedLocales) {
        throw "Upstream APK has an invalid Gecko locale set. " +
            "Missing: $($missingLocales -join ', '); unexpected: $($unexpectedLocales -join ', ')"
    }

    $geckoLibraries = @(
        "libclearkey.so", "libcrashhelper.so", "libcrashtools.so", "libfreebl3.so",
        "libgkcodecs.so", "liblgpllibs.so", "libmozavcodec.so", "libmozavutil.so",
        "libmozglue.so", "libnss3.so", "libplugin-container.so", "libsoftokn3.so", "libxul.so"
    )
    $apkStream = [IO.File]::OpenRead($ApkPath)
    $geckoviewDirectory = Join-Path $root "$ObjDir\dist\geckoview"
    $metadataDirectory = Join-Path $root ".tmp\upstream-geckoview\metadata\$Abi"
    try {
        $apk = [IO.Compression.ZipArchive]::new($apkStream, [IO.Compression.ZipArchiveMode]::Read, $false)
        try {
            $omniEntry = $apk.GetEntry("assets/omni.ja")
            if (-not $omniEntry) {
                throw "Upstream APK is missing assets/omni.ja"
            }
            foreach ($library in $geckoLibraries) {
                if (-not $apk.GetEntry("lib/$Abi/$library")) {
                    throw "Upstream APK is missing lib/$Abi/$library"
                }
            }
            foreach ($expectedEntry in $Provenance.entries.PSObject.Properties) {
                $entry = $apk.GetEntry($expectedEntry.Name)
                if (-not $entry) {
                    throw "Upstream APK is missing pinned entry $($expectedEntry.Name)"
                }
                $actualSha256 = Get-ZipEntrySha256 -Entry $entry
                if ($actualSha256 -ne $expectedEntry.Value) {
                    throw "Upstream $($expectedEntry.Name) SHA-256 $actualSha256 does not match pinned digest $($expectedEntry.Value)"
                }
            }

            New-Item -ItemType Directory -Force -Path $metadataDirectory | Out-Null
            foreach ($metadata in @("application.ini", "platform.ini", "precomplete", "removed-files")) {
                $source = Join-Path $geckoviewDirectory $metadata
                if (Test-Path -LiteralPath $source) {
                    Copy-Item -LiteralPath $source -Destination $metadataDirectory -Force
                }
            }
            if (Test-Path -LiteralPath $geckoviewDirectory) {
                Remove-Item -LiteralPath $geckoviewDirectory -Recurse -Force
            }
            New-Item -ItemType Directory -Force -Path (Join-Path $geckoviewDirectory "assets") | Out-Null
            New-Item -ItemType Directory -Force -Path (Join-Path $geckoviewDirectory "lib\$Abi") | Out-Null

            foreach ($entryName in @("assets/omni.ja") + ($geckoLibraries | ForEach-Object { "lib/$Abi/$_" })) {
                $entry = $apk.GetEntry($entryName)
                $target = Join-Path $geckoviewDirectory ($entryName -replace '/', '\\')
                $targetStream = [IO.File]::Create($target)
                try {
                    $entryStream = $entry.Open()
                    try { $entryStream.CopyTo($targetStream) }
                    finally { $entryStream.Dispose() }
                }
                finally { $targetStream.Dispose() }
            }
        }
        finally { $apk.Dispose() }
    }
    finally { $apkStream.Dispose() }

    foreach ($metadata in @("application.ini", "platform.ini", "precomplete", "removed-files")) {
        $source = Join-Path $metadataDirectory $metadata
        if (Test-Path -LiteralPath $source) {
            Copy-Item -LiteralPath $source -Destination $geckoviewDirectory -Force
        }
    }
    Assert-CachedGeckoPackage -ObjDir $ObjDir -Abi $Abi -ExpectedLocales $ExpectedLocales
    Write-Output "Imported upstream $ExpectedBaseline GeckoView package for $Abi from $ApkPath"
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
        elseif ($UseUpstreamGecko) {
            $upstreamGecko = $upstreamGeckoManifest.PSObject.Properties[$baseline].Value.PSObject.Properties[$configuration.Abi]
            if (-not $upstreamGecko) {
                throw "Missing pinned upstream GeckoView source for $baseline/$($configuration.Abi) in $upstreamGeckoFile"
            }
            $sourceApk = Get-UpstreamGeckoApk `
                -Baseline $baseline `
                -Abi $configuration.Abi `
                -Url $upstreamGecko.Value.url `
                -ExpectedSha256 $upstreamGecko.Value.apkSha256 `
                -ProvidedPath $UpstreamGeckoApk
            Import-UpstreamGeckoPackage `
                -ObjDir $configuration.ObjDir `
                -Abi $configuration.Abi `
                -ApkPath $sourceApk `
                -ExpectedLocales $expectedLocales `
                -ExpectedBaseline $baseline `
                -ExpectedVersionCode $upstreamVersionCode `
                -Provenance $upstreamGecko.Value
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
