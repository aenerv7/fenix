# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$state = Join-Path $root ".mozbuild"
$sdk = Join-Path $state "android-sdk-windows"
$ndk = Join-Path $state "android-ndk-r29"
$jdk = Join-Path $state "jdk\jdk-17.0.18+8"

@(
    ".mozbuild",
    ".gradle",
    ".cargo",
    ".rustup",
    ".android",
    ".tmp",
    "artifacts"
) | ForEach-Object {
    New-Item -ItemType Directory -Force -Path (Join-Path $root $_) | Out-Null
}

$env:MOZILLABUILD = Join-Path $root ".toolchain\mozilla-build"
$env:MACH_PS1_USE_MOZILLABUILD = "1"
$env:MOZBUILD_STATE_PATH = $state
$env:MOZCONFIG = Join-Path $root "tools\fenix\mozconfig-android-local"
$env:GRADLE_USER_HOME = Join-Path $root ".gradle"
$env:CARGO_HOME = Join-Path $root ".cargo"
$env:RUSTUP_HOME = Join-Path $root ".rustup"
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
$env:ANDROID_NDK_HOME = $ndk
$env:ANDROID_AVD_HOME = Join-Path $state "android-device\avd"
$env:ANDROID_USER_HOME = Join-Path $root ".android"
$env:JAVA_HOME = $jdk
$env:SCCACHE_DIR = Join-Path $root ".mozbuild\sccache"
$env:PYTHONUTF8 = "1"
$env:PYTHONIOENCODING = "utf-8"
$env:TEMP = Join-Path $root ".tmp"
$env:TMP = $env:TEMP
$env:PATH = (@(
    (Join-Path $root ".cargo\bin"),
    (Join-Path $sdk "platform-tools"),
    (Join-Path $sdk "cmdline-tools\21.0\bin"),
    (Join-Path $jdk "bin"),
    $env:PATH
) -join [IO.Path]::PathSeparator)

$quote = {
    param([string] $Value)

    $singleQuote = [char] 0x27
    $backslash = [char] 0x5c
    $escaped = $Value.Replace(
        [string] $singleQuote,
        [string] ($singleQuote, $backslash, $singleQuote, $backslash, $singleQuote -join "")
    )
    [string] $singleQuote + $escaped + [string] $singleQuote
}

$rootForBash = (& $quote $root.Replace([char] 0x5c, [char] 0x2f))
$mach = (& $quote (Join-Path $root "mach").Replace([char] 0x5c, [char] 0x2f))
$arguments = $args | ForEach-Object { & $quote ([string] $_) }
$command = "cd $rootForBash && " + ((@($mach) + $arguments) -join " ")

& (Join-Path $env:MOZILLABUILD "start-shell.bat") -no-start -defterm -c $command
exit $LASTEXITCODE
