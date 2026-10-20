# Synchronizing an official Firefox Android release

## Release model

Fenix uses official Firefox Android release tags as immutable baselines:

```text
FIREFOX-ANDROID_154_0_1_RELEASE  + Fenix QoL commits = fenix-154.0.1-r1
FIREFOX-ANDROID_155_0_RELEASE    + upstream delta + Fenix changes = fenix-155.0-r1
FIREFOX-ANDROID_155_0_1_RELEASE  + upstream delta + Fenix changes = fenix-155.0.1-r1
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
.\tools\fenix\sync-official-release.ps1 -Version 155.0.1
```

The script performs these operations:

1. Validates the version and clean worktree.
2. Resolves `FIREFOX-ANDROID_155_0_1_RELEASE` from the official repository.
3. Creates `sync/firefox-android-155.0.1` from `fenix`.
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
git commit -m "Update Fenix baseline to Firefox Android 155.0.1"
```

To abandon the candidate safely:

```powershell
git merge --abort
git switch fenix
git branch -D sync/firefox-android-155.0.1
```

After manually completing a conflicted sync, update `FENIX_UPSTREAM_RELEASE` to the new version.

## Validate the candidate

At minimum:

```powershell
.\tools\fenix\mach-local.ps1 gradle fenix:spotlessKotlinCheck
.\tools\fenix\mach-local.ps1 gradle fenix:testDebugUnitTest
.\tools\fenix\mach-local.ps1 gradle fenix:assembleDebug
.\tools\fenix\build-release-local.ps1 -UseUpstreamGecko -Abi arm64-v8a
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
git commit -m "Update Fenix baseline to Firefox Android 155.0.1"
git switch fenix
git merge --ff-only sync/firefox-android-155.0.1
git tag -a fenix-155.0.1-r1 -m "Fenix 155.0.1-r1"
git push origin fenix fenix-155.0.1-r1
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

## Firefox Android 155.0.1 synchronization

The 155.0.1 update was prepared from the official `FIREFOX-ANDROID_155_0_1_RELEASE` tag in the
candidate worktree `sync/firefox-android-155.0.1`. The upstream delta was merged into the Fenix
155.0-r14 tree and all localization conflicts were resolved in favor of the existing Fenix product
policy, branding, and Simplified Chinese resources. The candidate merge is recorded in commit
`d6a29c7d19da`.

The update preserves the Fenix tab-group behavior that always opens the group sheet fully expanded,
the retired Sports/World Cup removal, privacy and autofill reductions, and Fenix branding. The
release uses the pinned official 155.0.1 arm64-v8a GeckoView package recorded in
`FENIX_UPSTREAM_GECKOVIEW.json`; local GeckoView compilation is prohibited because no Fenix-authored
Gecko or native source changed.

The retained release target is `fenix-155.0.1-r8`, with only the arm64-v8a APK published. The exact official
arm64-v8a versionCode is `2016182530`; the release checklist also verifies all 99 Gecko locales,
native library hashes, application ID, signature schemes, and the final APK checksum.

## Firefox Android 156.0 synchronization

The 156.0 update was prepared from the official `FIREFOX-ANDROID_156_0_RELEASE` tag on the candidate
branch `sync/firefox-android-156.0`. The upstream delta spans about 16.7k files. Conflicts were
resolved file by file in favor of the existing Fenix product policy, branding, and Simplified Chinese
resources; no locale or branding decision was taken from upstream.

The update preserves Fenix branding (`github.aenerv7.fenix`), the tab-group behavior, the IP
Protection entry point, and the removal of password, autofill, sync, and Play Store rating entry
points. The release uses the pinned official 156.0 arm64-v8a GeckoView package recorded in
`FENIX_UPSTREAM_GECKOVIEW.json`; local GeckoView compilation is prohibited because no Fenix-authored
Gecko or native source changed. The exact official arm64-v8a versionCode is `2016183650`.

The 156.0 baseline replaces ktlint with Spotless/ktfmt, so `fenix:ktlint` and `fenix:ktlintFormat` no
longer exist. Use `fenix:spotlessKotlinCheck` to verify formatting and `fenix:spotlessApply` to fix
it. Upstream also made `PreferenceFileInformation.xmlResourceId` nullable for Compose-based settings
screens, renamed `Core.summarizationSettings` to `summarizationSettingsBinding` and added its
`aiFeatureBlockStorage` argument, and renamed the IP Protection pill state from `proxyActiveShown` to
`proxyActivation == ProxyActivation.TurningOn`.

### Silent no-op merge hazard

`tools/fenix/sync-official-release.ps1` applies the upstream delta with a single `git apply --3way`
call. `git apply` is atomic: if any one file fails, it discards the entire patch and leaves the tree
unchanged while the merge commit may still be created. A 156.0 candidate was once committed in that
state: the merge changed only `FENIX_UPSTREAM_RELEASE`, so the source tree stayed on 155.0.1 while
the release metadata declared 156.0 and pinned the 156.0 GeckoView binaries. That mismatch crashed
the app on startup and the release had to be withdrawn.

Files the fork deliberately deleted (autofill, password manager, review prompt, Play Store rating,
Firefox wordmark) are the usual cause: upstream still modifies them, so `git apply` reports
`does not exist in index` and aborts everything.

Always verify a synchronized candidate before committing it:

```powershell
git diff --name-only <fenix-parent> <candidate> | Measure-Object -Line
```

The count must be close to the size of the upstream delta (tens of thousands of files), never one or
two. Apply the delta with `git apply --reject` so failures are isolated into `.rej` files instead of
discarding the whole patch, resolve those files, and confirm that upstream-added files actually exist
in the working tree. Never treat a zero exit code, or a merge commit that merely exists, as evidence
that the delta landed.

A baseline update also advances `CLOBBER` (156.0 carries the 2026-09-09 merge day clobber), so the
existing object directories require a clobber acknowledgement before the next `mach build`. Generated
sources such as `CrashReport.kt` are produced into the object directory from
`toolkit/crashreporter/CrashAnnotations.yaml`; they are not checked in, and a stale copy fails the
Kotlin build with unresolved `Annotation` members until the object directory is regenerated.

## Firefox Android 156.0.1 synchronization

The 156.0.1 update was prepared from the official `FIREFOX-ANDROID_156_0_1_RELEASE` tag on the
candidate branch `sync/firefox-android-156.0.1`, starting from the 156.0 baseline. The upstream delta
spans 306 files and roughly 15k added lines. The candidate changes 309 files against its Fenix parent,
which matches the delta size plus the baseline marker and the two pin files.

All 43 conflicts were confined to the Fenix localization resources `values-*/strings.xml`. They were
resolved with one rule: where `ours` and `theirs` define the same string name, the Fenix line wins
(branding and established terminology); where upstream adds new strings, upstream is taken. Six locales
(fa, hi-rIN, hr, is, sl, sr) contained branding conflicts in which upstream writes Firefox and the fork
writes Fenix; the Fenix wording was kept everywhere. `values-zh-rCN` needed the most care: upstream
removed `customize_toggle_jump_back_in` and `ip_protection_menu_auth_required`, added five VPN location
strings, and restructured the privacy-report and recent-tabs comments. The local duplicate entries that
resulted were removed, and the new strings were translated, restoring zero missing entries against
`values/strings.xml`.

The release uses the pinned official 156.0.1 arm64-v8a GeckoView package recorded in
`FENIX_UPSTREAM_GECKOVIEW.json`; local GeckoView compilation is prohibited because no Fenix-authored
Gecko or native source changed. The exact official arm64-v8a versionCode is `2016185922`.

### Stale staged GeckoView asset

A debug or test Gradle run replaces the object directory's staged GeckoView `omni.ja` with an
`en-US`-only copy. A subsequent `-UseUpstreamGecko` release build imports the correct native libraries
but the Gradle asset-merge tasks still report `UP-TO-DATE`, so the assembled APK keeps the
single-locale `omni.ja` and the release script fails with `has an invalid Gecko locale set`. The first
156.0.1 build attempt failed exactly this way.

Diagnose by reading `res/multilocale.txt` inside `assets/omni.ja` of the assembled APK; it contains
only `en-US` instead of the 99-locale list. Fix by deleting the stale copies under
`<objdir>\dist\geckoview\assets\omni.ja` and
`<objdir>\gradle\build\mobile\android\**\merge*Assets\omni.ja`, then rebuilding with
`-UseUpstreamGecko`. Never repair this by compiling Gecko locally.

## Firefox Android 157.0 synchronization

The 157.0 update was prepared from the official `FIREFOX-ANDROID_157_0_RELEASE` tag on the candidate
branch `sync/firefox-android-157.0`, starting from the 156.0.1 baseline. The upstream delta spans
15460 files and about 1.25M added lines.

The delta was applied with `git apply --reject`, and every rejected file was then reconciled with a
three-way merge against the fork's own revision of that file, using the 156.0.1 blob as the base.
30 files were rejected; 21 of them held 38 real conflicts, all inside `mobile/android/fenix`. Two
rejected paths are files the fork had deleted (the autofill service and the review prompt) that
upstream still modifies; the fork deletions stand. The candidate changes 15458 files against its
Fenix parent, which is the upstream delta minus those two paths.

Upstream changes the fork adopts, with the fork's product policy reapplied:

- `TabsUseCases.removeTab` and `removeTabs` renamed the `excludedTabIds` argument to
  `excludedFallbackTabIds`. The fork's `getExcludedNormalTabIds(closingTabIds)` keeps its extra
  argument and is renamed `getExcludedFallbackNormalTabIds` to match.
- `ExpandedTabGroupActions` gained `onUngroupTabGroupClick`, which the expanded group ignores like
  the other group-level actions. `onAddNewTabClick` stays non-nullable because the fork requires the
  add-tab action to remain visible, so upstream's `else null` branch is not taken.
- The efficiency test framework replaced `groups = listOf("...")` with `setOf(Group.…)` and the
  enumerated `all` list with `scrollTraversalOrder`. The fork keeps its removal of the
  page-summaries and Google Play rating selectors and of their navigation registrations.
- `MenuBanner` was reduced upstream to a wrapper around the components banner, which draws the
  Mozilla illustration. The fork keeps its own text-only banner.
- `GeckoProvider` keeps the fork's reduced API: no autofill or login storage, and no
  `applicationScope` argument on `createRuntime`.
- `TabLayoutTest` and `BrowserToolbarMiddlewareTest` are fork rewrites. Upstream's new tests for the
  tab-group onboarding callback, the homepage-as-new-tab back and forward actions, and the wide
  toolbar were re-added on top of the fork's tests.
- `values-*/strings.xml`: `browser_menu_change_wallpaper` became `browser_menu_customize_homepage`,
  `ip_protection_title` became `ip_protection_settings_title`, and
  `ip_protection_data_limit_reached_snackbar_action` became
  `ip_protection_open_settings_snackbar_action`. Branding conflicts keep the Fenix wording.

The delta adds no fixed-term activity, so the activity-removal policy had nothing to remove.
Branding improved: upstream deleted the `fox_alert_crash_dark` and `fox_alert_crash_light` fox
vectors used by the startup crash screen and replaced them with the non-fox `ic_kit_plug_error`,
`kit_plug_error`, and `kit_devices_sync*` illustrations.

`values-zh-rCN` has no missing entry against `values/strings.xml` and no duplicate name: the two new
strings were translated, and the two renamed entries reuse the established wording.

The release uses the pinned official 157.0 arm64-v8a GeckoView package recorded in
`FENIX_UPSTREAM_GECKOVIEW.json`; local GeckoView compilation is prohibited because no Fenix-authored
Gecko or native source changed. The exact official arm64-v8a versionCode is `2016186458`.

### Stale generated crash-annotation sources

`CrashReport.kt` and `CrashReport.java` are generated into the object directory from
`CrashAnnotations.yaml` through `load.py` and `generate.py`. The copies left by an earlier baseline
no longer matched the annotations the Android Components sources use, and Gradle's up-to-date checks
had hidden the mismatch until the merge invalidated those tasks. `mach build faster` cannot repair
them on this host: `third_party/angle` generates `angle_commit.h` with `file_generate_wrapper.py`
and the wrapper passes `/third_party/angle/` as the working directory, which does not exist on
Windows (`NotADirectoryError`). Regenerate the two files directly with the mach build virtualenv
Python, passing forward-slash paths so that `generate.py` can derive the package name, and never by
compiling Gecko for a release.

## Firefox Android 157.0.1 synchronization

The 157.0.1 update was prepared from the official `FIREFOX-ANDROID_157_0_1_RELEASE` tag on the
candidate branch `sync/firefox-android-157.0.1`, starting from the 157.0 baseline. The upstream delta
spans 343 files (54 added, 1 deleted, 288 modified).

The delta applied with `git apply --3way` left conflicts in 9 `mobile/android/fenix`
`values-*/strings.xml` files (`values-el`, `values-en-rCA`, `values-es-rCL`, `values-hu`,
`values-lv`, `values-oc`, `values-tt`, `values-uk`, `values-zh-rCN`). Every hunk was resolved on the
fork side, and the genuinely new upstream translations whose name exists in the fork's
`values/strings.xml` were then appended. This keeps the fork's branding and its removed entries
while matching the fork's string set. The candidate changes 344 files against its Fenix parent (the
343 upstream changes plus `FENIX_UPSTREAM_RELEASE`).

`values-zh-rCN` remains complete: no missing entry against `values/strings.xml` and no duplicate
name. Upstream added translations only for existing strings in the other eight conflicted locales;
it added and removed no English string in this patch release.

The release uses the pinned official 157.0.1 arm64-v8a GeckoView package recorded in
`FENIX_UPSTREAM_GECKOVIEW.json`; local GeckoView compilation is prohibited because no Fenix-authored
Gecko or native source changed. The exact official arm64-v8a versionCode is `2016188626`.

## Firefox Android 158.0 synchronization

The 158.0 update was prepared from the official `FIREFOX-ANDROID_158_0_RELEASE` tag
(`3c71aeacdb29`) on the candidate branch `sync/firefox-android-158.0`, starting from the 157.0 baseline
that this clone still had. The upstream delta spans 15042 files from 157.0; from the 157.0.1 baseline
that the remote branch reached in parallel it spans 14748 files.

### Integrating the parallel 157.0.1 baseline

The remote `fenix` branch gained its 157.0.1 synchronization (baseline commit, artifact pins,
documentation, documentation gate record) while this clone was still on 157.0, so those commits were
merged into the 158.0 branch instead of being replaced, and the release candidate is that merge:
14763 files change against its first parent, the 157.0.1 baseline.

The merge conflicted in 40 files. Version stamps and upstream files whose 157.0.1 change is contained
in 158.0 take the 158.0 side. The 15 fenix `values-*/strings.xml` conflicts resolve to content
identical to the 158.0 tree: every string name the 157.0.1 side carried was already present with the
same text. `mobile/android/fenix` and `mobile/android/android-components` are therefore unchanged
against the revision the release APK was built from, and the APK did not need a rebuild. The changelog,
sync, and documentation-toolchain documents keep both baseline sections.

### Three-way merge instead of a patch application

The delta was not applied with `git apply`. The upstream diff was fed to

```text
git merge-tree --write-tree --merge-base=FIREFOX-ANDROID_157_0_RELEASE HEAD FIREFOX-ANDROID_158_0_RELEASE
```

and the resulting tree became a merge commit whose second parent is the 158.0 tag. This removes the
silent no-op hazard documented for the 156.0 synchronization: every file the fork did not touch takes
upstream's new content, files the fork did touch receive the merge of both edits, and only files
where the two sides change the same lines carry conflict markers. For this delta that is 31 files
with 46 conflict blocks, all inside `mobile/android/fenix` plus the root `AGENTS.md`; the same delta
through `git apply --reject` rejects 70 files, because a patch cannot merge hunks that a real
three-way merge resolves.

The candidate changes 15040 files against its Fenix parent, which is the upstream delta from 157.0
minus the fork's rename/delete pair for the Mozilla wordmark.

### Upstream moves the fork adopts

- Tab-group storage moved out of the app into the android-components module
  `feature-tabgroups-storage`. The fork's imports become
  `mozilla.components.feature.tabgroups.storage.{data,database,repository,fakes}`, and
  `createTabGroupWithTabs` receives the tab ids inside `TabGroup(tabIds = …)`.
- `TabInteractionHandler` became `ItemInteractionHandler`, `TabItemInteractionState` became
  `ItemInteractionState`, and the tab item animations moved from `SharedTabItemUi.kt` into the new
  `tabstray/ui/tabitems/TabItemAnimations.kt`.
- `TabGroupMiddleware` moved to `org.mozilla.fenix.tabgroups.middleware`.
- `TabGroupAction.TabAddedToGroup` became `TabAddedToExistingTabGroup`; upstream's
  `TabAddedToNewTabGroup` is still an empty branch (bug 2069535).
- Upstream renamed `Wordmark.kt` to `WordmarkAndLogo.kt` and reworked it. The fork keeps its own
  rabbit wordmark and its deletion of the Mozilla one, so the upstream file stays deleted.
- `MenuAction.Navigate.EditBookmark` gained `guidToEdit`, and upstream's new menu provider classes
  are kept. The fork's removal of the passwords entry — the `MainMenu` library item, the
  `onPasswordsMenuClick` parameter, `MenuAction.Navigate.Passwords`, and its navigation and
  telemetry branches — is re-applied to the new structure.

### Fork behaviour reapplied

- `TabItemAnimations.kt` is new upstream code, so the fork's item animation policy had to be applied
  to it again: `defaultGridItemAnimation` and `defaultListItemAnimation` pass `fadeOutSpec = null`
  unconditionally, so a removed item leaves the layout instead of fading out detached from it.
- The expanded group keeps its group-scoped multi-select, its `selectionBanner`/`selectionMenu`
  overlays, the system Back handler that consumes Back while that toolbar is open, and the
  always-visible add-new-tab action.
- `TabStorageMiddleware` keeps the fork's storage actions for tabs removed from a group, tabs closed
  from a group, and undo that restores tabs into their group.
- The limited-time activity policy had nothing to remove: the delta adds no campaign, tournament, or
  other fixed-term activity.
- `values-zh-rCN` gained the 17 names that the 158.0 delta adds to `values/strings.xml`, translated
  with the terminology already used in that file; it has no missing name and no duplicate name.

### Release pins

The release uses the pinned official 158.0 arm64-v8a GeckoView package recorded in
`FENIX_UPSTREAM_GECKOVIEW.json`; local GeckoView compilation is prohibited because no Fenix-authored
Gecko or native source changed. The exact official arm64-v8a versionCode is `2016189050`, and the
official APK SHA-256 is `4E57F3D92B5F77FEAB700D454E8EFB5030279855A2BAE7DB1A14521AC60DF466`.

### Three-way merge hazards

A merge tree is not self-validating either. `git merge-tree` resolves a file whenever the two sides
edit different regions, which hides the case where upstream rewrote the region the fork had customised
into a different shape. Four files lost fork code that way and had to be rebuilt by hand:

- `tabgroups/ExpandedTabGroup.kt`: upstream's version has no group selection toolbar, no BackHandler,
  no snackbar and no `FixedBottomPopupPositionProvider`.
- `tabstray/ui/TabManagementFragment.kt`: `RenderExpandedTabGroup` lost its selection mode,
  `onExitSelectMode`, `onBack`, `onItemLongClick`, `selectionBanner`, `selectionMenu` and
  `snackbarHostState`, and upstream's `if (homepageAsNewTabEnabled) … else null` would have hidden
  the add-new-tab action.
- `tabstray/ui/tabitems/SharedTabItemUi.kt`: the fork's animation tail duplicated what upstream moved
  into `TabItemAnimations.kt`.
- `tabstray/data/TabsTrayItem.kt`: `shouldFullyExpandOnFirstOpen` and its two count constants
  disappeared while four callers remained.

Two API divergences also need a decision rather than a merge:

- The fork replaced upstream's `BottomSheetSceneStrategy.bottomSheet(fullyExpandOnFirstOpen = …)` with
  its own `skipOpeningAnimation`, which starts the sheet in the expanded state instead of expanding it
  after a delay. Upstream's new callers in `tabgroups/flow/TabGroupFlowFragment.kt` and
  `tabstray/ui/TabManagementFragment.kt` are mapped onto the fork's parameter.
- Because the fork adds `SelectedTabsRemovedFromGroup`, `SelectedTabsClosedFromGroup` and
  `RestoreTabsToGroup` to `TabGroupAction`, upstream's new exhaustive `when` statements in
  `tabgroups/flow/TabGroupFlowReducer.kt` and `tabgroups/flow/TabGroupFlowTelemetryMiddleware.kt`
  list them as no-ops.

`BackgroundServicesTest.kt` is reverted to the fork's revision: the automotive tests upstream adds
there cover the password and credit-card sync engines the fork removes, and they construct
`BackgroundServices` with storage arguments the fork's constructor does not take.

Named arguments the fork adds at a call site are the quietest loss of all: they leave no dangling
reference, so the file compiles and the behaviour simply reverts. Rebuilding
`tabstray/ui/TabManagementFragment.kt` from upstream dropped `skipPartiallyExpanded = true` and
`modalBottomSheetProperties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)` from the
expanded-group sheet entry, which put the group sheet back into the partially expanded state that the
fork removes. When a merge resolution rewrites a file from upstream, diff the fork's call sites in that
file (`git diff HEAD^1 <candidate> -- <file>` plus the previous baseline) and check every named
argument the fork had added. Keep such a set of arguments in one named helper rather than repeating it
at each call site, and assert it in a unit test: the group sheet now goes through
`BottomSheetSceneStrategy.tabGroupSheet`, covered by `BottomSheetSceneStrategyTest`.

### Toolchain updates the baseline forces

`python/mozboot/mozboot/android.py` raises three pinned toolchain versions, and configure dies when the
installed tool reports a different one, so the local toolchain state had to be updated before the first
configure:

- `NDK_VERSION` `r29` → `r30`, installed as `.mozbuild/android-ndk-r30`;
- `CMDLINE_TOOLS_VERSION` `21.0` → `23.0`, installed as
  `.mozbuild/android-sdk-windows/cmdline-tools/23.0`;
- the Gradle JDK `17.0.18+8` → `17.0.20.1+1`, installed under `.mozbuild/jdk`.

`tools/fenix/mach-local.ps1` points `ANDROID_NDK_HOME`, the `PATH` command-line tools entry, and
`JAVA_HOME` at those directories.

The baseline also adds a `CbindgenHeader` for the new `dom/push/rust` crate, and its generation runs
`cargo metadata --frozen` with the object directory's `.cargo/config.toml`. That file is a preprocessed
copy of `.cargo/config.toml.in` and is only rewritten by the `misc` target, so an object directory left
over from an older baseline keeps the old git source pins and cargo fails with
`failed to load source for dependency` (or cbindgen reports `Unable to find dom_push`). Run
`mach build misc` once after a baseline update to regenerate it before `mach build faster`.

The baseline also changes the `build` command-site definition, so mach recreates
`.mozbuild/srcdirs/<srcdir>/_virtualenvs/build`. On Windows, Gradle runs `mach configure` through that
virtualenv's `python.exe`, and mach cannot delete the interpreter it is running under, so `:machConfigure`
fails with `PermissionError: [WinError 5] ... Scripts/python.exe`. Run one build-site command through
`tools/fenix/mach-local.ps1` (for example `configure`) so the virtualenv is rebuilt under MozillaBuild's
Python before Gradle is invoked again.

## Firefox Android 158.0.1 synchronization

The 158.0.1 update was prepared from the official `FIREFOX-ANDROID_158_0_1_RELEASE` tag
(`9378fbc58660`) on the candidate branch `sync/firefox-android-158.0.1`, starting from the 158.0-r2
tree. The upstream delta spans 395 files (8586 insertions, 7245 deletions) and is almost entirely
localization. The Fenix-side code changes are an Application Services bump to 158.0.1, the new
Merino manifest, and `initial_experiments.json`; no GeckoView, Gecko, C++, or Rust source changes.

The delta was applied with `git apply --3way`. Ten fenix `values-*/strings.xml` files conflicted
(`values-fa`, `values-fy-rNL`, `values-hy-rAM`, `values-kk`, `values-lv`, `values-pl`,
`values-pt-rBR`, `values-sk`, `values-uk`, `values-zh-rCN`). The delta changes no English string in
`values/strings.xml` or `values/arrays.xml`, so every conflict re-translates a name the fork already
carries and was resolved on the fork side.

The candidate changes 386 files against its Fenix parent: the 385 upstream files whose contents
differ, plus `FENIX_UPSTREAM_RELEASE`. The ten conflicted locale files resolve to the fork's existing
content, so they do not appear in that count. `values-zh-rCN` still has no missing entry against
`values/strings.xml` and no duplicate name.

The release uses the pinned official 158.0.1 arm64-v8a GeckoView package recorded in
`FENIX_UPSTREAM_GECKOVIEW.json`; local GeckoView compilation is prohibited because no Fenix-authored
Gecko or native source changed. The exact official arm64-v8a versionCode is `2016191346`, and the
official APK SHA-256 is `A5F247C6729BBAE1F970AA7FEB58AB7CA55570241D4BF536A9B3DE602477CA11`.

## Source-to-binary traceability

If APKs are distributed, record their SHA-256 hashes and the exact Fenix tag in release notes. The
source URL and tag must remain available to recipients for as long as required by the applicable
licenses. Never attach signing keys or `signing-local.properties` to a release.
