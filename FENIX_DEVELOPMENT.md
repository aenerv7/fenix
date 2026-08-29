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

To retry one architecture while preserving completed APKs, pass its ABI to the release script:

```powershell
.\tools\fenix\build-release-local.ps1 -Abi x86_64
```

For Fenix-only Kotlin, resource, or manifest changes after a successful full release build, reuse
the cached Gecko packages:

```powershell
.\tools\fenix\build-release-local.ps1 -ReuseGecko -Abi arm64-v8a
```

Omit `-Abi` to rebuild all supported APKs. This path verifies the cached Gecko libraries and
multi-locale package, then rebuilds only the Android application. Do not use it after changing
Gecko, C++, Rust, or Gecko locale sources; use the default full build in that case.

Signed filenames use the baseline from `FENIX_UPSTREAM_RELEASE`. When the current commit already has
a matching `fenix-<version>-rN` tag, that revision is reused. Otherwise the script selects the next
revision after the highest matching local or `origin` tag. For example, a new untagged commit after
`fenix-154.0.1-r4` produces `Fenix-154.0.1-r5-arm64-v8a-release.apk`. Use `-VersionName` to rebuild a
specific release revision.

For a narrow test class, append Gradle's test selector:

```powershell
.\tools\fenix\mach-local.ps1 gradle fenix:testDebugUnitTest `
    --tests org.mozilla.fenix.tabgroups.ExpandedTabGroupTest
```

Slow command output should be redirected to `artifacts/` and inspected there instead of piping the
live process through output filters.

### Windows native-test limitation in the 154.0.1 baseline

Some Fenix JVM test classes use `FenixGleanTestRule`, which loads Application Services through JNA.
The upstream `full-megazord-libsForTests-154.0.1.jar` contains Linux and macOS megazord libraries but
does not contain the required Windows native libraries. On native Windows, these classes fail during
test-rule initialization with `UnsatisfiedLinkError` for `jnidispatch.dll`; their test bodies have not
started at that point.

Do not repeatedly clear Gradle caches or download only `jnidispatch.dll`: JNA is merely the first
missing layer, and the Windows megazord is absent as well. Run affected Glean-backed unit tests in a
Linux environment or CI. Unaffected unit tests, lint, APK builds, emulator tests, and visual checks
continue to run on Windows. Recheck this limitation after changing `FENIX_UPSTREAM_RELEASE`, because a
newer official baseline may provide complete host-native test artifacts.

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
