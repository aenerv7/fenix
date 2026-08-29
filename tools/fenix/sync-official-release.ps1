# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern("^\d+\.\d+(\.\d+)?$")]
    [string] $Version,
    [string] $SourceBranch = "fenix",
    [string] $UpstreamRemote = "upstream"
)

$ErrorActionPreference = "Stop"
$officialRepository = "https://github.com/mozilla-firefox/firefox.git"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$baselineFile = Join-Path $root "FENIX_UPSTREAM_RELEASE"

function Invoke-Git {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]] $Arguments)

    & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed"
    }
}

Push-Location $root
try {
    $worktreeStatus = (& git status --porcelain=v1 --untracked-files=all) -join "`n"
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to inspect the worktree"
    }
    if ($worktreeStatus) {
        throw "The worktree must be clean before synchronizing a release"
    }

    $currentVersion = (Get-Content $baselineFile -Raw).Trim()
    if ($Version -eq $currentVersion) {
        throw "Fenix is already based on Firefox Android $Version"
    }

    $remoteUrl = (& git remote get-url $UpstreamRemote 2>$null)
    if ($LASTEXITCODE -ne 0) {
        Invoke-Git remote add $UpstreamRemote $officialRepository
        $remoteUrl = $officialRepository
    }
    if ($remoteUrl -notmatch "(^|[/:])mozilla-firefox/firefox(\.git)?$") {
        throw "Remote '$UpstreamRemote' is not the official Mozilla Firefox repository: $remoteUrl"
    }

    $currentTag = "FIREFOX-ANDROID_$($currentVersion.Replace('.', '_'))_RELEASE"
    $targetTag = "FIREFOX-ANDROID_$($Version.Replace('.', '_'))_RELEASE"
    $candidateBranch = "sync/firefox-android-$Version"

    & git show-ref --verify --quiet "refs/heads/$SourceBranch"
    if ($LASTEXITCODE -ne 0) {
        throw "Source branch does not exist: $SourceBranch"
    }
    & git show-ref --verify --quiet "refs/heads/$candidateBranch"
    if ($LASTEXITCODE -eq 0) {
        throw "Candidate branch already exists: $candidateBranch"
    }

    Invoke-Git fetch --no-tags $UpstreamRemote `
        "+refs/tags/$currentTag`:refs/tags/$currentTag" `
        "+refs/tags/$targetTag`:refs/tags/$targetTag" `
        --depth=1
    Invoke-Git switch --create $candidateBranch $SourceBranch

    & git merge --no-ff --no-commit --strategy=ours --allow-unrelated-histories $targetTag
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to prepare the merge with $targetTag"
    }

    $patchFile = Join-Path (Join-Path $root ".tmp") "fenix-upstream-sync.patch"
    New-Item -ItemType Directory -Force -Path (Split-Path $patchFile) | Out-Null
    try {
        & git diff --binary --full-index --no-ext-diff `
            $currentTag $targetTag --output="$patchFile"
        if ($LASTEXITCODE -ne 0) {
            throw "Unable to create the Firefox patch from $currentTag to $targetTag"
        }

        & git apply --3way --index $patchFile
        if ($LASTEXITCODE -ne 0) {
            throw @"
Firefox Android $Version conflicts with Fenix changes. Resolve the affected files on
'$candidateBranch', stage them, and commit the prepared merge manually.
"@
        }
    }
    finally {
        Remove-Item -LiteralPath $patchFile -Force -ErrorAction SilentlyContinue
    }

    Set-Content -LiteralPath $baselineFile -Value $Version -Encoding utf8NoBOM
    Write-Output "Prepared $candidateBranch on $targetTag"
    Write-Output "Review the diff, run the validation checklist, and commit FENIX_UPSTREAM_RELEASE."
}
finally {
    Pop-Location
}
