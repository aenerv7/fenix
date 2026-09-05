# Fenix changes

## 155.0.1-r1

### 中文

官方上游基线：`FIREFOX-ANDROID_155_0_1_RELEASE`。本版本同步 Firefox Android 155.0.1
上游修订，并保留 Fenix 相对于该基线的全部有效产品改动。

#### 有效 Fenix 改动

- 使用 `github.aenerv7.fenix` application ID、Fenix 名称和兔子品牌，保留必要的 Mozilla/Firefox 上游与许可证说明；Focus 不在构建范围内。
- 补全简体中文和官方 Android Gecko 多语言资源；关于页、更新链接、搜索组件、启动器和 Fenix Labs 使用 Fenix 品牌。
- 移除密码、地址、信用卡等个人信息管理入口、自动填充服务和默认同步范围；移除 Google Play 评分集成及已结束的 Sports/World Cup 活动。
- 保留 IP Protection 入口，默认完成首次引导，并维持 Fenix 的隐私、商店和设置裁剪策略。
- 群组标签页打开链接默认留在原群组，支持群组范围多选、移出、删除撤销和空群组恢复；群组工具栏、菜单、返回行为和拖拽状态保持正确。
- 从群组标签页进入“全部标签页”时自动展开并定位到当前标签；群组弹层始终跳过半高状态直接全高打开，不再依据窗口尺寸或标签数量计算初始高度。
- 关闭最后一个非群组标签页时清理快照、列表固定项和拖拽状态；新建标签页工具栏和搜索组件在手机、平板、横竖屏保持可用。

#### 发布与验证

- 仅发布 `arm64-v8a` APK，使用官方 155.0.1 多语言 GeckoView，严格沿用官方 `versionCode 2016182530`；未进行本地 GeckoView 编译或打包。
- 发布流程校验官方基线、ABI、99 个 Gecko locale（含 `zh-CN`）、`assets/omni.ja`、Gecko 原生库、application ID、版本、签名和校验和。
- APK：`Fenix-155.0.1-r1-arm64-v8a-release.apk`。官方 GeckoView APK SHA-256：`C0DCF28DC5ABF68094A4C7E53496939C91939331483AB8C073193620E7310775`。
- `.idsig` 仅保留本地校验和重签名使用，不作为 GitHub Release 资产；Windows Glean 原生库限制仍需 Linux 或 CI 覆盖。

### English

Official upstream baseline: `FIREFOX-ANDROID_155_0_1_RELEASE`. This release synchronizes the
Firefox Android 155.0.1 upstream revision and retains every effective Fenix product change relative
to that baseline.

#### Effective Fenix changes

- Uses the `github.aenerv7.fenix` application ID, Fenix name, and rabbit branding while retaining required Mozilla/Firefox upstream and licensing references; Focus is outside the build scope.
- Completes Simplified Chinese and official Android Gecko locale resources; the About screen, What's New link, search widget, launcher, and Fenix Labs use Fenix branding.
- Removes management entry points, autofill service, and default sync scope for passwords, addresses, credit cards, and other personal data; removes Google Play rating integration and the finished Sports/World Cup activity.
- Keeps the IP Protection entry point, marks new installations onboarding-complete, and preserves Fenix privacy, store, and settings reductions.
- Keeps grouped-tab links in their group by default and supports group-scoped selection, remove, delete undo, and empty-group restoration; group toolbars, menus, Back behavior, and drag state remain consistent.
- Opening All Tabs from a grouped tab expands and locates the current tab; group sheets always skip the half-expanded state and open fully, with no window-size or tab-count height calculation.
- Clearing the last ungrouped tab removes its snapshot, pinned list item, and drag state; the group new-tab toolbar and search widget remain usable on phones, tablets, portrait, and landscape.

#### Release and validation

- Publishes only the `arm64-v8a` APK using the official 155.0.1 multi-locale GeckoView and exact official `versionCode 2016182530`; no local GeckoView compilation or packaging was performed.
- The release process verifies the official baseline, ABI, all 99 Gecko locales (including `zh-CN`), `assets/omni.ja`, Gecko native libraries, application ID, version, signature, and checksums.
- APK: `Fenix-155.0.1-r1-arm64-v8a-release.apk`. Official GeckoView APK SHA-256: `C0DCF28DC5ABF68094A4C7E53496939C91939331483AB8C073193620E7310775`.
- `.idsig` is retained locally for verification and re-signing and is not a GitHub Release asset; the Windows Glean native-library limitation still requires Linux or CI coverage.

## 155.0-r14

### 中文

官方上游基线：`FIREFOX-ANDROID_155_0_RELEASE`

以下为 r14 最终版本相对于该官方基线的全部有效 Fenix 修改；不包含后来已撤销的中间实现。

#### 品牌、版本与项目链接

- 将 Android 应用 ID 改为 `github.aenerv7.fenix`，产品名称改为 Fenix，并移除上游版本名称中的 `-default` 后缀；Focus 不在本项目的构建和发布范围内。
- 在启动器、启动画面、主页、关于页面、Gecko About 页面和 favicon 中使用 Fenix 兔子品牌；启动器前景采用透明画布并缩放至原尺寸的 80%，避免图标过大。
- 将打包 Android 语言中面向用户的 Firefox 产品名称替换为 Fenix，同时保留上游来源、许可证和兼容性说明中的必要 Mozilla/Firefox 引用。
- “关于 Fenix”保留对 aenerv7@GitHub、Mozilla 和 Firefox 上游源码的本地化署名；支持链接指向项目 GitHub，隐私声明与权利链接指向 Fenix 项目文档，“更新内容”使用 Fenix 名称。
- “关于 Fenix”在上游版本信息后显示当前第 14 次修改。`r14` 仅作为显示和发布元数据，不改变上游 `versionName 155.0` 或 arm64-v8a 的 `versionCode 2016180970`。

#### 本地化与隐私裁剪

- 补全稳定版使用的简体中文资源，包括标签页群组操作和标签，以及将移动端书签根目录显示为“移动收藏夹”。
- 打包完整的官方 Android Gecko 语言集，使 WebExtension 和 Gecko 界面使用所选应用语言而不是回退到 `en-US`；本次 APK 已验证 99 个 Gecko locale，包括 `zh-CN`。
- 移除密码、地址和信用卡等个人信息的管理入口、快捷方式、Intent、设置索引、后台初始化与维护，以及 Android 自动填充服务和相关活动。
- 禁用登录信息自动填充，并从默认同步范围中移除密码、地址和信用卡数据及其设置项。
- 保留并接通内置 VPN（IP Protection）的设置、主菜单、引导提示、状态反馈和位置选择入口，确保上游布局更新后 Fenix 的入口仍可用。

#### 首次使用、商店与限时功能

- 新安装默认视为已完成首次引导，直接使用默认配置启动；移除已无生产引用的旧 PWA 第三次访问安装引导对话框。
- 移除 Google Play 评分入口、自动评价提示、Play Review SDK、商店回退逻辑及相关评分遥测。
- 按限时活动策略完整移除已结束的 Sports/World Cup 功能，包括入口、状态、业务逻辑、测试、字符串、旗帜和专用图片，同时保留名称相似但无关的搜索设置。

#### 标签页与群组

- 群组标签页打开的链接默认留在原群组；链接菜单可选择在当前群组打开或新建群组。
- 可配置工具栏快捷方式创建的新标签页保留在当前群组，随后提交的网址或搜索也保留群组关系；主页中的网址和搜索在当前标签页提交。
- 群组展开页支持群组范围的多选和“移出群组”，部分删除撤销可恢复成员关系，删除全部标签页后的撤销可恢复被清空的群组。
- 群组选择工具栏与内容宽度对齐，并正确处理层级、系统栏内边距、明暗主题、菜单位置和返回操作；第一次返回只退出选择状态，不会同时收起群组。
- 使用上游群组顶部工具栏的新建标签页按钮并保证其在手机、平板和横竖屏均可见，移除重复的 Fenix 浮动按钮。
- 将独立标签页加入群组后保持目标群组可见并正确选中；进入“全部标签页”时自动展开所选标签页所在群组并滚动到所选标签页，自动展开不播放打开动画。
- 关闭最后一个非群组标签页时同步清理数据快照、Lazy 列表固定项和拖拽状态，避免残留不可操作的旧条目。
- 修复群组内及“全部标签页”中的长按拖拽状态，在持续拖动期间保留正确的选择与重排状态，不再因同步切换交互模式而取消手势。
- 修复从群组内标签页打开“全部标签页”时群组弹层错误回弹到半高的问题；现在会根据最新群组内容保持正确的全展开状态。
- 群组弹层现在始终跳过半高状态并直接全高打开；移除按窗口尺寸和标签数量决定初始高度的无效逻辑。

#### 界面与兔子资源

- 搜索小组件的运行时图标、布局和系统添加预览统一使用无背景 Fenix 兔子；添加预览恢复为横向长条搜索框，左侧兔子放大且不遮挡搜索文字或麦克风区域。
- 移除默认浏览器提示中的 Mozilla 图片及其占位，将关闭按钮调整到右侧垂直居中。
- Fenix 实验室空状态使用无背景兔子资源；顶部欢迎提示仅保留文字，移除右侧兔子图标。

#### 发布与验证

- 仅发布 `arm64-v8a` APK，使用官方 155.0 多语言 GeckoView 组装，并严格沿用官方 arm64-v8a `versionCode 2016180970`。
- 发布流程校验官方基线、ABI、99 个 locale、`assets/omni.ja`、`libmozglue.so`、`libxul.so`、应用 ID、版本、APK Signature Scheme v2/v3、文件大小和 SHA-256。
- 定向标签页群组、标签页托盘、搜索小组件和 Fenix 实验室回归覆盖已在相应修订中通过；r14 另通过 `fenix:ktlintFormat`、`fenix:ktlint`、`fenix:compileDebugKotlin` 和目标单元测试任务。受 Windows Application Services 原生库限制的 Glean 测试会在本机跳过，仍需由 Linux 或 CI 覆盖。
- 发布资产为 `Fenix-155.0-r14-arm64-v8a-release.apk`，大小 `130818099` 字节，SHA-256 为 `E1AD8C6BC0A49B8417230748569285C01E50C13B06F3096F818E2425503237F5`。
- `.idsig` 仅保留本地用于校验，不作为 GitHub Release 资产发布。

### English

Official upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

The following is the complete set of effective Fenix changes in the final r14 build relative to that official baseline; intermediate implementations that were later reverted are excluded.

#### Branding, version, and project links

- Changed the Android application ID to `github.aenerv7.fenix`, renamed the product to Fenix, and removed the upstream `-default` version-name suffix. Focus is outside this project's build and release scope.
- Uses Fenix rabbit branding for the launcher, splash screen, homepage, About screen, Gecko About page, and favicons. The launcher foreground uses a transparent canvas with the rabbit scaled to 80% to avoid an oversized icon.
- Replaced user-facing Firefox product-name text in packaged Android locales with Fenix while preserving required Mozilla/Firefox references in upstream-source, licensing, and compatibility text.
- The About Fenix screen keeps localized attribution to aenerv7@GitHub, Mozilla, and the upstream Firefox source. Support links to the project GitHub repository, privacy and rights link to the Fenix project documentation, and What's New uses the Fenix name.
- About Fenix displays modification number 14 after the upstream version. `r14` is display and release metadata only and does not change upstream `versionName 155.0` or the arm64-v8a `versionCode 2016180970`.

#### Localization and privacy reductions

- Filled missing Simplified Chinese resources used by the stable build, including tab-group actions and labels, and displays the mobile bookmarks root as “移动收藏夹”.
- Packages the complete official Android Gecko locale set so WebExtension and Gecko UI follow the selected application language instead of falling back to `en-US`; this APK verifies 99 Gecko locales, including `zh-CN`.
- Removed management entry points, shortcuts, intents, settings indexing, background initialization and maintenance for passwords, addresses, credit cards, and other personal information, along with the Android autofill service and its activities.
- Disabled login autofill and removed passwords, addresses, and credit cards plus their settings from the default sync scope.
- Kept and reconnected the built-in VPN (IP Protection) settings, main-menu entry, onboarding prompt, status feedback, and location picker so the Fenix entry points remain available after the upstream layout update.

#### First use, store integration, and limited-time features

- New installations default to onboarding complete and start with the default configuration. The obsolete, unreferenced third-visit PWA installation onboarding dialog was also removed.
- Removed the Google Play rating entry, automatic review prompts, Play Review SDK, store fallbacks, and related rating telemetry.
- Fully removed the finished Sports/World Cup feature under the limited-time activity policy, including entry points, state, business logic, tests, strings, flags, and dedicated artwork, while retaining unrelated search settings with similar names.

#### Tabs and tab groups

- Links opened from a grouped tab remain in that group by default; link menus can open in the current group or create a new group.
- New tabs created by the configurable toolbar shortcut remain in the current group, and subsequent URL or search submissions retain that group relationship; homepage URL and search submissions stay in the current tab.
- Expanded groups support group-scoped multi-selection and Remove from group. Undo restores membership after partial deletion and restores an emptied group after all its tabs are deleted.
- The group selection toolbar matches the content width and correctly handles layering, system-bar insets, light/dark themes, menu placement, and Back. The first Back exits selection without also collapsing the group.
- Uses the upstream group-toolbar new-tab action and keeps it visible on phones, tablets, portrait, and landscape, with the duplicate Fenix floating action removed.
- After moving a standalone tab into a group, the target group remains visible and correctly selected. Entering All Tabs automatically expands the selected tab's group and scrolls to the selected tab without playing the opening animation.
- Closing the last ungrouped tab clears the data snapshot, pinned Lazy-list item, and drag state together so no stale, non-interactive row remains.
- Fixed long-press drag state in expanded groups and All Tabs so selection and reordering remain valid during continued dragging instead of the gesture being cancelled by a synchronous interaction-mode change.
- Fixed the group sheet snapping back to half-expanded when All Tabs is opened from a grouped tab; the latest group contents now determine whether the sheet stays fully expanded.
- Group sheets now always skip the half-expanded state and open fully; the obsolete window-size and tab-count height policy has been removed.

#### UI and rabbit assets

- The search widget runtime icon, layouts, and system add-widget preview consistently use the background-free Fenix rabbit. The add preview is a horizontal search bar with a larger rabbit on the left that does not cover the search text or microphone area.
- Removed Mozilla artwork and its reserved space from the default-browser prompt, with the close button vertically centered at the right edge.
- Fenix Labs uses the background-free rabbit for its empty state; the top welcome banner keeps only its text and no longer shows a rabbit on the right.

#### Release and validation

- Publishes only the `arm64-v8a` APK, assembled with the official 155.0 multi-locale GeckoView, and strictly preserves official arm64-v8a `versionCode 2016180970`.
- The release process verifies the official baseline, ABI, all 99 locales, `assets/omni.ja`, `libmozglue.so`, `libxul.so`, application ID, version, APK Signature Schemes v2/v3, file size, and SHA-256.
- Focused tab-group, tab-tray, search-widget, and Fenix Labs regression coverage passed in the relevant revisions. r14 also passed `fenix:ktlintFormat`, `fenix:ktlint`, `fenix:compileDebugKotlin`, and the targeted unit-test tasks. Glean tests affected by missing Windows Application Services native libraries are skipped locally and still require Linux or CI coverage.
- The release asset is `Fenix-155.0-r14-arm64-v8a-release.apk`, size `130818099` bytes, SHA-256 `E1AD8C6BC0A49B8417230748569285C01E50C13B06F3096F818E2425503237F5`.
- `.idsig` is retained locally for verification and is not published as a GitHub Release asset.

## 155.0-r11

### 中文

上游基线：`FIREFOX-ANDROID_155_0_RELEASE`

#### 标签页与群组

- 修复标签页长按拖拽过程中选择模式被同步清除的问题，长按后继续拖拽时保持正确的选择和重排状态。
- 保留上游长按手势序列，避免在手势回调中同步切换交互模式导致拖拽被取消。

#### 发布与验证

- 仅发布 `arm64-v8a` 架构 APK，严格沿用上游 `versionCode 2016180970`。
- 使用官方 155.0 多语言 GeckoView 组装，校验基线、ABI、完整 locale 集、`omni.ja`、Gecko 原生库、应用 ID、签名和校验和后再发布。
- `.idsig` 仅保留本地用于校验，不作为 GitHub Release 资产发布。

### English

Upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

#### Tabs and groups

- Fixed selection mode being cleared while dragging a tab after a long press; selection and reordering now remain in the correct state throughout the gesture.
- Preserved the upstream long-press gesture sequence so changing interaction mode from a gesture callback cannot cancel the active drag.

#### Release and validation

- Publishes only the `arm64-v8a` APK and strictly preserves upstream `versionCode 2016180970`.
- Assembles the release with the official 155.0 multi-locale GeckoView and verifies the baseline, ABI, complete locale set, `omni.ja`, Gecko native libraries, application ID, signature, and checksums before publishing.
- `.idsig` is retained locally for verification and is not published as a GitHub Release asset.

## 155.0-r10

### 中文

上游基线：`FIREFOX-ANDROID_155_0_RELEASE`

#### 标签页与群组

- 修复在“全部标签页”中关闭最后一个非群组标签页后的网格残留，移除不可操作的旧条目。
- 修复群组内标签页长按后拖拽立即退出选择模式的问题，保留拖拽期间的选中状态。

#### 发布与 GeckoView

- 仅发布 `arm64-v8a` 架构 APK，严格沿用上游 `versionCode 2016180970`。
- 使用官方 155.0 多语言 GeckoView 二进制组装，清理本地自编译 GeckoView 产物；校验基线、ABI、99 个 locale、`omni.ja` 和 Gecko 原生库后再打包。
- `.idsig` 仅保留本地用于校验，不作为 GitHub Release 资产发布。

### English

Upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

#### Tabs and groups

- Fixed the non-interactive grid residue left after closing the last ungrouped tab in All Tabs.
- Fixed long-press drag immediately leaving selection mode inside a tab group; selection now remains active throughout the drag.

#### Release and GeckoView

- Publishes only the `arm64-v8a` APK and strictly preserves upstream `versionCode 2016180970`.
- Packages the official Firefox Android 155.0 multi-locale GeckoView binaries after cleaning locally compiled GeckoView output; baseline, ABI, all 99 locales, `omni.ja`, and Gecko native libraries are verified before assembly.
- `.idsig` is retained locally for verification and is not published as a GitHub Release asset.

## 155.0-r9

### 中文

上游基线：`FIREFOX-ANDROID_155_0_RELEASE`

#### 标签页与群组

- 群组内长按标签页进入选择模式后，返回操作由群组弹层优先处理，并立即清理工具栏、选中状态和本地拖拽状态，不再收起群组或残留选择界面。
- 关闭最后一个非群组标签页时立即清除 Lazy 列表固定项与拖拽状态，并对浏览器标签页数据建立不可变快照，避免旧条目停留到下一次操作才消失。

#### 品牌资产

- 桌面搜索小组件的运行时图标、布局预览和系统小组件预览统一改用无背景 Fenix 兔子徽标。
- 移除默认浏览器提示图片后，将关闭按钮调整为右侧垂直居中。

#### 发布

- 仅发布 `arm64-v8a` 架构 APK，严格沿用上游 `versionCode 2016180970`。
- `.idsig` 仅保留本地用于校验，不作为 GitHub Release 资产发布。

### English

Upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

#### Tabs and groups

- Back is now consumed by the expanded-group sheet while tab selection is active, immediately clearing the toolbar, selection, and local drag state without collapsing the group or leaving stale selection UI.
- Closing the last ungrouped tab now clears pinned Lazy-list and drag state immediately and uses an immutable browser-tab snapshot, preventing the removed row from lingering until another action refreshes the tray.

#### Brand assets

- The home-screen search widget now uses the background-free Fenix rabbit for its runtime icon, layout previews, and system widget preview.
- After removing the default-browser prompt artwork, its close button is now vertically centered at the right edge.

#### Release

- Publishes only the `arm64-v8a` APK and strictly preserves upstream `versionCode 2016180970`.
- `.idsig` is retained locally for verification and is not published as a GitHub Release asset.

## 155.0-r8

### 中文

上游基线：`FIREFOX-ANDROID_155_0_RELEASE`

#### 标签页群组

- 修复关闭最后一个非群组标签页后旧条目仍残留在“全部标签页”界面的显示状态；数据源和列表交互状态都会同步清理。
- 修复群组内标签页长按工具栏返回时重复触发导航，第一次返回只关闭选择工具栏，不会同时收起群组。

#### 品牌与发布

- 发布图标使用透明画布上缩小至原尺寸 80% 的兔子徽标，修复启动器图标资源缺失问题；应用内不带背景的兔子资源保持不变。
- 仅发布 `arm64-v8a` 架构 APK，严格沿用上游 `versionCode 2016180970`。
- `.idsig` 仅保留本地用于校验，不作为 GitHub Release 资产发布。
- 本次仅改动 Fenix Kotlin、资源和构建校验逻辑，复用已验证的 155.0 多语言 GeckoView；未来复用上游预编译 GeckoView 必须匹配基线、Gecko 修订、ABI、完整语言集和原生库 SHA-256，否则回退编译。

### English

Upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

#### Tab groups

- Fixed stale display state after closing the last ungrouped tab while an open group remains; both the data source and list interaction state are now cleared.
- Fixed duplicate navigation from the expanded-group long-press toolbar: the first Back press closes only the selection toolbar and does not collapse the group.

#### Branding and release

- The launcher now uses a transparent rabbit foreground scaled to 80% of its original size, fixing the missing launcher icon; the background-free in-app rabbit resource is unchanged.
- Published the `arm64-v8a` APK only and kept the upstream `versionCode 2016180970`.
- `.idsig` is retained locally for verification and is not published as a GitHub Release asset.
- This revision changes only Fenix Kotlin, resources, and build validation logic, so it reuses the validated 155.0 multi-locale GeckoView package. Any future upstream prebuilt GeckoView reuse must match the baseline, Gecko revision, ABI, complete locale set, and native-library SHA-256, or fall back to compilation.

## 155.0-r4

### 中文

上游基线：`FIREFOX-ANDROID_155_0_RELEASE`

#### 标签页群组

- 移除我们后来添加的、与上游功能重复的群组内浮动新建标签页按钮，保留顶部工具栏中的新建标签页按钮。
- 顶部工具栏新建标签页按钮在手机、平板及横竖屏布局中始终显示，用于替代已移除的浮动按钮。
- 将群组内标签页长按后的选择工具栏宽度与群组视图内容对齐，避免工具栏铺满整个窗口。
- 修复仅剩一个独立标签页且存在标签页群组时关闭该标签页后仍显示并保持选中的问题。

#### 发布与验证

- 仅发布 `arm64-v8a` 架构 APK。
- 严格沿用上游 arm64-v8a 基线 `versionCode 2016180970`，不因 r4 修订改变。
- `.idsig` 仅保留本地用于校验，不作为 GitHub Release 资产发布。

### English

Upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

#### Tab groups

- Removed the fork-added floating new-tab button from the expanded group because it duplicated the toolbar action; the top-toolbar new-tab button remains available.
- The top-toolbar new-tab button is always shown across phone, tablet, portrait, and landscape layouts, replacing the removed floating action.
- Matched the long-press selection toolbar width to the expanded group's content width instead of the full window.
- Fixed the last ungrouped tab remaining visible and selected after it was closed while an open tab group remained.

#### Release and validation

- Published the `arm64-v8a` APK only.
- Keeps the upstream arm64-v8a baseline `versionCode 2016180970`; the r4 revision never changes it.
- `.idsig` is retained locally for verification and is not published as a GitHub Release asset.

## 155.0-r3

### 中文

上游基线：`FIREFOX-ANDROID_155_0_RELEASE`

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

Upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

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

上游基线：`FIREFOX-ANDROID_155_0_RELEASE`

#### 品牌

- 将 Fenix 实验室横幅徽标替换为透明背景的 Fenix 兔子徽标。

#### 标签页群组

- 在展开的标签页群组视图中使用与“全部标签页”相同的控件增加新建标签页浮动操作。
- 保持选择模式下隐藏操作，并预留底部滚动空间，确保最后一个标签页完全可见。

#### 验证

- 定向 `ExpandedTabGroupTest` 测试套件通过，覆盖新建标签页激活、选择模式可见性和列表底部遮挡检查。
- Debug 单元测试编译以及 Release Kotlin/Java 编译通过。

### English

Upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

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
