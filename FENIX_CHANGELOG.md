# Fenix changes

## 154.0.1-r6

Upstream baseline: `FIREFOX-ANDROID_154_0_1_RELEASE`

### Tab groups

- Corrected the selected All Tabs item index to point to the containing group card instead of the
  tab's former standalone position when grouped tabs are not contiguous in storage order.
- Strengthened data transformation and Compose regression coverage for moving a selected tab from
  the end of the list into an earlier group.

## 154.0.1-r5

Upstream baseline: `FIREFOX-ANDROID_154_0_1_RELEASE`

### Tab groups

- Kept the selected tab group visible in All Tabs after moving a selected standalone tab into that
  group instead of retaining the tab's previous standalone scroll position.
- Added Compose regression coverage for scrolling the newly selected group into view.

## 154.0.1-r4

Upstream baseline: `FIREFOX-ANDROID_154_0_1_RELEASE`

### First-run experience

- Skipped the initial onboarding flow for new installations so Fenix starts with the default
  configuration.
- Replaced the Firefox splash-screen artwork with the Fenix rabbit launcher artwork.
- Added unit coverage for the default completed onboarding state.

## 154.0.1-r3

Upstream baseline: `FIREFOX-ANDROID_154_0_1_RELEASE`

### Crash fix

- Fixed the dark-theme startup crash caused by passing an Android inset drawable to Compose's
  `painterResource` API for the homepage wordmark.
- Kept the same white-rabbit `Fenix` wordmark in light, dark, normal, and private contexts through a
  drawable resource alias that Compose supports.

### Branding

- Added a dedicated Gecko `Fenix` brand package with rabbit About artwork and favicons.
- Replaced remaining user-facing `Firefox` product-name text with `Fenix` across all packaged Android
  locales while preserving upstream-source, licensing, and compatibility references.
- Preserved the localized About attribution that credits aenerv7@GitHub, Mozilla, and the upstream
  Firefox source while using `Fenix` for the app itself.

### Release packaging

- Built arm64-v8a, armeabi-v7a, and x86_64 releases from separate Gecko target object directories.
- Added release checks that require the full Gecko locale set plus matching `libmozglue.so` and
  `libxul.so` native libraries in every ABI APK before signing.
- Added post-signature verification and separated unsigned and signed release artifacts.

## 154.0.1-r2

Upstream baseline: `FIREFOX-ANDROID_154_0_1_RELEASE`

### Branding and packaging

- Replaced the release launcher icon with the centered white Dwarf Hotot rabbit branding.
- Restored the new-tab homepage and About page wordmarks with the rabbit logo and `Fenix` name.
- Replaced the About page's Firefox attribution with localized fork attribution.

### App-store integration

- Removed the settings entry for rating the app on Google Play.
- Removed automatic review prompts, Play Review SDK integration, store fallbacks, and rating
  telemetry.

### Tab groups

- Kept tabs opened with the configurable toolbar shortcut in the current group, while leaving other
  new-tab buttons ungrouped.
- Kept URL and search submissions in tabs created by the grouped new-tab shortcut.
- Kept homepage URL and search submissions in the current tab so grouped shortcuts retain their
  parent tab.

## 154.0.1-r1

Upstream baseline: `FIREFOX-ANDROID_154_0_1_RELEASE`

### Branding and packaging

- Changed the Android application ID to `github.aenerv7.fenix`.
- Changed the user-facing application name to `Fenix`.
- Removed the upstream `-default` suffix from the displayed version.
- Kept Focus outside the supported build and release scope.

### Simplified Chinese localization

- Filled missing `zh-rCN` resources used by the stable Android build.
- Added localized tab-group actions and labels.
- Displayed the mobile bookmarks root as “移动收藏夹”.
- Packaged the full official Android Gecko locale set into release APKs so WebExtensions and Gecko
  UI use the selected application language instead of falling back to `en-US`.

### Passwords, personal information, and autofill

- Removed password and personal-information management entry points from the UI.
- Removed the Android autofill service and its configuration activities.
- Removed password, address, and credit-card data from the default sync scope.
- Removed related shortcuts, intents, background initialization, and settings indexing.

### Tab groups

- Kept links opened by a grouped tab in that tab group by default.
- Added context-menu actions for opening a link in the current group or creating a new group.
- Scoped group selection mode to tabs in the expanded group.
- Added group multi-select actions, including “Remove from group”.
- Restored group membership when undoing partial tab deletion.
- Restored an emptied tab group when undoing deletion of all its tabs.
- Fixed the group selection toolbar layering, navigation-bar insets, light/dark colors, dismissal,
  and empty-group behavior.
- Positioned the overflow menu above the toolbar and matched the Material dropdown direction and
  timing while keeping the toolbar stationary.

### Validation performed

- Fenix debug and release variants compile successfully.
- Relevant tab-group unit and Compose tests pass. On Windows, 193 selected JVM tests passed; the 95
  tests in `DefaultTabManagerControllerTest` require the Windows native Application Services test
  libraries that are absent from the upstream 154.0.1 artifact, as documented in
  `FENIX_DEVELOPMENT.md`.
- Debug builds were installed and visually checked with gesture and three-button navigation.
- Release APKs were locally signed and checked for package name, version, ABI, and signature.

Release APKs and signing material are intentionally not stored in this source repository.
