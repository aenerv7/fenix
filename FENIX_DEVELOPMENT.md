# Fenix development

## Scope

Fenix is maintained as a focused Android fork. Use `fenix:*` Gradle tasks and do not build Focus.
Mozilla's general Firefox source documentation remains authoritative for the rest of the tree.

## Current fork customization summary

The current upstream baseline is Firefox Android 155.0.1 (`FIREFOX-ANDROID_155_0_1_RELEASE`). The first
upstream synchronization, including manual conflict resolution and the limited-time activity policy,
is recorded in [FENIX_UPSTREAM_SYNC.md](FENIX_UPSTREAM_SYNC.md). Release-by-release user-facing notes
are kept in [FENIX_CHANGELOG.md](FENIX_CHANGELOG.md).

### Upstream merge and activity policy

- Baseline updates are manually initiated and promoted. No scheduled GitHub Actions workflow merges
  new Firefox Android releases into `fenix`. Use `tools/fenix/sync-official-release.ps1` to prepare a
  candidate, then resolve and review it file by file, apply the product policies below, and complete
  the documented validation before promotion.
- The 155.0 update was the first upstream merge after the original 154.0.1 Fenix baseline. Conflicts
  were resolved file by file, preserving fork behavior and local-only files where they remained
  compatible with the new upstream APIs.
- A finished campaign, tournament, experiment, or other fixed-term activity is not retained merely
  because it exists in the previous baseline. Remove its state, actions, reducers, entry points,
  controller/interactor code, tests, strings, artwork, and flags. Keep shared infrastructure and
  unrelated settings whose names only happen to overlap. The retired Sports/World Cup activity was
  removed under this rule.
- Upstream features that duplicate a fork-added control take precedence. The fork-added floating
  new-tab action inside expanded groups was removed after the upstream group-toolbar plus action was
  confirmed; the upstream action is required to remain visible on phones, tablets, and both
  orientations.

### Branding and localization

- Visible Mozilla Firefox fox assets are removed from the fork's user-facing surfaces. Fenix Labs,
  the About screen, the home-screen search widget, and launcher assets use the Fenix rabbit branding;
  the default-browser prompt no longer displays Mozilla artwork. The Labs and in-app rabbit mark is
  background-free; the launcher foreground uses a transparent canvas with the rabbit scaled to 80% so
  the app icon is not oversized. The widget and launcher variants must be checked separately because
  they use different Android resource paths.
- The add-widget preview must remain a wide horizontal search field. Its rabbit belongs in a larger,
  contained left icon area, while the runtime layouts reserve separate space for the search text and
  microphone action. The Fenix Labs welcome banner is text-only; the rabbit is reserved for the Labs
  empty state instead of being used as a small banner decoration.
- The default-browser prompt no longer reserves space for the removed fox artwork; its close button
  is vertically centered at the right edge.
- The About Fenix screen displays the fork revision after the upstream version line using localized
  Android resources. This revision is display metadata only and never changes `versionName` or
  `versionCode`. The About "What's new" link and text refer to Fenix rather than Firefox.
- Release notes are always bilingual: a complete `## 中文` section followed by an equivalent
  `## English` section.
- Because GitHub keeps only the latest successful revision for each upstream baseline, the retained
  release notes must list all effective user-facing Fenix changes relative to the exact official
  upstream baseline, not only changes since the previous `rN`. Exclude intermediate behavior that
  was later reverted, and keep artifact metadata and validation results current for the retained APK.
  Before replacing an `rN`, derive the notes from the final source tree and baseline diff, verify that
  the Chinese and English sections describe the same changes, and only then remove the superseded
  remote release and tag.

### Tabs and tab groups

- Expanded groups reuse the All Tabs selection toolbar styling and content width. Long-pressing a
  group tab dispatches the group-scoped selection action, so the group toolbar appears without
  changing the global tab-tray navigation behavior.
- Back while a group-tab long-press selection toolbar is open is consumed by the expanded-group
  screen. It closes the toolbar and clears selection/local drag state once; it must not collapse the
  group or run the root back action a second time.
- Closing the last ungrouped tab while a group remains must remove the item from both the immutable
  browser-tab snapshot and the Lazy list/grid interaction state immediately. Reset missing drag keys
  without starting a stale animation, so the removed tab cannot remain visible, selected, or
  interactable until a later navigation.
- The upstream long-press gesture sequence is intentionally preserved. Do not synchronously change
  the `pointerInput` mode from the long-press callback: doing so cancels the active gesture. Normal
  long-press drag paths use `preserveSelectMode = false`; selection-mode synchronization waits until
  the active drag key is cleared. This lets a held long press select a tab and still lets a continued
  drag move it. The corresponding guard is currently an unpublished working-tree fix after r10 and
  must be verified before the next release.

### Release and GeckoView decisions

- Keep the exact upstream ABI-specific `versionCode`; the fork `rN` suffix is only release metadata
  shown in About Fenix. Changing `versionCode` would break normal overwrite and downgrade paths.
- Publish only the selected `arm64-v8a` APK for the current release workflow. `.idsig` files remain
  local verification/re-signing inputs and are not GitHub Release assets.
- For Fenix-only changes, always import the pinned official multi-locale Fenix APK with
  `-UseUpstreamGecko`; never build or package GeckoView locally. Validate baseline, Gecko revision,
  ABI, all locales, `omni.ja`, native libraries, versionCode, signature, and checksums before
  assembly. Never accept an API-only or single-locale package as a release GeckoView.
- Local GeckoView packaging is an explicit exception only when Fenix itself changes GeckoView,
  Gecko, C++, Rust, or Gecko locale sources. An upstream baseline update must use that baseline's
  pinned official package. Use `-BuildLocalGecko` only for Fenix-authored native or Gecko locale
  changes and record those changes in the release notes.
- GitHub Release retention is per upstream baseline: after the new release is verified, retain only
  the highest successfully published `rN` release and matching remote tag for that baseline. This
  does not require deleting local APKs, `.idsig` files, notes, logs, or reusable build caches.

### Current validation state

The 155.0.1-r3 candidate preserves the tested 155.0-r14 Fenix changes while applying the official
155.0.1 upstream delta. Its validation results are recorded in the release notes. The arm64-v8a
release uses the pinned official 155.0.1 GeckoView package through `-UseUpstreamGecko`; no local
GeckoView build is permitted for this baseline update. The Windows `FenixGleanTestRule`
native-library limitation remains documented below; affected tests need Linux or CI coverage even
when the Windows task completes by skipping them.

## Repository-local state

The wrapper at `tools/fenix/mach-local.ps1` redirects developer state into the checkout:

| State | Repository path |
| --- | --- |
| Mozilla toolchains and Android SDK/NDK | `.mozbuild/` |
| Gradle cache | `.gradle/` |
| Cargo and Rustup | `.cargo/`, `.rustup/` |
| Android user data and AVDs | `.android/`, `.mozbuild/android-device/` |
| Temporary files | `.tmp/` |
| Build output | `obj-firefox-android-aarch64/`, `obj-firefox-android-arm/`, `obj-firefox-android-x86_64/` |
| Logs, screenshots, and recordings | `artifacts/` |

These paths are ignored by Git.

## Bootstrap

On Windows, download MozillaBuild from Mozilla's official distribution and extract it to
`.toolchain/mozilla-build/`. This location is ignored by Git and is the only host-level prerequisite
used by the wrapper.

From PowerShell at the repository root:

```powershell
.\tools\fenix\mach-local.ps1 bootstrap
```

The wrapper then directs bootstrap downloads into `.mozbuild/`. The checked-in mozconfig selects
`mobile/android`, targets `aarch64`, and writes the object directory inside the checkout.

## Searchfox CLI

Searchfox CLI is repository-local and must be invoked through
`tools/fenix/searchfox-local.ps1`. Do not rely on a global executable or add it to `PATH`.

Install it once from the repository root:

```powershell
cargo install --root .\searchfox-cli searchfox-cli
.\tools\fenix\searchfox-local.ps1 --help
```

The ignored installation lives under `searchfox-cli/`, and the wrapper stores cache data under
`.mozbuild/searchfox-cache`. See [tools/fenix/README.md](tools/fenix/README.md) for the canonical
command reference and examples. Root-level `AGENTS.md` directs future development sessions to the
same wrapper.

## Build and test

```powershell
# Debug APKs
.\tools\fenix\mach-local.ps1 gradle fenix:assembleDebug

# Fenix unit tests
.\tools\fenix\mach-local.ps1 gradle fenix:testDebugUnitTest

# Kotlin formatting and lint
.\tools\fenix\mach-local.ps1 gradle fenix:ktlintFormat fenix:ktlint

# Arm64 multi-locale release APK using the official upstream GeckoView package
.\tools\fenix\build-release-local.ps1 -UseUpstreamGecko -Abi arm64-v8a
```

Release builds must use `build-release-local.ps1`. By default, and for every Fenix-only change, it
downloads the pinned official multi-locale GeckoView package for each selected ABI, verifies the
baseline, locale set, native libraries, versionCode, signature, and checksum, then assembles and
signs Fenix. Running `fenix:assembleRelease` directly is not a release workflow because it can stage
an incomplete single-locale GeckoView package.

### GeckoView source policy

The release workflow must never compile or package GeckoView locally for Fenix-only changes. Apply
this order:

1. Reuse existing signed APKs when they were built from the intended source changes and already pass
   version, application ID, ABI, locale, signature, and checksum validation.
2. Re-sign an existing validated unsigned APK when only the signing output needs to change.
3. For Fenix-only Kotlin, resource, or manifest changes, assemble with `-UseUpstreamGecko` and keep
   the verified upstream GeckoView package.
4. Retry only the failed or invalid ABI with `-UseUpstreamGecko -Abi`; preserve completed artifacts.
5. Use `-BuildLocalGecko` only after a GeckoView, Gecko, C++, Rust, or Gecko locale source change.

Creating a commit or annotated release tag after a successful build does not by itself invalidate
the APKs, provided the exact intended source changes were present during the build. Do not rebuild
solely to align commit or tag metadata unless a release contract explicitly requires that metadata
inside the binaries. For Fenix-only changes, a local GeckoView package is never a valid fallback.

Local GeckoView packaging is required only after Fenix-authored changes to GeckoView, Gecko, C++,
Rust, or Gecko locale sources. Updating to an official upstream baseline uses that baseline's pinned
official package. Use `-BuildLocalGecko` explicitly only for the source-change exception. For all
other changes, use the pinned upstream package and treat any local GeckoView package as invalid
release input.

Debug and test Gradle tasks can replace an object directory's staged GeckoView package with an
`en-US`-only package. This does not invalidate previously verified signed APKs. Always restore the
official package with `-UseUpstreamGecko` before assembling a Fenix-only release; never repair it by
silently compiling Gecko locally.

To retry one architecture while preserving completed APKs, pass its ABI to the release script:

```powershell
.\tools\fenix\build-release-local.ps1 -UseUpstreamGecko -Abi x86_64
```

For Fenix-only Kotlin, resource, or manifest changes, use the pinned upstream package:

```powershell
.\tools\fenix\build-release-local.ps1 -UseUpstreamGecko -Abi arm64-v8a
```

Omit `-Abi` to produce all supported APKs. The script downloads the official multi-locale package,
verifies its provenance and contents, and assembles the Android application. A custom source may be
passed with `-UpstreamGeckoApk`, but it must pass the same checks; missing or ambiguous provenance is
a hard failure. To package a changed GeckoView locally, use `-BuildLocalGecko` and document the
native source change; do not use that switch for Fenix-only changes.

Signed filenames use the baseline from `FENIX_UPSTREAM_RELEASE`. When the current commit already has
a matching `fenix-<version>-rN` tag, that revision is reused. Otherwise the script selects the next
revision after the highest matching local or `origin` tag. For example, a new untagged commit after
`fenix-154.0.1-r4` produces `Fenix-154.0.1-r5-arm64-v8a-release.apk`. Use `-VersionName` to rebuild a
specific release revision.

Release APKs must use the exact versionCode from the corresponding official upstream APK for the
same baseline and ABI. The checked-in `FENIX_UPSTREAM_VERSION_CODES.json` records those values;
update it from the official Mozilla archive when changing `FENIX_UPSTREAM_RELEASE`. The release
script passes the recorded value to Gradle and verifies the resulting APK manifest, so a build-time
clock value cannot silently become the release versionCode. For the 155.0.1 baseline, the official
arm64-v8a value is `2016182530`.

Do not add a fork revision offset: a fork build is a modified build of that upstream versionCode,
and changing it would prevent normal downgrade or replacement workflows.

The `rN` suffix is fork release metadata, not an Android package version. The release script passes
it to `BuildConfig.FENIX_RELEASE_REVISION` only so the About Fenix screen can show the revision. It
must not affect `versionCode` or the upstream `versionName`.

### Release completion and cache retention

Publish a release only after the source commit and annotated tag are ready and every selected ABI
has passed application ID, version, ABI, locale, native-library, signature, and checksum validation.
After upload, read the GitHub Release back and confirm that it is not a draft, is marked as the latest
release, and that every remote asset name, size, and SHA-256 digest matches the local file. Only then
may superseded GitHub Releases and tags for the same baseline be removed.

Every GitHub Release body must be bilingual, with a complete Chinese section first and a complete
English section second. Use the headings `## 中文` and `## English`, and keep the version metadata,
change summary, validation results, supported ABI, and `.idsig` publication policy equivalent in
both sections. Do not publish release notes that contain only one language.

GitHub Release history uses a per-baseline latest-revision retention policy. For each upstream
baseline, keep only the highest successfully published `fenix-<baseline>-rN` GitHub Release and its
matching tag. When publishing a new revision for one baseline, verify the new release first, then
remove older releases and tags for that same baseline; never remove the retained latest release of
another baseline. This remote-release policy does not require deleting local APKs, `.idsig` files,
release notes, logs, or cross-release build caches; retain those locally according to reuse and
verification needs.

If an ABI fails, retry only that ABI and retain already completed current-revision APKs. A transient
Android Lint internal failure is not a reason to rebuild Gecko or other ABIs; rerun the affected ABI
first and require the normal lint, packaging, and signature checks to pass on the successful attempt.

For a narrow test class, append Gradle's test selector:

```powershell
.\tools\fenix\mach-local.ps1 gradle fenix:testDebugUnitTest `
    --tests org.mozilla.fenix.tabgroups.ExpandedTabGroupTest
```

Slow command output should be redirected to `artifacts/` and inspected there instead of piping the
live process through output filters.

### Windows native-test limitation in the 154.0.1, 155.0, and 155.0.1 baselines

Some Fenix JVM test classes use `FenixGleanTestRule`, which loads Application Services through JNA.
The upstream `full-megazord-libsForTests-154.0.1.jar` contains Linux and macOS megazord libraries but
does not contain the required Windows native libraries. On native Windows, these classes fail during
test-rule initialization with `UnsatisfiedLinkError` for `jnidispatch.dll`; their test bodies have not
started at that point. The same limitation was observed when validating the 155.0 and 155.0.1
baselines.

Do not repeatedly clear Gradle caches or download only `jnidispatch.dll`: JNA is merely the first
missing layer, and the Windows megazord is absent as well. Run affected Glean-backed unit tests in a
Linux environment or CI. On native Windows, `FenixGleanTestRule` marks these tests skipped before
their test bodies run, so the full Gradle task can complete without turning the missing native
artifact into a product failure; skipped tests still need Linux/CI coverage. Unaffected unit tests,
lint, APK builds, emulator tests, and visual checks continue to run on Windows. Recheck this
limitation after changing `FENIX_UPSTREAM_RELEASE`, because a newer official baseline may provide
complete host-native test artifacts.

## Local signing

Signing files are local-only and must never be committed:

- `MAGI-OpenSource.jks`
- `signing-local.properties`

The properties file has this shape:

```properties
storePassword=replace-with-local-value
keyPassword=replace-with-local-value
```

`build-release-local.ps1` signs selected APKs automatically. To re-sign an existing unsigned APK
without rebuilding it, run:

```powershell
.\tools\fenix\sign-release-local.ps1 -Abi arm64-v8a `
    -VersionName 154.0.1-r5 `
    -KeyStorePath .\MAGI-OpenSource.jks `
    -KeyAlias magi-opensource
```

The script reads passwords from the ignored properties file and passes them to `apksigner` through
process environment variables. It does not print passwords. Unsigned APKs are written under
`artifacts/fenix-release/unsigned/`; verified signed APKs are written under
`artifacts/fenix-release/signed/`.

## Before committing

```powershell
git diff --check
git status --short --untracked-files=all
```

Confirm that no key stores, password files, APKs, SDKs, AVDs, logs, or generated object directories
appear in the staged changes.
