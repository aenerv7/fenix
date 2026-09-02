# Fenix development

## Scope

Fenix is maintained as a focused Android fork. Use `fenix:*` Gradle tasks and do not build Focus.
Mozilla's general Firefox source documentation remains authoritative for the rest of the tree.

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

# Multi-locale release APKs, without Focus
.\tools\fenix\build-release-local.ps1
```

Release builds must use `build-release-local.ps1`. It creates separate Gecko builds for arm64-v8a,
armeabi-v7a, and x86_64, reads the upstream Android locale list, packages the matching Gecko locales
into GeckoView, and assembles Fenix without regenerating the multi-locale omnijar. It then verifies
the complete locale set and matching Gecko native libraries and signs every selected ABI APK. Running
`fenix:assembleRelease` directly produces an `en-US`-only GeckoView even though the Android UI
resources remain multilingual, and a single Gecko target cannot supply valid native libraries for
the other ABI APKs.

### Reuse-first release policy

A full multi-ABI Gecko and Fenix release is expensive. Reuse existing validated output by default,
and use the least expensive operation that can produce a correct release. Apply this order:

1. Reuse existing signed APKs when they were built from the intended source changes and already pass
   version, application ID, ABI, locale, signature, and checksum validation.
2. Re-sign an existing validated unsigned APK when only the signing output needs to change.
3. For Fenix-only Kotlin, resource, or manifest changes, rebuild the application with `-ReuseGecko`
   and keep the verified multi-locale Gecko packages.
4. Retry only the failed or invalid ABI with `-Abi`; preserve completed ABI artifacts.
5. Run the default full build only when a broader option above cannot produce a valid artifact.

Creating a commit or annotated release tag after a successful build does not by itself invalidate
the APKs, provided the exact intended source changes were present during the build. Do not rebuild
solely to align commit or tag metadata unless a release contract explicitly requires that metadata
inside the binaries. Before starting a full build while reusable artifacts exist, identify the input
that makes reuse unsafe. If no such input exists, reuse the artifacts or ask for explicit approval to
rebuild.

A full build is required after changes to Gecko, C++, Rust, Gecko locale sources, the upstream
baseline, or another build input that affects the native or multi-locale package. It is also required
when the relevant cache or artifacts are missing, fail validation, or have uncertain provenance.
Validation failure is a reason to rebuild only the affected layer or ABI, not automatically every
artifact.

Debug and test Gradle tasks can replace an object directory's staged GeckoView package with an
`en-US`-only package. This does not invalidate previously verified signed APKs. The release script's
`-ReuseGecko` validation inspects `res/multilocale.txt` inside the staged `omni.ja`, not only whether
the file exists. If the cached package is incomplete, the script automatically rebuilds the Gecko
package and its complete locale set before assembling Fenix; with `-SkipBuild`, it fails explicitly
instead of producing a single-language APK. Do not discard valid signed artifacts merely because a
later test changed intermediate build state.

To retry one architecture while preserving completed APKs, pass its ABI to the release script:

```powershell
.\tools\fenix\build-release-local.ps1 -Abi x86_64
```

For Fenix-only Kotlin, resource, or manifest changes after a successful full release build, reuse
the cached Gecko packages:

```powershell
.\tools\fenix\build-release-local.ps1 -ReuseGecko -Abi arm64-v8a
```

Omit `-Abi` to rebuild all supported APKs. This path verifies the cached Gecko libraries and exact
multi-locale package, repairs an invalid cached package automatically, and then rebuilds the Android
application. Do not use it after changing Gecko, C++, Rust, or Gecko locale sources; use the default
full build in that case.

Signed filenames use the baseline from `FENIX_UPSTREAM_RELEASE`. When the current commit already has
a matching `fenix-<version>-rN` tag, that revision is reused. Otherwise the script selects the next
revision after the highest matching local or `origin` tag. For example, a new untagged commit after
`fenix-154.0.1-r4` produces `Fenix-154.0.1-r5-arm64-v8a-release.apk`. Use `-VersionName` to rebuild a
specific release revision.

Release APKs must use the exact versionCode from the corresponding official upstream APK for the
same baseline and ABI. The checked-in `FENIX_UPSTREAM_VERSION_CODES.json` records those values;
update it from the official Mozilla archive when changing `FENIX_UPSTREAM_RELEASE`. The release
script passes the recorded value to Gradle and verifies the resulting APK manifest, so a build-time
clock value cannot silently become the release versionCode. For the 155.0 baseline, the official
values are arm64-v8a `2016180970`, armeabi-v7a `2016180968`, and x86_64 `2016180974`.

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

### Windows native-test limitation in the 154.0.1 and 155.0 baselines

Some Fenix JVM test classes use `FenixGleanTestRule`, which loads Application Services through JNA.
The upstream `full-megazord-libsForTests-154.0.1.jar` contains Linux and macOS megazord libraries but
does not contain the required Windows native libraries. On native Windows, these classes fail during
test-rule initialization with `UnsatisfiedLinkError` for `jnidispatch.dll`; their test bodies have not
started at that point. The same limitation was observed when validating the 155.0 baseline.

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
