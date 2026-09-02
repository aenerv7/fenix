# Fenix changes

## 155.0-r4

### 中文

上游基线：`FIREFOX-ANDROID_155_RELEASE`

#### 标签页群组

- 移除我们后来添加的、与上游功能重复的群组内浮动新建标签页按钮，保留顶部工具栏中上游提供的新建标签页按钮。
- 将群组内标签页长按后的选择工具栏宽度与群组视图内容对齐，避免工具栏铺满整个窗口。

#### 发布与验证

- 仅发布 `arm64-v8a` 架构 APK。
- 严格沿用上游 arm64-v8a 基线 `versionCode 2016180970`，不因 r4 修订改变。
- `.idsig` 仅保留本地用于校验，不作为 GitHub Release 资产发布。

### English

Upstream baseline: `FIREFOX-ANDROID_155_RELEASE`

#### Tab groups

- Removed the fork-added floating new-tab button from the expanded group because it duplicated the upstream action; the upstream new-tab button in the top toolbar remains available.
- Matched the long-press selection toolbar width to the expanded group's content width instead of the full window.

#### Release and validation

- Published the `arm64-v8a` APK only.
- Keeps the upstream arm64-v8a baseline `versionCode 2016180970`; the r4 revision never changes it.
- `.idsig` is retained locally for verification and is not published as a GitHub Release asset.

## 155.0-r3

### 中文

上游基线：`FIREFOX-ANDROID_155_RELEASE`

#### 实验室徽标

- 使用独立的透明背景兔子资源，并按横幅原有尺寸显示，避免回退到 Firefox 徽标或被布局裁切。

#### 标签页群组

- 展开群组中的新建标签页按钮不再依赖“主页作为新标签页”实验开关。
- 保持与“全部标签页”一致的浮动按钮样式、选择模式隐藏逻辑和底部滚动空间。

#### 版本信息

- 在“关于 Fenix”的上游版本信息后显示“当前版本第 3 次修改”。
- 修订号使用 Android 本地化资源，不改变上游基线的 versionName 或 versionCode。

#### 验证

- `ExpandedTabGroupTest` 定向测试通过。
- APK 使用已验证的多语言 GeckoView 构建，并完成 arm64-v8a、签名和资源校验。
- 严格沿用上游基线对应 ABI 的 Android versionCode（arm64-v8a 为 `2016180970`），确保 fork 包仍可按正常方式覆盖或降回对应上游版本。

### English

Upstream baseline: `FIREFOX-ANDROID_155_RELEASE`

#### Labs badge

- Uses a dedicated transparent-background rabbit resource at the original banner dimensions, preventing fallback to the Firefox badge or layout clipping.

#### Tab groups

- The expanded group new-tab action no longer depends on the “homepage as new tab” experiment switch.
- Preserved the All Tabs floating-button style, selection-mode visibility logic, and bottom scroll space.

#### Version information

- Shows “Current version modification #3” below the upstream version line on the About Fenix screen.
- Uses Android localized resources for the revision without changing the upstream baseline versionName or versionCode.

#### Validation

- The focused `ExpandedTabGroupTest` suite passed.
- The APK reuses the validated multi-locale GeckoView build and passed arm64-v8a, signature, and resource checks.
- Keeps the upstream baseline ABI-specific Android versionCode (`2016180970` for arm64-v8a) so the fork remains replaceable and can be normally downgraded to the corresponding upstream build.

## 155.0-r2

### 中文

上游基线：`FIREFOX-ANDROID_155_RELEASE`

#### 品牌

- 将 Fenix 实验室横幅徽标替换为透明背景的 Fenix 兔子徽标。

#### 标签页群组

- 在展开的标签页群组视图中使用与“全部标签页”相同的控件增加新建标签页浮动操作。
- 保持选择模式下隐藏操作，并预留底部滚动空间，确保最后一个标签页完全可见。

#### 验证

- 定向 `ExpandedTabGroupTest` 测试套件通过，覆盖新建标签页激活、选择模式可见性和列表底部遮挡检查。
- Debug 单元测试编译以及 Release Kotlin/Java 编译通过。

### English

Upstream baseline: `FIREFOX-ANDROID_155_RELEASE`

#### Branding

- Replaced the Firefox Labs banner badge with the backgroundless Fenix rabbit mark.

#### Tab groups

- Added a floating new-tab action to the expanded tab-group view using the same control as All Tabs.
- Kept the action hidden during tab selection mode and reserved bottom scroll space so the last tab
  remains fully visible.

#### Validation

- The focused `ExpandedTabGroupTest` suite passed, including new-tab activation, selection-mode
  visibility, and bottom-of-list overlap coverage.
- Debug unit-test compilation and release Kotlin/Java compilation passed.

## 154.0.1-r8

Upstream baseline: `FIREFOX-ANDROID_154_0_1_RELEASE`

### Tab groups

- Removed the opening animation when the selected tab's group is opened automatically from All Tabs.
- Preserved the opening animation when a user opens a tab group directly.

## 154.0.1-r7

Upstream baseline: `FIREFOX-ANDROID_154_0_1_RELEASE`

### Tab groups

- Opened the selected tab's group automatically when entering All Tabs and scrolled the expanded
  group to the selected tab.
- Added reducer and Compose regression coverage for the automatic navigation and nested scroll
  position.

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
