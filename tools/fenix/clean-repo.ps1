# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

param(
    [switch] $DryRun,
    [switch] $IncludeAvds,
    [switch] $GitGc
)

$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$rootPrefix = $root + [IO.Path]::DirectorySeparatorChar

# Object directory entries the upstream GeckoView release flow reads:
#   mobile/  generated GeckoView Java sources and the manifest overlay
#   dist/    only geckoview/, the package imported from the upstream APK
# The rest is produced by a local Gecko build, which Fenix-only releases must
# not need. See "GeckoView source policy" in FENIX_DEVELOPMENT.md.
$objKeep = @("dist", "mobile", "config.statusd", "faster", "_build_manifests", "_tests", "python")
$distKeep = @("geckoview")

function Remove-Entry {
    param([Parameter(Mandatory)][string] $Path)

    $full = [IO.Path]::GetFullPath($Path)
    if (-not $full.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove a path outside the repository: $full"
    }
    if (-not (Test-Path -LiteralPath $full)) {
        return
    }
    if ($DryRun) {
        Write-Output "would remove $full"
        return
    }

    Write-Output "removing $full"
    Remove-Item -LiteralPath $full -Recurse -Force
}

function Clear-ObjDir {
    param([Parameter(Mandatory)][string] $ObjDir)

    foreach ($entry in Get-ChildItem -LiteralPath $ObjDir -Directory -Force) {
        if ($objKeep -notcontains $entry.Name) {
            Remove-Entry $entry.FullName
        }
    }

    $dist = Join-Path $ObjDir "dist"
    if (-not (Test-Path -LiteralPath $dist)) {
        return
    }
    foreach ($entry in Get-ChildItem -LiteralPath $dist -Force) {
        if ($distKeep -notcontains $entry.Name) {
            Remove-Entry $entry.FullName
        }
    }
}

function Get-GradleVersion {
    $properties = Join-Path $root "gradle\wrapper\gradle-wrapper.properties"
    $url = (Get-Content -LiteralPath $properties | Where-Object { $_ -like "distributionUrl=*" }) -replace "^distributionUrl=", ""

    [regex]::Match($url, "gradle-([\d.]+)-").Groups[1].Value
}

function Clear-OldGradle {
    $current = Get-GradleVersion
    if (-not $current) {
        Write-Warning "Unable to read the Gradle version from gradle-wrapper.properties"
        return
    }

    $versionPattern = "^\d+\.\d+(\.\d+)?$"
    foreach ($parent in @((Join-Path $gradleHome "caches"), $gradleHome)) {
        Get-ChildItem -LiteralPath $parent -Directory -Force -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match $versionPattern -and $_.Name -ne $current } |
            ForEach-Object { Remove-Entry $_.FullName }
    }

    Get-ChildItem -LiteralPath (Join-Path $gradleHome "wrapper\dists") -Directory -Force -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like "gradle-*-bin" -and $_.Name -ne "gradle-$current-bin" } |
        ForEach-Object { Remove-Entry $_.FullName }
}

function Clear-OldNdk {
    $androidPy = Join-Path $root "python\mozboot\mozboot\android.py"
    $match = [regex]::Match((Get-Content -LiteralPath $androidPy -Raw), 'NDK_VERSION\s*=\s*"([^"]+)"')
    if (-not $match.Success) {
        Write-Warning "Unable to read NDK_VERSION from $androidPy"
        return
    }

    $current = "android-ndk-$($match.Groups[1].Value)"
    Get-ChildItem -LiteralPath $state -Directory -Force -Filter "android-ndk-*" |
        Where-Object { $_.Name -ne $current } |
        ForEach-Object { Remove-Entry $_.FullName }
}

$state = Join-Path $root ".mozbuild"
$gradleHome = Join-Path $root ".gradle"

$objDirs = Get-ChildItem -LiteralPath $root -Directory -Force |
    Where-Object { $_.Name -like "obj-*" -and (Test-Path -LiteralPath (Join-Path $_.FullName "config.status")) }
foreach ($objDir in $objDirs) {
    Clear-ObjDir $objDir.FullName
}

# Visual Studio and the toolchain archives serve local Gecko builds only. The
# mach virtualenv under srcdirs/ is kept: rebuilding it needs the network.
foreach ($name in @("vs", "clang-tools", "toolchains")) {
    Remove-Entry (Join-Path $state $name)
}
Clear-OldNdk

# Emulator state is recreated by `mach bootstrap`; drop it only on request.
if ($IncludeAvds) {
    Remove-Entry (Join-Path $state "android-device")
}

Clear-OldGradle

Get-ChildItem -LiteralPath (Join-Path $root "artifacts") -Directory -Force -Filter "docs-full-*" -ErrorAction SilentlyContinue |
    ForEach-Object { Remove-Entry $_.FullName }

# Downloaded SDK, NDK, and JDK archives; the extracted copies stay in place.
Get-ChildItem -LiteralPath (Join-Path $root ".tmp") -File -Force -ErrorAction SilentlyContinue |
    Where-Object { $_.Extension -in ".zip", ".xz", ".zst", ".gz" } |
    ForEach-Object { Remove-Entry $_.FullName }

if ($GitGc) {
    if ($DryRun) {
        Write-Output "would run git gc --prune=now"
    }
    else {
        & git -C $root gc --prune=now
    }
}
