# Synchronizing an official Firefox Android release

## Release model

Fenix uses official Firefox Android release tags as immutable baselines:

```text
FIREFOX-ANDROID_154_0_1_RELEASE  + Fenix QoL commits = fenix-154.0.1-r1
FIREFOX-ANDROID_155_0_RELEASE    + replayed commits  = fenix-155.0-r1
```

Daily development branches and prerelease build tags are not used as stable baselines. The canonical
current baseline is stored in `FENIX_UPSTREAM_RELEASE`.

The repository is intentionally usable as a shallow clone. Updating therefore fetches the exact old
and new release tags, applies the upstream tree delta to Fenix, and records a merge commit; it does
not require downloading the complete Firefox history.

## One-time remote layout

```powershell
git remote -v
```

Expected layout:

```text
origin    git@github.com:aenerv7/fenix.git
upstream  https://github.com/mozilla-firefox/firefox.git
```

The sync script adds `upstream` when it is absent and refuses to use an unrelated URL under that name.

## Prepare a release update

Start from a clean, tested `fenix` branch:

```powershell
git switch fenix
git status --short
.\tools\fenix\sync-official-release.ps1 -Version 155.0
```

The script performs these operations:

1. Validates the version and clean worktree.
2. Resolves `FIREFOX-ANDROID_155_0_RELEASE` from the official repository.
3. Creates `sync/firefox-android-155.0` from `fenix`.
4. Starts a merge with the new official tag and applies the upstream tree delta to Fenix.
5. Updates `FENIX_UPSTREAM_RELEASE` after the patch applies successfully.

It does not merge into `fenix`, create a release tag, push, or publish binaries.

## Resolve conflicts

If the Fenix patch cannot be applied cleanly, the merge remains open on the candidate branch. Resolve
each affected file, stage it, update the baseline marker, and commit the prepared merge:

```powershell
git status
git add <resolved-files>
git add FENIX_UPSTREAM_RELEASE
git commit -m "Update Fenix baseline to Firefox Android 155.0"
```

To abandon the candidate safely:

```powershell
git merge --abort
git switch fenix
git branch -D sync/firefox-android-155.0
```

After manually completing a conflicted sync, update `FENIX_UPSTREAM_RELEASE` to the new version.

## Validate the candidate

At minimum:

```powershell
.\tools\fenix\mach-local.ps1 gradle fenix:ktlint
.\tools\fenix\mach-local.ps1 gradle fenix:testDebugUnitTest
.\tools\fenix\mach-local.ps1 gradle fenix:assembleDebug
.\tools\fenix\mach-local.ps1 gradle fenix:assembleRelease
```

Also install the debug APK and manually verify:

- tab-group link routing and context menus;
- group-only multi-select, remove, delete, and undo;
- toolbar/menu positioning with gesture and three-button navigation;
- light and dark themes;
- absence of password, personal-information, sync, and Android autofill entry points;
- simplified Chinese strings;
- package name, app name, and version display.

## Promote the candidate

Commit the updated baseline marker and any conflict resolutions, then merge the candidate into
`fenix`:

```powershell
git add FENIX_UPSTREAM_RELEASE
git commit -m "Update Fenix baseline to Firefox Android 155.0"
git switch fenix
git merge --ff-only sync/firefox-android-155.0
git tag -a fenix-155.0-r1 -m "Fenix 155.0-r1"
git push origin fenix fenix-155.0-r1
```

## Automated check and merge

The `Sync Firefox Android release` GitHub Actions workflow checks official release tags every day
and can also be started manually. When a newer `FIREFOX-ANDROID_<VERSION>_RELEASE` tag exists, it
prepares an `automation/firefox-android-<VERSION>` branch, runs Fenix invariant checks, opens a pull
request, and merges it into `fenix`. A patch conflict or failed invariant check leaves `fenix`
unchanged and fails the workflow for manual review. The workflow does not build or publish APKs.

Keep the upstream release tag unchanged. Fenix tags identify the fork revision and must never be used
to impersonate an official Mozilla release.

## Source-to-binary traceability

If APKs are distributed, record their SHA-256 hashes and the exact Fenix tag in release notes. The
source URL and tag must remain available to recipients for as long as required by the applicable
licenses. Never attach signing keys or `signing-local.properties` to a release.
