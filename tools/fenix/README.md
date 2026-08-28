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
.\tools\fenix\build-release-local.ps1
```

Use `build-release-local.ps1` for release APKs. It follows Gecko's multi-locale packaging flow and
verifies that the official Android Gecko locales are present in every output APK.

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
