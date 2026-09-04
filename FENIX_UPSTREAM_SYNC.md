# Synchronizing an official Firefox Android release

## Release model

Fenix uses official Firefox Android release tags as immutable baselines:

```text
FIREFOX-ANDROID_154_0_1_RELEASE  + Fenix QoL commits = fenix-154.0.1-r1
FIREFOX-ANDROID_155_0_RELEASE    + upstream delta + Fenix changes = fenix-155.0-r1
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

Run the full source-documentation gate with a new output directory after each baseline update. The
command, failure classification, and upstream dependency policy are documented in
[FENIX_DOCS_TOOLCHAIN.md](FENIX_DOCS_TOOLCHAIN.md). Keep documentation toolchain compatibility fixes
separate from Android product and release commits.

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

## Manual synchronization policy

Baseline updates are intentionally initiated, reviewed, validated, and promoted manually. There is
no scheduled GitHub Actions workflow that detects a new Firefox Android release or merges it into
`fenix`. The fork changes product behavior across enough upstream-owned Android code that a clean
textual patch application is not sufficient evidence of a correct update. Each baseline change must
also apply the activity-removal policy, reconcile upstream features that overlap Fenix behavior,
review branding and localization, and complete the validation checklist above.

Keep `tools/fenix/sync-official-release.ps1` as the mechanical preparation step. It fetches the exact
old and new official tags, creates a candidate branch, and applies the upstream tree delta. It must
not be treated as approval to merge: resolve conflicts file by file, review the complete candidate,
run the required validation, and promote it with the manual procedure above.

Keep the upstream release tag unchanged. Fenix tags identify the fork revision and must never be used
to impersonate an official Mozilla release.

## First upstream merge: Firefox Android 155.0

The Firefox Android 155.0 update was the first upstream synchronization performed after this fork's
initial Fenix baseline. The starting point was `fenix-154.0.1-r8`, with
`FIREFOX-ANDROID_154_0_1_RELEASE` recorded in `FENIX_UPSTREAM_RELEASE`.

The manually dispatched workflow run [33509955749](https://github.com/aenerv7/fenix/actions/runs/33509955749)
successfully found `FIREFOX-ANDROID_155_0_RELEASE`, but its synchronization job stopped on 27 merge
conflicts. The failed workflow left `fenix` unchanged, so the update was completed in a separate
candidate worktree on `sync/firefox-android-155.0`.

The manual procedure was:

1. Fetch the exact official 154.0.1 and 155.0 release tags and run
   `tools/fenix/sync-official-release.ps1 -Version 155.0`.
2. Apply the official tree delta to the candidate and resolve conflicts file by file. Existing Fenix
   behavior and local-only files were preserved when they did not conflict with the 155.0 APIs.
3. Reconnect local tab-group routing, scoped multi-select, Fenix branding, missing Simplified Chinese
   strings, IP protection entry points, and other documented QoL changes where the upstream layout
   had moved them.
4. Remove only obsolete code that had no production references after the API update, and update the
   affected tests and resource keys. Do not restore a local feature only because it existed in the
   previous baseline.
5. Apply the limited-time activity policy before preserving upstream-only additions. An activity
   tied to a finished event, campaign, tournament, experiment, or other fixed end date is removed at
   the product level unless it has been explicitly adopted as a permanent Fenix feature. Remove its
   state, actions, reducers, entry points, controller/interactor code, tests, localized strings, and
   dedicated artwork or other resources. Keep shared infrastructure and unrelated settings whose
   names merely overlap with the activity. In this merge, that policy removed the retired Sports/World
   Cup activity and its flags and artwork, while the unrelated search-optimization settings containing
   the word `sports` were retained.
6. Promote the tested candidate to `fenix`, create the annotated `fenix-155.0-r1` tag, and publish
   the arm64-v8a APK. The release follows the existing Fenix convention and publishes the APK only;
   the local `.idsig` remains available for verification and re-signing but is not a release asset.

The resulting source history is split into focused commits:

- `9c58145ed133`: Firefox Android 155.0 baseline and upstream tree delta;
- `15b02ce5be2e`: Kotlin formatting fixes;
- `d68c9c9c32dc`: compatibility conflict resolutions and removal of dead obsolete files;
- `747944944a84`: test compatibility fixes;
- `a8e6b79f2160`: retired Sports/World Cup resource removal;
- `8800eb3358db`: README release metadata update.

Validation for this first merge included `fenix:ktlint`, `fenix:compileDebugKotlin`, a full Gecko
build, targeted tab-group/browser-use-case unit tests, and the repository release script. The release
script verified 99 Gecko locales including `zh-CN`, matching arm64-v8a native libraries, the package
ID `github.aenerv7.fenix`, and APK Signature Scheme v2/v3. The full Windows unit-test task still has
the documented JNA/native limitation; failures occur while loading `jnidispatch.dll` before affected
test bodies run, so they are not treated as a 155.0 product regression without a Windows-native test
artifact.

The published result is [Fenix 155.0-r1](https://github.com/aenerv7/fenix/releases/tag/fenix-155.0-r1).
Its release notes record the exact source tag and APK SHA-256. Future upstream updates should reuse
this sequence, review each conflict against the current Fenix behavior, and run the limited-time
activity policy before assuming that every upstream file from the previous release should be kept.

## Source-to-binary traceability

If APKs are distributed, record their SHA-256 hashes and the exact Fenix tag in release notes. The
source URL and tag must remain available to recipients for as long as required by the applicable
licenses. Never attach signing keys or `signing-local.properties` to a release.
