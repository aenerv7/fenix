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
