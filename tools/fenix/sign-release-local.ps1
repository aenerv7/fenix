# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

param(
    [ValidateSet("arm64-v8a", "armeabi-v7a", "x86_64")]
    [string[]] $Abi = @("arm64-v8a", "armeabi-v7a", "x86_64"),
    [string] $VersionName,
    [string] $KeyStorePath,
    [string] $KeyAlias = "magi-opensource",
    [string] $PropertiesPath
)

$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$unsignedDirectory = Join-Path $root "artifacts\fenix-release\unsigned"
$signedDirectory = Join-Path $root "artifacts\fenix-release\signed"

$baseline = (Get-Content (Join-Path $root "FENIX_UPSTREAM_RELEASE") -Raw).Trim()
if (-not $VersionName -or $VersionName -notmatch "^$([regex]::Escape($baseline))-r[1-9]\d*$") {
    throw "VersionName must match the current baseline: $baseline-rN"
}
if (-not $KeyStorePath) {
    $KeyStorePath = Join-Path $root "MAGI-OpenSource.jks"
}
if (-not $PropertiesPath) {
    $PropertiesPath = Join-Path $root "signing-local.properties"
}

$properties = @{}
Get-Content $PropertiesPath | ForEach-Object {
    if ($_ -match "^([^#=]+)=(.*)$") {
        $properties[$matches[1].Trim()] = $matches[2].Trim()
    }
}

if (-not $properties.storePassword -or -not $properties.keyPassword) {
    throw "$PropertiesPath must define storePassword and keyPassword"
}

$buildToolsRoot = Join-Path $root ".mozbuild\android-sdk-windows\build-tools"
$buildTools = Get-ChildItem $buildToolsRoot -Directory |
    Sort-Object { [version] $_.Name } -Descending |
    Select-Object -First 1
if (-not $buildTools) {
    throw "No Android build-tools installation found under $buildToolsRoot"
}

$signer = Join-Path $buildTools.FullName "apksigner.bat"
$variants = [ordered] @{
    "arm64-v8a" = "fenix-arm64-v8a-release.apk"
    "armeabi-v7a" = "fenix-armeabi-v7a-release.apk"
    "x86_64" = "fenix-x86_64-release.apk"
}
$selectedVariants = @($variants.GetEnumerator() | Where-Object { $_.Key -in $Abi })

$env:FENIX_STORE_PASSWORD = $properties.storePassword
$env:FENIX_KEY_PASSWORD = $properties.keyPassword

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
New-Item -ItemType Directory -Force -Path $signedDirectory | Out-Null

try {
    foreach ($variant in $selectedVariants) {
        $source = Join-Path $unsignedDirectory $variant.Value
        $target = Join-Path $signedDirectory "Fenix-$VersionName-$($variant.Key)-release.apk"

        if (-not (Test-Path -LiteralPath $source)) {
            throw "Missing unsigned APK: $source"
        }

        $apk = [IO.Compression.ZipFile]::OpenRead($source)
        try {
            foreach ($library in @("libmozglue.so", "libxul.so")) {
                $entry = "lib/$($variant.Key)/$library"
                if (-not $apk.GetEntry($entry)) {
                    throw "$source is missing $entry"
                }
            }
        }
        finally {
            $apk.Dispose()
        }

        if (Test-Path -LiteralPath $target) {
            Remove-Item -LiteralPath $target -Force
        }

        & $signer sign `
            --ks $KeyStorePath `
            --ks-key-alias $KeyAlias `
            --ks-pass env:FENIX_STORE_PASSWORD `
            --key-pass env:FENIX_KEY_PASSWORD `
            --out $target `
            $source

        if ($LASTEXITCODE -ne 0) {
            throw "Signing failed for $($variant.Key)"
        }

        & $signer verify --verbose $target
        if ($LASTEXITCODE -ne 0) {
            throw "Signature verification failed for $($variant.Key)"
        }

        Write-Output "Signed $([IO.Path]::GetFileName($target))"
    }
}
finally {
    Remove-Item Env:FENIX_STORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:FENIX_KEY_PASSWORD -ErrorAction SilentlyContinue
}
