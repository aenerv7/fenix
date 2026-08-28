# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$cache = Join-Path $root ".mozbuild\searchfox-cache"
$executable = Join-Path $root "searchfox-cli\bin\searchfox-cli.exe"

if (-not (Test-Path -LiteralPath $executable)) {
    $executable = Join-Path $root "searchfox-cli\searchfox-cli.exe"
}

if (-not (Test-Path -LiteralPath $executable)) {
    throw "searchfox-cli is not installed under $root\searchfox-cli"
}

New-Item -ItemType Directory -Force -Path $cache | Out-Null
$env:XDG_CACHE_HOME = $cache

& $executable @args
exit $LASTEXITCODE
