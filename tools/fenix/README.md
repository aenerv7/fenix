# Fenix repository tools

Run every command in this document from the repository root in PowerShell. These wrappers keep
developer state inside the checkout and are the supported entry points for this fork.

## Searchfox CLI

Searchfox is the primary way to investigate upstream Firefox code. This repository does not install
it globally. Install the executable into the ignored `searchfox-cli/` directory:

```powershell
cargo install --root .\searchfox-cli searchfox-cli
```

Always invoke it through the checked-in wrapper:

```powershell
.\tools\fenix\searchfox-local.ps1 --help
.\tools\fenix\searchfox-local.ps1 --define 'AudioContext::AudioContext'
.\tools\fenix\searchfox-local.ps1 --define 'AudioSink'
.\tools\fenix\searchfox-local.ps1 --path 'mobile/android/*' -q 'TabsTray'
.\tools\fenix\searchfox-local.ps1 --id AudioSink -l 150 --cpp
```

The wrapper locates `searchfox-cli/bin/searchfox-cli.exe` and places its cache under
`.mozbuild/searchfox-cache`. Both locations stay inside the repository and are ignored by Git.

Use Searchfox identifier queries for C++, Rust, and Java. Use `--path` for text searches and keep the
path as narrow as practical. Use local `rg` only when looking for changes known to exist solely in
the current checkout. `--path` accepts one argument, but that argument may contain a glob.

Root-level `AGENTS.md` carries the same wrapper requirement so a newly started development session
can discover the command without relying on prior conversation context.

## Firefox Android build wrapper

`mach-local.ps1` redirects Mozilla toolchains, Gradle, Rust, Android SDK and AVD data, temporary
files, and build output into ignored repository paths:

```powershell
.\tools\fenix\mach-local.ps1 bootstrap
.\tools\fenix\mach-local.ps1 gradle fenix:assembleDebug
.\tools\fenix\build-release-local.ps1 -UseUpstreamGecko -Abi arm64-v8a
```

Use `build-release-local.ps1` for release APKs. For Fenix-only changes it imports the pinned official
multi-locale GeckoView package for each selected ABI, verifies the package, and signs the assembled
Fenix artifact. The signed filename uses the current baseline and release revision, such as
`Fenix-154.0.1-r5-arm64-v8a-release.apk`.
The script reads the exact per-ABI upstream versionCode from `FENIX_UPSTREAM_VERSION_CODES.json`
and validates that value in each generated APK.
Pass `-Abi x86_64` (or another ABI) to resume a single failed architecture without rebuilding the
completed APKs.

Release work must never compile GeckoView for Fenix-only changes. Prefer, in order: already validated
signed APKs, re-signing validated unsigned APKs, assembling with `-UseUpstreamGecko`, and retrying
only the affected ABI. A commit or annotated tag created after a successful build is not by itself a
reason to rebuild when the intended source changes were already present. See the GeckoView source
policy in [FENIX_DEVELOPMENT.md](../../FENIX_DEVELOPMENT.md).

For Fenix Kotlin, resource, or manifest-only changes, use the pinned upstream package:

```powershell
.\tools\fenix\build-release-local.ps1 -UseUpstreamGecko -Abi arm64-v8a
```

Omit `-Abi` to produce all supported APKs. The downloaded APK is cached under
`.tmp\upstream-geckoview\` and is never published. A custom source can be supplied with
`-UpstreamGeckoApk`; it must pass the same exact checks. A Maven AAR or APK without the complete
multi-locale package is rejected.

Only when GeckoView, Gecko, C++, Rust, or Gecko locale sources changed may you use
`-BuildLocalGecko`. Document that native source change in the release notes; this switch is a hard
exception and must not be used for Fenix-only changes.

When the current commit already has a matching `fenix-<version>-rN` tag, the build reuses that
revision. Otherwise it selects the next revision after the highest matching local or `origin` tag.
Pass `-VersionName <version>-rN` to rebuild a specific release. Use `sign-release-local.ps1 -Abi`
only when re-signing an existing unsigned APK without rebuilding it.

After publishing, verify the GitHub Release and its remote asset sizes and SHA-256 digests before
removing anything. GitHub Release history uses per-baseline latest-revision retention: keep only the
highest successfully published revision and matching tag for each upstream baseline. Remove older
releases and tags only for the baseline just verified. Because that leaves one retained release per
baseline, its bilingual notes must describe every effective user-facing Fenix difference from the
exact official baseline, not only the latest `rN` delta, and must omit intermediate behavior that was
later reverted. This remote-release policy does not require deleting local APKs, `.idsig` files,
notes, logs, or cross-release build caches. The full sequencing and validation rules are documented in
[FENIX_DEVELOPMENT.md](../../FENIX_DEVELOPMENT.md).

See [FENIX_DEVELOPMENT.md](../../FENIX_DEVELOPMENT.md) for setup, testing, emulator, and local signing
instructions, including the Firefox Android 154.0.1 Windows/JNA unit-test limitation. Do not treat
that native artifact failure as a reason to clear the repository-local Gradle cache repeatedly.

## Official release synchronization

Prepare a candidate branch against an exact Firefox Android stable release tag:

```powershell
.\tools\fenix\sync-official-release.ps1 -Version 155.0
```

See [FENIX_UPSTREAM_SYNC.md](../../FENIX_UPSTREAM_SYNC.md) before resolving, validating, or promoting
the candidate.
