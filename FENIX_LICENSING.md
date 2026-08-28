# Licensing and release policy

This document is an engineering checklist, not legal advice. The license text and third-party notices
in the source tree control if they differ from this summary.

## Source code

- Preserve existing copyright, license, and attribution notices.
- Treat modifications to MPL-covered files as MPL 2.0-covered source.
- Add the MPL 2.0 header to substantive new source files placed in MPL-covered modules.
- Respect the separate licenses of vendored and third-party components.
- Keep `LICENSE` and `toolkit/content/license.html` available with source distributions.

## Executable distribution

When distributing an APK outside a private environment:

- Make the corresponding MPL-covered source, including modifications, available by reasonable and
  timely means.
- Identify the exact source commit or Fenix release tag in the binary release notes.
- Do not impose terms that restrict recipients' MPL rights.
- State clearly that any warranty or support is offered by the fork maintainer, not Mozilla.

## Mozilla trademarks

MPL 2.0 grants source-code rights, not trademark rights. A modified build must have an independent
identity and must not imply Mozilla sponsorship, affiliation, or approval.

Before publishing an APK:

- Use the Fenix application name and `github.aenerv7.fenix` application ID.
- Replace Firefox and Mozilla logos, launcher icons, promotional graphics, and other user-facing brand
  assets with independently owned assets.
- Review strings and store metadata so “Firefox” and “Mozilla” appear only where factually necessary,
  such as attribution or compatibility statements.
- Do not call the modified binary Firefox or present it as an official Mozilla release.
- Include a visible non-affiliation statement and trademark attribution.

The upstream source tree necessarily contains Mozilla brand assets. Their presence in source does not
grant permission to brand a modified public binary with them. Until the branding audit above is
complete, builds are for private testing and must not be attached to public GitHub releases.

## Secrets and generated artifacts

Never commit or upload:

- Java key stores, signing certificates with private keys, or passwords;
- `signing-local.properties`;
- APKs or app bundles unless a deliberate, license-compliant release is being made;
- local SDKs, NDKs, JDKs, AVDs, caches, object directories, logs, recordings, or screenshots containing
  private data.

## Authoritative references

- <https://www.mozilla.org/MPL/2.0/>
- <https://www.mozilla.org/MPL/2.0/FAQ/>
- <https://www.mozilla.org/foundation/trademarks/policy/>
- <https://www.mozilla.org/foundation/trademarks/distribution-policy/>
- <https://github.com/mozilla-firefox/firefox>
