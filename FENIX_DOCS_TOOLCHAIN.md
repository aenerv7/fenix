# Documentation toolchain compatibility

## Scope

Firefox source documentation is an upstream subsystem and is maintained separately from Fenix
Android product changes and releases. A documentation build failure does not invalidate an APK when
the failure is outside `mobile/android/fenix`, but it must be classified and recorded before the
next upstream synchronization.

This repository currently inherits these pinned upstream components:

- Sphinx 7.4.7;
- sphinx-js 4.0.0;
- JSDoc 4.0.5;
- MyST-Parser 2.0.

Do not upgrade one component in isolation. Mozilla tracks the coordinated Sphinx, MyST, theme, and
sphinx-js upgrade in [Bug 2008577](https://bugzilla.mozilla.org/show_bug.cgi?id=2008577). Prefer
upstream fixes or a focused backport from that work over a fork-only parser or broad warning
suppression.

## Validation

Use the normal autodoc-enabled command. `--no-autodoc` removes the extensions that define the
`js:autoclass` and `js:autofunction` directives and therefore cannot validate the full tree.

For a quick Fenix-only check:

```powershell
.\tools\fenix\mach-local.ps1 doc mobile/android/fenix --no-serve --no-open
```

After every upstream baseline update, build the complete tree into a new ignored output directory:

```powershell
$docsOut = "artifacts/docs-full-$((Get-Date).ToUniversalTime().ToString('yyyyMMdd-HHmmss'))"
.\tools\fenix\mach-local.ps1 doc docs --no-serve --no-open --outdir $docsOut
```

A new directory matters because moztreedocs deliberately preserves unaccounted staging files for
live reload. Reusing an output directory across upstream layout changes can leave removed pages in
`html/_staging` and produce false duplicate-label failures. Do not clear all of `artifacts/`; use a
new output path, or remove only a confirmed generated documentation directory.

The command succeeds only when Sphinx returns zero and the warning checker reports `Failures: 0`.
Entries reported as `Known Failures` match the upstream allowlist in `docs/config.yml`; do not add a
new pattern merely to make the count green.

### 157.0 baseline run

`mach doc docs --no-serve --no-open --outdir artifacts/docs-full-<timestamp>` completes the build but
reports `Failures: 2`, both outside `mobile/android/fenix`:

```text
testing/marionette/client/marionette_driver/addons.py:docstring of marionette_driver.addons.Addons:1:
  ERROR: Unknown target name: "addonmanager api". [docutils]
testing/marionette/client/marionette_driver/localization.py:docstring of marionette_driver.localization.L10n:3:
  ERROR: Unknown target name: "localization". [docutils]
```

They are upstream Python docstring cross-references, not a Fenix product regression, and they are not
added to the allowlist. The first 157.0 attempt failed earlier with
`sphinx.errors.SphinxError: node.cmd was not found. Install it using "npm install -g jsdoc"`: sphinx-js
looks up `node` on `PATH` under the name `node.cmd`, and the repository-local toolchain only ships
`node.exe`. A local `.mozbuild/node/node.cmd` shim that forwards to `node.exe` fixes the host tooling
gap; `.mozbuild/` is ignored by Git, so it must be recreated on a fresh checkout before running the
documentation gate.

### 157.0.1 baseline run

`mach doc docs --no-serve --no-open --outdir artifacts/docs-full-<timestamp>` completes the build and
reports `Failures: 0` with `Known Failures: 295` (`build succeeded, 295 warnings`). The two Marionette
docstring cross-reference failures recorded for the 157.0 baseline no longer appear. Every remaining
diagnostic matches the upstream `docs/config.yml` allowlist, including the JSDoc type-expression
diagnostics and the docutils substitution, indentation, and cross-reference warnings, so no new
allowlist entry was added. The local `.mozbuild/node/node.cmd` shim described above was still
required.

### 158.0 baseline run

`mach doc docs --no-serve --no-open --outdir artifacts/docs-full-20261013-101112` completes with
`Failures: 0` and `Known Failures: 37`. The two marionette docstring cross-reference errors that were
unaccounted for at 157.0 are gone, so no new pattern was added to `docs/config.yml`. The remaining
diagnostics are the recorded categories: JSDoc relaxed-mode lexing of TypeScript-style expressions,
MyST cross-reference mismatches in the generated JS and NSS pages, and python/taskgraph reference
ambiguities. The repository-local `.mozbuild/node/node.cmd` shim was already present.

### 158.0.1 baseline run

`mach doc docs --no-serve --no-open --outdir artifacts/docs-full-20261020-160653` completes with
`Failures: 0` and `Known Failures: 37`, the same count and diagnostic categories as the 158.0 run, so
no new pattern was added to `docs/config.yml`.

### 158.0.2 baseline run

`mach doc docs --no-serve --no-open --outdir artifacts/docs-full-20261021-095503` completes with
`Failures: 0` and `Known Failures: 37`, again the same count and diagnostic categories as the 158.0 and
158.0.1 runs, so no new pattern was added to `docs/config.yml`.

### 159.0 baseline run

`mach doc docs --no-serve --no-open --outdir artifacts/docs-full-20261027-230453` completes with
`Failures: 0` and `Known Failures: 37`, unchanged across the 158.0 series, so no new pattern was added
to `docs/config.yml`.

## JSDoc compatibility debt

The Firefox Android 155.0 tree emits JSDoc diagnostics for TypeScript-style type expressions such as
tuple types, index signatures, arrow function types, `Values<typeof ...>`, `Parameters<...>`,
intersection types, and `import("...").Type`. JSDoc writes these diagnostics to standard error while
Sphinx 7 can still generate HTML. They are not Fenix product regressions, but they are upgrade debt:
the upstream Sphinx 9 work converts these annotations to forms accepted by the coordinated
sphinx-js/JSDoc pipeline.

At each baseline update:

1. Run the full clean-output build and retain its log under `artifacts/`.
2. Compare the diagnostic categories with the previous baseline.
3. Backport an upstream annotation fix only when the affected documentation is needed before the
   next baseline; keep the product type meaning as precise as the standard JSDoc syntax permits.
4. Never hide every `Unable to parse a tag's type expression` message. New categories may indicate a
   real documentation regression or a dependency mismatch.
5. Re-evaluate all local compatibility code when the upstream dependency pins change, and remove a
   workaround once the corresponding upstream fix is present.

## Windows staging compatibility

`docs/config.yml` stores repository paths with forward slashes. The moztreedocs staging filter must
compare those patterns with a repository-relative, separator-normalized source path. Comparing an
absolute native Windows path directly causes exclusions such as `js/src/doc/Debugger` to be ignored,
which publishes the Debugger reference twice and fails the duplicate-label gate.

The path behavior is covered by `tools/moztreedocs/test/test_path_exclusions.py`. Run it with:

```powershell
.\tools\fenix\mach-local.ps1 python-test tools/moztreedocs/test
```
