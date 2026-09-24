# Fenix changes

## 156.0.1-r2

### 中文

官方上游基线：`FIREFOX-ANDROID_156_0_1_RELEASE`（与 `156.0.1-r1` 相同）。本版本移除盾牌面板
“Fenix 正在防护”横幅中的小狐狸美术资源，其余内容与 `156.0.1-r1` 相同。

点击地址栏盾牌图标弹出的面板中，“%s 正在防护”横幅会在文案右侧显示上游的 Firefox 狐狸头矢量图
（`kit_head_protection_blocker_banner`）。可见的 Mozilla 狐狸资源不允许出现在 Fenix 面向用户的
界面上。

狐狸是独立的 vector（透明背景，仅狐狸头），横幅底板由 Compose 绘制紫→橙渐变，两者不是同一个美术
资产，因此只移除狐狸，底板保留。该 drawable 已无任何引用，一并删除。

#### 发布与验证

- `fenix:spotlessKotlinCheck` 通过。
- `ProtectionPanelTest` 6 项全部通过，覆盖横幅文案、复数与点击回调，确认移除图片未影响横幅行为与
  无障碍描述。
- 仅发布 `arm64-v8a` APK，使用官方 156.0.1 多语言 GeckoView，严格沿用官方 `versionCode 2016185922`
  和上游 `versionName 156.0.1`；未进行本地 GeckoView 编译或打包。
- 已确认签名 APK 的资源表中不再包含 `kit_head_protection_blocker_banner`。
- APK：`Fenix-156.0.1-r2-arm64-v8a-release.apk`，大小 `131247654` 字节，SHA-256：`FACBDC52C7FCF00AD0C7E1C1DDC6D8CD3FEE7E5DEDFC9D977B0688A2743AECB6`。
- 对应完整源码：[fenix-156.0.1-r2](https://github.com/aenerv7/fenix/tree/fenix-156.0.1-r2)。Fenix 是非官方独立修改版，不受 Mozilla 赞助或背书；维护与支持由本项目提供。保留 MPL 2.0 和第三方许可；Firefox 是 Mozilla Foundation 的商标。
- `.idsig` 仅保留本地校验和重签名使用，不作为 GitHub Release 资产；Windows Glean 原生库限制仍需 Linux 或 CI 覆盖。

### English

Official upstream baseline: `FIREFOX-ANDROID_156_0_1_RELEASE` (unchanged from `156.0.1-r1`). This
release removes the small fox artwork from the shield panel's "is on guard" banner; everything else is
identical to `156.0.1-r1`.

In the panel opened from the shield icon next to the URL, the `%s is on guard` banner rendered the
upstream Firefox fox head vector (`kit_head_protection_blocker_banner`) to the right of the text.
Visible Mozilla fox assets are not allowed on Fenix user-facing surfaces.

The fox is a standalone vector (transparent background, fox head only) while the banner plate is a
purple-to-orange gradient drawn by Compose, so the two are not a single artwork asset. Only the fox is
removed and the plate is kept. The drawable had no remaining references and is deleted as well.

#### Release and validation

- `fenix:spotlessKotlinCheck` passes.
- All 6 `ProtectionPanelTest` cases pass, covering the banner text, plurals, and click callback, which
  confirms that removing the image did not affect the banner behavior or its accessibility description.
- Publishes only the `arm64-v8a` APK using the official 156.0.1 multi-locale GeckoView and the exact
  official `versionCode 2016185922` with upstream `versionName 156.0.1`; no local GeckoView compilation
  or packaging was performed.
- The signed APK resource table was confirmed to no longer contain `kit_head_protection_blocker_banner`.
- APK: `Fenix-156.0.1-r2-arm64-v8a-release.apk`, size `131247654` bytes, SHA-256: `FACBDC52C7FCF00AD0C7E1C1DDC6D8CD3FEE7E5DEDFC9D977B0688A2743AECB6`.
- Complete corresponding source: [fenix-156.0.1-r2](https://github.com/aenerv7/fenix/tree/fenix-156.0.1-r2). Fenix is an independent unofficial modified build, not sponsored or endorsed by Mozilla; this project provides maintenance and support. MPL 2.0 and third-party licenses are retained; Firefox is a trademark of the Mozilla Foundation.
- `.idsig` is retained locally for verification and re-signing and is not a GitHub Release asset; the Windows Glean native-library limitation still requires Linux or CI coverage.

## 156.0.1-r1

### 中文

官方上游基线：`FIREFOX-ANDROID_156_0_1_RELEASE`。本版本将上游基线从 156.0 更新到 Firefox Android
156.0.1，并把 Fenix 的全部有效改动重新落位到 156.0.1 代码之上。

上游 156.0 到 156.0.1 的增量为 306 个文件、约 1.5 万行新增。合并以 `git apply --3way` 应用上游树
增量，43 个冲突全部集中在 Fenix 的本地化资源 `values-*/strings.xml`，已逐文件解决。候选相对于
Fenix 父提交实际改动 309 个文件，与上游增量规模一致，确认增量已完整落地，而非只改基线标记。

#### 有效 Fenix 改动

- 使用 `github.aenerv7.fenix` application ID、Fenix 名称和兔子品牌；保留必要的 Mozilla/Firefox
  上游与许可证说明；Focus 不在构建范围内。
- 主页、关于页和关于弹层使用透明背景的 Fenix 兔子 wordmark，替换上游的 Firefox wordmark 组件；
  私人浏览解锁页与生物识别解锁页使用 Fenix wordmark 资源。启动器透明前景缩放至 80%。
- 标签页群组：始终以完全展开方式打开群组面板；展开群组复用全部标签页的选择工具栏样式与内容宽度；
  长按群组内标签页派发群组作用域的选择动作，不改变全局标签栏导航行为。
- 展开群组界面自行消费系统返回：有选择时先清空选择，浏览器当前标签页属于该群组时显示该标签页，
  否则折叠群组。手动关闭面板（拖拽、遮罩或把手）始终折叠群组并清空选择，绝不打开标签页。
- 关闭最后一个未分组标签页时，同步从不可变快照和 Lazy 列表交互状态中移除该项，并重置缺失的拖拽键，
  避免残留的标签页仍可见、可选中或可交互。
- 保留上游长按手势序列：不在长按回调中同步切换 `pointerInput` 模式，以免取消进行中的手势。
- 地址栏与搜索建议点击使用当前 `AppStore.searchState.sourceTabId`，保留既有标签页/群组归属、隐私
  模式，以及搜索来源缺失时的新建标签页回退。
- IP Protection 入口保留。已移除密码、自动填充、同步与 Play 商店评分入口；自定义标签页
  “Powered by Fenix” 菜单项使用 Fenix 品牌。
- 已按限时活动政策移除 Sports/世界杯活动；名称恰好重叠的搜索优化设置保留。
- 简体中文为本地维护语言：`values-zh-rCN` 覆盖 `values/strings.xml` 的全部 1789 条字符串与
  `values/arrays.xml` 的全部 5 个数组，缺失数为 0。

#### 本次基线的界面变化

- 新增“无法在此设备上打开文件夹选择器”提示的简体中文翻译（`preferences_downloads_no_folder_picker_available`）。
- 上游移除 `customize_toggle_jump_back_in` 与 `ip_protection_menu_auth_required`，本地化文件同步删除。
- 上游新增 VPN 位置相关的 5 条字符串（最快位置说明、位置不可用说明与错误、推荐位置提示、位置列表
  不可用标题），已全部翻译，品牌名沿用 Fenix。
- 隐私报告通知与最近标签页标题改用上游重组后的注释格式与措辞，去除本地重复条目。
- 保留 Fenix 品牌：凡上游写 Firefox 而本fork写 Fenix 的字符串（fa、hi-rIN、hr、is、sl、sr 六种
  语言），一律沿用 Fenix 文案。

#### 发布与验证

- 仅发布 `arm64-v8a` APK，使用官方 156.0.1 多语言 GeckoView，严格沿用官方 `versionCode 2016185922`
  和上游 `versionName 156.0.1`；未进行本地 GeckoView 编译或打包。
- 发布流程校验官方基线、ABI、99 个 Gecko locale（含 `zh-CN`）、`assets/omni.ja`、Gecko 原生库、
  application ID、版本、签名和校验和。
- `fenix:spotlessKotlinCheck` 通过；`fenix:testDebugUnitTest` 完成 6061 项测试，失败 252 项、
  跳过 1339 项。失败的 27 个测试类全部是 156.0 基线上已失败的同一批（其中 228 项为 Windows
  Glean/JNA 原生库限制），相对上一基线新增失败数为 0。
- 已从签名 APK 的资源表确认新增中文串解析为 `zh-rCN` 中文值（如“无法在此设备上打开文件夹选择器。”）。
- APK：`Fenix-156.0.1-r1-arm64-v8a-release.apk`，大小 `131256412` 字节，SHA-256：`4F7736FE3988107EF0A29E962F67C89BD70731DC62FB9AACF8CFB2361E1174E0`。
- 对应完整源码：[fenix-156.0.1-r1](https://github.com/aenerv7/fenix/tree/fenix-156.0.1-r1)。Fenix 是非官方独立修改版，不受 Mozilla 赞助或背书；维护与支持由本项目提供。保留 MPL 2.0 和第三方许可；Firefox 是 Mozilla Foundation 的商标。
- `.idsig` 仅保留本地校验和重签名使用，不作为 GitHub Release 资产；Windows Glean 原生库限制仍需 Linux 或 CI 覆盖。

### English

Official upstream baseline: `FIREFOX-ANDROID_156_0_1_RELEASE`. This release updates the baseline from
156.0 to Firefox Android 156.0.1 and re-applies the complete Fenix change set on top of the 156.0.1
sources.

The upstream 156.0 to 156.0.1 delta spans 306 files and roughly 15k added lines. The upstream tree
delta was applied with `git apply --3way`; all 43 conflicts were confined to the Fenix localization
resources `values-*/strings.xml` and were resolved file by file. The candidate changes 309 files
against its Fenix parent, matching the upstream delta size, which confirms the delta landed in full
rather than only the baseline marker.

#### Effective Fenix changes

- Uses the `github.aenerv7.fenix` application ID, the Fenix name, and the rabbit branding; retains the
  required Mozilla/Firefox upstream and license notices; Focus is out of build scope.
- The home screen, About screen, and About sheet use the transparent-background Fenix rabbit wordmark
  instead of the upstream Firefox wordmark component; the private-browsing unlock and biometric unlock
  screens use Fenix wordmark resources. The launcher foreground is scaled to 80%.
- Tab groups: the group sheet always opens fully expanded; expanded groups reuse the All Tabs selection
  toolbar styling and content width; long-pressing a group tab dispatches the group-scoped selection
  action without changing the global tab-tray navigation behavior.
- The expanded-group screen consumes system Back itself: it clears a non-empty selection first, shows
  the browser tab when the focused tab belongs to the displayed group, and otherwise collapses the
  group. Manual sheet dismissal (drag, scrim, or handle) always collapses the group and clears its
  selection; it never opens a tab.
- Closing the last ungrouped tab removes the item from both the immutable snapshot and the Lazy
  list/grid interaction state immediately and resets missing drag keys, so the removed tab cannot
  remain visible, selected, or interactable.
- The upstream long-press gesture sequence is preserved: the `pointerInput` mode is not switched
  synchronously from the long-press callback, which would cancel the active gesture.
- URL and search suggestion clicks use the current `AppStore.searchState.sourceTabId`, preserving
  existing tab/group membership, private mode, and the new-tab fallback when the search source is
  absent.
- The IP Protection entry point is retained. Password, autofill, sync, and Play Store rating entry
  points are removed; the Custom Tab "Powered by Fenix" menu item uses Fenix branding.
- The Sports/World Cup activity was removed under the limited-time activity policy; search-optimization
  settings whose names merely overlap are retained.
- Simplified Chinese is fork-maintained: `values-zh-rCN` covers all 1789 strings in
  `values/strings.xml` and all 5 arrays in `values/arrays.xml`, with zero missing.

#### User-visible changes from this baseline

- Adds the Simplified Chinese translation for the new "Unable to open the folder picker on this
  device." message (`preferences_downloads_no_folder_picker_available`).
- Upstream removed `customize_toggle_jump_back_in` and `ip_protection_menu_auth_required`; the
  localization files drop them as well.
- Upstream added 5 VPN location strings (fastest-location description, unavailable-location
  description and error, recommended-location notice, and locations-unavailable title); all are
  translated, using the Fenix brand name.
- The privacy report notification and recent tabs header adopt the upstream reorganized comment format
  and wording, removing local duplicates.
- Fenix branding is preserved: where upstream writes Firefox and this fork writes Fenix (the fa, hi-rIN,
  hr, is, sl, and sr locales), the Fenix wording is kept.

#### Release and validation

- Publishes only the `arm64-v8a` APK using the official 156.0.1 multi-locale GeckoView and the exact official `versionCode 2016185922` with upstream `versionName 156.0.1`; no local GeckoView compilation or packaging was performed.
- The release process verifies the official baseline, ABI, all 99 Gecko locales (including `zh-CN`), `assets/omni.ja`, Gecko native libraries, application ID, version, signature, and checksums.
- `fenix:spotlessKotlinCheck` passes; `fenix:testDebugUnitTest` completed 6061 tests with 252 failures and 1339 skipped. All 27 failing test classes are the same set that already failed on the 156.0 baseline (228 of the failures are the Windows Glean/JNA native-library limitation), so there are zero new failures relative to the previous baseline.
- The new Chinese string was confirmed to resolve to a `zh-rCN` Chinese value directly from the signed APK resource table (for example 无法在此设备上打开文件夹选择器。).
- APK: `Fenix-156.0.1-r1-arm64-v8a-release.apk`, size `131256412` bytes, SHA-256: `4F7736FE3988107EF0A29E962F67C89BD70731DC62FB9AACF8CFB2361E1174E0`.
- Complete corresponding source: [fenix-156.0.1-r1](https://github.com/aenerv7/fenix/tree/fenix-156.0.1-r1). Fenix is an independent unofficial modified build, not sponsored or endorsed by Mozilla; this project provides maintenance and support. MPL 2.0 and third-party licenses are retained; Firefox is a trademark of the Mozilla Foundation.
- `.idsig` is retained locally for verification and re-signing and is not a GitHub Release asset; the Windows Glean native-library limitation still requires Linux or CI coverage.

## 156.0-r2

### 中文

官方上游基线：`FIREFOX-ANDROID_156_0_RELEASE`。本版本补全简体中文，其余内容与 `156.0-r1` 相同。

`156.0-r1` 使用的资源已包含上游 156.0 新增的 38 条面向用户的字符串，但 `values-zh-rCN` 尚无对应
翻译。Android 在缺少翻译时按字符串回退到英文，因此这些条目在中文界面中显示为英文，例如设置中标签页
选项里的 “Enable Tab Groups”。

本次为全部 38 条补齐简体中文翻译，沿用文件既有术语（标签页群组、取消群组、跟踪器、隐私报告、收藏集、
重试）。改动为纯新增，未修改任何现有翻译，并已按 `values/strings.xml` 的顺序插入对应位置。

#### 补齐的界面

- 标签页群组：设置中的启用开关、三点菜单的取消群组、取消群组的确认对话框标题、正文与按钮。
- 标签页操作的无障碍自定义操作：上移、下移、左移、右移标签页。
- PDF 工具：签名、下载、打印、分享的内容描述，以及添加签名对话框的标题、关闭说明、输入提示、清除与添加按钮。
- 隐私报告通知：通知渠道名称与说明，以及有/无跟踪器时的通知标题与正文。
- IP Protection：VPN 关闭状态的标签与说明、位置不可用提示、位置重置提示、位置列表不可用标题、返回按钮说明、免费试用入口。
- 收藏集迁移：主页迁移卡片标题、正文与链接。
- 其他：最近标签页标题、定制主页“继续”、扩展操作失败重试、最深主题、无可用同步设备说明、Firefox Labs 网站隔离说明。

#### 发布与验证

- 仅发布 `arm64-v8a` APK，使用官方 156.0 多语言 GeckoView，严格沿用官方 `versionCode 2016183650` 和上游 `versionName 156.0`；未进行本地 GeckoView 编译或打包。
- 发布流程校验官方基线、ABI、99 个 Gecko locale（含 `zh-CN`）、`assets/omni.ja`、Gecko 原生库、application ID、版本、签名和校验和。
- `fenix:assembleDebug` 与 `fenix:spotlessKotlinCheck` 通过。
- 资源审计确认 `values/strings.xml` 的 1788 条字符串与 `values/arrays.xml` 的 5 个数组在 `values-zh-rCN` 中均有对应条目，缺失数为 0；复数资源 15 条亦全部覆盖。
- 已将该 APK 安装到 Android 34 x86_64 模拟器并将系统语言设为简体中文：应用正常启动，无 `FATAL EXCEPTION`；从已安装 APK 的资源表逐条确认 38 条字符串均解析为 `zh-rCN` 中文值；界面确认真实显示“搜索”“添加快捷方式”“一起抓跟踪器”“免费试用”等中文文案。
- APK：`Fenix-156.0-r2-arm64-v8a-release.apk`，大小 `131010652` 字节，SHA-256：`C8D3E79B6D307AC219457B70D21419368814F790DEBDD532070DE54105A0FC94`。
- 对应完整源码：[fenix-156.0-r2](https://github.com/aenerv7/fenix/tree/fenix-156.0-r2)。Fenix 是非官方独立修改版，不受 Mozilla 赞助或背书；维护与支持由本项目提供。保留 MPL 2.0 和第三方许可；Firefox 是 Mozilla Foundation 的商标。
- `.idsig` 仅保留本地校验和重签名使用，不作为 GitHub Release 资产；Windows Glean 原生库限制仍需 Linux 或 CI 覆盖。

### English

Official upstream baseline: `FIREFOX-ANDROID_156_0_RELEASE`. This release completes the Simplified
Chinese localization; everything else is identical to `156.0-r1`.

The resources shipped in `156.0-r1` already included 38 new user-visible strings from the 156.0
baseline, but `values-zh-rCN` had no translation for them. Android falls back to English per string
when a translation is missing, so those entries appeared in English in the Chinese UI, for example
"Enable Tab Groups" in the tab settings.

All 38 strings are now translated, reusing the terminology already established in the file
(标签页群组, 取消群组, 跟踪器, 隐私报告, 收藏集, 重试). The change is additive: no existing translation
is modified, and each string is inserted at the position matching its counterpart in
`values/strings.xml`.

#### Surfaces completed

- Tab groups: the settings enable toggle, the three-dot menu ungroup action, and the ungroup confirmation dialog title, body, and button.
- Custom accessibility actions for reordering a tab up, down, left, and right.
- PDF tools: the sign, download, print, and share content descriptions, plus the add-signature dialog title, close description, input placeholder, and clear and add buttons.
- Privacy report notifications: the channel name and description, and the notification headline and body for when trackers have and have not been blocked.
- IP Protection: the VPN-off label and description, the unavailable-location label, the location-reset snackbar, the locations-unavailable title, the back button description, and the free-trial entry point.
- Collections migration: the homepage migration card title, message, and link.
- Miscellaneous: the recent tabs header, the customize-homepage continue toggle, the add-on failure retry action, the darkest theme name, the no-sync-devices description, and the Firefox Labs website isolation description.

#### Release and validation

- Publishes only the `arm64-v8a` APK using the official 156.0 multi-locale GeckoView and the exact official `versionCode 2016183650` with upstream `versionName 156.0`; no local GeckoView compilation or packaging was performed.
- The release process verifies the official baseline, ABI, all 99 Gecko locales (including `zh-CN`), `assets/omni.ja`, Gecko native libraries, application ID, version, signature, and checksums.
- `fenix:assembleDebug` and `fenix:spotlessKotlinCheck` pass.
- A resource audit confirms that all 1788 strings in `values/strings.xml` and all 5 arrays in `values/arrays.xml` now have a `values-zh-rCN` entry, with zero missing; all 15 plurals are covered as well.
- The APK was installed on an Android 34 x86_64 emulator with the system language set to Simplified Chinese: the app launched with no `FATAL EXCEPTION`, all 38 strings were confirmed to resolve to `zh-rCN` Chinese values directly from the installed APK resource table, and the UI was confirmed to display Chinese text such as 搜索, 添加快捷方式, 一起抓跟踪器, and 免费试用.
- APK: `Fenix-156.0-r2-arm64-v8a-release.apk`, size `131010652` bytes, SHA-256: `C8D3E79B6D307AC219457B70D21419368814F790DEBDD532070DE54105A0FC94`.
- Complete corresponding source: [fenix-156.0-r2](https://github.com/aenerv7/fenix/tree/fenix-156.0-r2). Fenix is an independent unofficial modified build, not sponsored or endorsed by Mozilla; this project provides maintenance and support. MPL 2.0 and third-party licenses are retained; Firefox is a trademark of the Mozilla Foundation.
- `.idsig` is retained locally for verification and re-signing and is not a GitHub Release asset; the Windows Glean native-library limitation still requires Linux or CI coverage.

## 156.0-r1

### 中文

官方上游基线：`FIREFOX-ANDROID_156_0_RELEASE`。本版本将上游基线更新到 Firefox Android 156.0，并把
Fenix 的全部有效产品改动重新落位到 156.0 代码之上。

上游 155.0.1 到 156.0 的增量为约 1.67 万个文件。此前一次 156.0-r1 候选版本必须撤回：它的合并提交
只改动了基线标记文件，源码仍停留在 155.0.1，而版本说明与 GeckoView 二进制却按 156.0 声明，导致打开即
崩溃。本次重新合并后，候选相对于 Fenix 父提交实际改动约 1.67 万个文件，并已逐项校验。

#### 有效 Fenix 改动

- 使用 `github.aenerv7.fenix` application ID、Fenix 名称和兔子品牌，保留必要的 Mozilla/Firefox
  上游与许可证说明；Focus 不在构建范围内。
- 主页、关于页和关于弹层使用透明背景的 Fenix 兔子 wordmark，替换上游的 Firefox wordmark 组件；
  私人浏览解锁页与生物识别解锁页使用 Fenix wordmark 资源。启动器透明前景缩放至 80%，圆形与
  monochrome 图标沿用同一兔子前景。
- 补全简体中文和官方 Android Gecko 多语言资源；关于页、更新链接、搜索组件、启动器和 Fenix Labs
  使用 Fenix 品牌；关于页保留本地化的维护者及上游署名，显示第 1 次修改，但不改变 Android 包版本。
- 搜索小组件保持横向预览并独立预留图标、文字和麦克风区域，新建标签按钮使用兔子图标而非动态应用图标。
- 移除密码、地址、信用卡等个人信息管理入口、自动填充服务、自动填充配置和默认同步范围；同步设置只保留
  书签、历史和标签页。
- 移除 Google Play 评分集成、评分提示中间件、自定义评分弹层及其遥测，移除 Play 商店相关依赖与设置项；
  设置搜索索引相应移除自动填充和登录条目。
- 移除自定义标签页菜单与主菜单中的密码入口；移除主页 Firefox wordmark 组件及其测试。
- 保留已结束的 Sports/World Cup 活动移除结果；限时活动的状态、逻辑、测试、字符串和专用资源保持移除，
  保留无关的同名搜索设置。
- 保留 IP Protection 入口，设置、主菜单、引导、状态与位置选择保持接通，并采用 156.0 的代理激活动画状态。
- 群组标签页打开链接默认留在原群组，支持群组范围多选、移出、删除撤销和空群组恢复；群组工具栏、菜单、
  返回行为和拖拽状态保持正确。
- 链接菜单支持在当前群组或新群组打开；可配置工具栏快捷方式创建的新标签保留群组关系，部分删除撤销恢复
  成员关系，全部删除撤销恢复空群组。
- 从群组标签页进入“全部标签页”时自动展开并定位到当前标签；群组弹层始终跳过半高状态直接全高打开。
- 系统返回优先取消标签页或群组的长按选择并关闭对应工具栏；无选择时，若聚焦标签属于当前展开群组则显示
  该标签，否则收起群组；手动收起群组只关闭群组并清除选择，不打开标签页。
- 关闭最后一个非群组标签页时清理快照、列表固定项和拖拽状态；新建标签页工具栏和搜索组件在手机、平板、
  横竖屏保持可用。
- URL 和搜索建议点击沿用当前搜索来源标签，来源仍存在时保留群组关系，来源缺失或已关闭时才新建标签。

#### 发布与验证

- 仅发布 `arm64-v8a` APK，使用官方 156.0 多语言 GeckoView，严格沿用官方 `versionCode 2016183650`
  和上游 `versionName 156.0`；未进行本地 GeckoView 编译或打包。
- 发布流程校验官方基线、ABI、99 个 Gecko locale（含 `zh-CN`）、`assets/omni.ja`、Gecko 原生库、
  application ID、版本、签名和校验和；APK Signature Scheme v2 与 v3 均通过。
- `fenix:compileDebugKotlin`、`fenix:spotlessKotlinCheck`、`fenix:testDebugUnitTest` 的测试源码编译与
  `fenix:assembleDebug` 通过。156.0 用 Spotless/ktfmt 取代 ktlint，全树已按新格式化规则统一。
- `fenix:testDebugUnitTest` 执行 6057 项测试，253 项失败。其中 229 项为已记录的 Windows Glean/JNA
  原生库限制，其余为 Windows 环境的 Robolectric DataStore 重命名、SQLite 路径与路径分隔符差异。设置搜索
  索引回归已修复，相关测试通过。无失败项可归因于本次合并。
- 已将该 Release APK 安装到 Android 34 x86_64 模拟器（arm64-v8a 转译）并实际启动：`HomeActivity`
  处于 resumed 且可见，进程持续存活，无 `FATAL EXCEPTION`；GeckoView 引擎正常初始化并上报遥测。
  界面确认显示兔子 wordmark、“Fenix”名称、搜索栏、“You're protected”保护状态与标签计数，Fenix
  品牌与 156.0 代码均生效。
- 全量文档 gate 通过：`Failures: 0`、`Known Failures: 443`、`build succeeded`，未新增警告屏蔽规则。
- APK：`Fenix-156.0-r1-arm64-v8a-release.apk`，大小 `131010652` 字节，SHA-256：`4435B8708E934F136853B5BF5D5A67AFC07B3BB27372448D069D306DFBAA7980`。
- 对应完整源码：[fenix-156.0-r1](https://github.com/aenerv7/fenix/tree/fenix-156.0-r1)。Fenix 是非官方独立修改版，不受 Mozilla 赞助或背书；维护与支持由本项目提供。保留 MPL 2.0 和第三方许可；Firefox 是 Mozilla Foundation 的商标。
- `.idsig` 仅保留本地校验和重签名使用，不作为 GitHub Release 资产；Windows Glean 原生库限制仍需 Linux 或 CI 覆盖。

### English

Official upstream baseline: `FIREFOX-ANDROID_156_0_RELEASE`. This release updates the baseline to
Firefox Android 156.0 and re-applies every effective Fenix product change on top of the 156.0 sources.

The 155.0.1 to 156.0 upstream delta spans about 16.7k files. An earlier 156.0-r1 candidate had to be
withdrawn: its merge commit changed only the baseline marker, leaving the sources on 155.0.1 while the
release notes and GeckoView binaries declared 156.0. That mismatch crashed the app on startup. The
replacement merge changes about 16.7k files relative to the Fenix parent and each item was verified.

#### Effective Fenix changes

- Uses the `github.aenerv7.fenix` application ID, Fenix name, and rabbit branding while retaining
  required Mozilla/Firefox upstream and licensing references; Focus is outside the build scope.
- Home, About, and the About dialog use the transparent Fenix rabbit wordmark in place of upstream's
  Firefox wordmark composables; the private-browsing unlock screen and biometric unlock screen use
  Fenix wordmark resources. The transparent launcher foreground is scaled to 80%, and the round and
  monochrome icons reuse the same rabbit foreground.
- Completes Simplified Chinese and official Android Gecko locale resources; the About screen, update
  links, search widget, launcher, and Fenix Labs use Fenix branding. About retains localized
  maintainer/upstream attribution and displays modification number 1 without changing Android package
  versions.
- The search widget keeps a horizontal preview with separate icon, text, and microphone areas, and its
  new-tab button uses the rabbit icon instead of the dynamic app icon.
- Removes management entry points, the autofill service, autofill configuration, and default sync
  scope for passwords, addresses, credit cards, and other personal data; sync settings retain only
  bookmarks, history, and tabs.
- Removes Google Play rating integration, the review-prompt middleware, the custom review-prompt
  bottom sheet and its telemetry, the Play Store dependency and settings entries; the settings search
  index consequently drops the autofill and logins entries.
- Removes the passwords entry points from the Custom Tab menu and the main menu, and removes the home
  Firefox wordmark component and its tests.
- Keeps the retired Sports/World Cup activity removed: its state, logic, tests, strings, and dedicated
  assets stay out, while unrelated similarly named search settings remain.
- Keeps the IP Protection entry point, with settings, main menu, onboarding, state, and location
  selection wired, and adopts the 156.0 proxy-activation animation state.
- Keeps grouped-tab links in their group by default and supports group-scoped selection, remove,
  delete undo, and empty-group restoration; group toolbars, menus, Back behavior, and drag state
  remain consistent.
- Link menus support opening in the current or a new group. New tabs from the configurable toolbar
  shortcut retain their group relationship; partial-delete undo restores membership, and undo after
  deleting all members restores the group.
- Opening All Tabs from a grouped tab expands and locates the current tab; group sheets always skip
  the half-expanded state and open fully.
- System Back first clears non-empty tab/group selection and its toolbar. With no selection, it shows
  the focused tab if that tab belongs to the expanded group, or collapses the group otherwise; manual
  dismissal only collapses the group and clears selection, without opening a tab.
- Clearing the last ungrouped tab removes its snapshot, pinned list item, and drag state; the group
  new-tab toolbar and search widget remain usable on phones, tablets, portrait, and landscape.
- URL and search suggestion clicks use the current search source tab, preserving its group when the
  source still exists and only creating a new tab when the source is missing or closed.

#### Release and validation

- Publishes only the `arm64-v8a` APK using the official 156.0 multi-locale GeckoView and the exact
  official `versionCode 2016183650` with upstream `versionName 156.0`; no local GeckoView compilation
  or packaging was performed.
- The release process verifies the official baseline, ABI, all 99 Gecko locales (including `zh-CN`),
  `assets/omni.ja`, Gecko native libraries, application ID, version, signature, and checksums; APK
  Signature Scheme v2 and v3 both pass.
- `fenix:compileDebugKotlin`, `fenix:spotlessKotlinCheck`, the `fenix:testDebugUnitTest` test-source
  compilation, and `fenix:assembleDebug` pass. Version 156.0 replaces ktlint with Spotless/ktfmt, and
  the whole tree was normalized to the new formatting rules.
- `fenix:testDebugUnitTest` ran 6057 tests with 253 failures. 229 are the documented Windows
  Glean/JNA native-library limitation; the remainder are Windows-environment Robolectric DataStore
  rename, SQLite path, and path-separator differences. The settings search index regression was fixed
  and its tests now pass. No failure is attributable to this merge.
- The release APK was installed on an Android 34 x86_64 emulator (arm64-v8a translation) and actually
  launched: `HomeActivity` was resumed and visible, the process stayed alive with no
  `FATAL EXCEPTION`, and the GeckoView engine initialized and reported telemetry normally. The UI was
  confirmed to show the rabbit wordmark, the "Fenix" name, the search bar, the "You're protected"
  status, and the tab counter, so both Fenix branding and 156.0 code are in effect.
- The full documentation gate passes: `Failures: 0`, `Known Failures: 443`, `build succeeded`, with no
  new warning suppression rules.
- APK: `Fenix-156.0-r1-arm64-v8a-release.apk`, size `131010652` bytes, SHA-256: `4435B8708E934F136853B5BF5D5A67AFC07B3BB27372448D069D306DFBAA7980`.
- Complete corresponding source: [fenix-156.0-r1](https://github.com/aenerv7/fenix/tree/fenix-156.0-r1). Fenix is an independent unofficial modified build, not sponsored or endorsed by Mozilla; this project provides maintenance and support. MPL 2.0 and third-party licenses are retained; Firefox is a trademark of the Mozilla Foundation.
- `.idsig` is retained locally for verification and re-signing and is not a GitHub Release asset; the Windows Glean native-library limitation still requires Linux or CI coverage.

## 155.0.1-r8

### 中文

官方上游基线：`FIREFOX-ANDROID_155_0_1_RELEASE`。本版本修复自定义标签页菜单的品牌图标，沿用 Firefox Android 155.0.1
上游修订，并保留 Fenix 相对于该基线的全部有效产品改动。

#### 有效 Fenix 改动

- 将自定义标签页菜单中「由 Fenix 驱动」左侧的 Firefox 图标替换为现有透明背景 Fenix 兔子图标；顶部和底部工具栏布局均使用相同资源，保留原有尺寸、间距和本地化文字。

- 修复点击地址栏网址或搜索建议时在群组外新建标签的问题：改用本次搜索的实时来源标签，来源仍存在时在原标签加载并保留群组关系；来源缺失或已关闭时才按原规则新建，保留隐私模式和主页作为新标签的设置行为。

- 使用 `github.aenerv7.fenix` application ID、Fenix 名称和兔子品牌，保留必要的 Mozilla/Firefox 上游与许可证说明；Focus 不在构建范围内。
- 补全简体中文和官方 Android Gecko 多语言资源；关于页、更新链接、搜索组件、启动器和 Fenix Labs 使用 Fenix 品牌。
- 启动器透明前景缩放至 80%；应用内徽标无背景，主页和关于页显示 Fenix wordmark。搜索小组件保持横向预览并独立预留图标、文字和麦克风区域；默认浏览器提示移除 Mozilla 图片及占位，关闭按钮右侧居中；Labs 欢迎横幅仅保留文字，空状态保留独立徽标。
- 关于页保留本地化的维护者及上游署名，支持、隐私与权利、更新链接指向 Fenix 项目；显示第 8 次修改，但不改变 Android 包版本。移动书签根目录显示为“移动收藏夹”。
- 移除密码、地址、信用卡等个人信息管理入口、自动填充服务和默认同步范围；移除 Google Play 评分集成及已结束的 Sports/World Cup 活动。
- 同时移除相关快捷方式、Intent、设置索引、后台初始化和维护，禁用登录自动填充；移除评分提示、SDK、回退及相关遥测。限时活动的状态、逻辑、测试、字符串和专用资源一并移除，保留无关的同名搜索设置。
- 保留 IP Protection 入口，默认完成首次引导，并维持 Fenix 的隐私、商店和设置裁剪策略。
- IP Protection 的设置、主菜单、引导、状态与位置选择保持接通；移除已无生产引用的旧 PWA 第三次访问安装引导。
- 群组标签页打开链接默认留在原群组，支持群组范围多选、移出、删除撤销和空群组恢复；群组工具栏、菜单、返回行为和拖拽状态保持正确。
- 链接菜单支持在当前群组或新群组打开；可配置工具栏快捷方式创建的新标签及后续网址或搜索保留群组关系，主页提交在当前标签页完成。部分删除撤销恢复成员关系，全部删除撤销恢复空群组。
- 群组多选工具栏与内容宽度对齐，处理系统栏内边距、明暗主题、菜单方向、层级和动画；保留上游持续长按拖拽时序，不在手势回调中同步切换交互模式。使用上游顶部新建标签按钮，移除重复浮动按钮。
- 从群组标签页进入“全部标签页”时自动展开并定位到当前标签；群组弹层始终跳过半高状态直接全高打开，不再依据窗口尺寸或标签数量计算初始高度。
- 自动展开不播放打开动画，手动打开保留动画；把独立标签加入群组后保持目标群组可见且正确选中。
- 系统返回优先取消标签页或群组的长按选择并关闭对应工具栏；无选择时，若聚焦标签属于当前展开群组则显示该标签，否则收起群组；全部标签页根页直接显示聚焦标签。手动收起群组只关闭群组并清除选择，不打开标签页。群组内部在无选择时也接收系统返回事件。
- 关闭最后一个非群组标签页时清理快照、列表固定项和拖拽状态；新建标签页工具栏和搜索组件在手机、平板、横竖屏保持可用。

#### 发布与验证

- 仅发布 `arm64-v8a` APK，使用官方 155.0.1 多语言 GeckoView，严格沿用官方 `versionCode 2016182530`；未进行本地 GeckoView 编译或打包。
- 发布流程校验官方基线、ABI、99 个 Gecko locale（含 `zh-CN`）、`assets/omni.ja`、Gecko 原生库、application ID、版本、签名和校验和。
- `fenix:ktlint`、Release 构建及 lintVital 通过。已核对 Release 编译产物中两种工具栏布局均调用 Fenix 品牌组件，并检查最终 APK 的兔子资源、包名、版本、ABI、签名及校验和。本次仅替换图标，未新增或重跑单元测试；当前未连接 Android 设备，未做真机或模拟器界面验证。`mach try auto` 因缺少 Mozilla Auth0 登录授权未启动远端 CI，不计为通过。
- 本次不变更上游基线，沿用既有全量文档基线检查；本次启用 autodoc 的 Fenix 文档构建退出码 0、`Failures: 0`、`Known Failures: 15`，未新增警告屏蔽规则。
- APK：`Fenix-155.0.1-r8-arm64-v8a-release.apk`，大小 `130793410` 字节，SHA-256：`A722C2D351086A9572E0E641BCDE7BA334B039587E4B66F4C85936B63EC89A7D`。
- 对应完整源码：[fenix-155.0.1-r8](https://github.com/aenerv7/fenix/tree/fenix-155.0.1-r8)。Fenix 是非官方独立修改版，不受 Mozilla 赞助或背书；维护与支持由本项目提供。保留 MPL 2.0 和第三方许可；Firefox 是 Mozilla Foundation 的商标。
- `.idsig` 仅保留本地校验和重签名使用，不作为 GitHub Release 资产；Windows Glean 原生库限制仍需 Linux 或 CI 覆盖。

### English

Official upstream baseline: `FIREFOX-ANDROID_155_0_1_RELEASE`. This release fixes Custom Tab menu
branding while retaining the Firefox Android 155.0.1 baseline and every effective Fenix product
change relative to that baseline.

#### Effective Fenix changes

- Replaces the Firefox icon beside Powered by Fenix in the Custom Tab menu with the existing transparent Fenix rabbit. Top and bottom toolbar layouts share the same resource and retain the existing size, spacing, and localized text.

- Fixes URL/search suggestion clicks creating an ungrouped tab by using the current search source. Suggestions load in the existing source tab and preserve its group when the source still exists; missing or closed sources retain the new-tab fallback, with private mode and homepage-as-new-tab behavior preserved.

- Uses the `github.aenerv7.fenix` application ID, Fenix name, and rabbit branding while retaining required Mozilla/Firefox upstream and licensing references; Focus is outside the build scope.
- Completes Simplified Chinese and official Android Gecko locale resources; the About screen, What's New link, search widget, launcher, and Fenix Labs use Fenix branding.
- Scales the transparent launcher foreground to 80%; in-app marks are background-free, and home/About show the Fenix wordmark. The search widget keeps a horizontal preview with separate icon, text, and microphone areas. The default-browser prompt removes Mozilla artwork and its reserved space and centers the close button on the right. The Labs welcome banner is text-only, with a dedicated mark in its empty state.
- About retains localized maintainer/upstream attribution and points support, privacy/rights, and update links to Fenix. It displays modification number 8 without changing Android package versions. The mobile bookmarks root uses the Simplified Chinese label "移动收藏夹".
- Removes management entry points, autofill service, and default sync scope for passwords, addresses, credit cards, and other personal data; removes Google Play rating integration and the finished Sports/World Cup activity.
- Also removes related shortcuts, intents, settings indexing, background initialization and maintenance, and disables login autofill. Removes review prompts, SDK, fallbacks, and related telemetry. The retired activity's state, logic, tests, strings, and dedicated assets are removed while unrelated similarly named search settings remain.
- Keeps the IP Protection entry point, marks new installations onboarding-complete, and preserves Fenix privacy, store, and settings reductions.
- IP Protection settings, main menu, onboarding, status, and location picker remain connected. Removes the obsolete, unreferenced third-visit PWA installation onboarding.
- Keeps grouped-tab links in their group by default and supports group-scoped selection, remove, delete undo, and empty-group restoration; group toolbars, menus, Back behavior, and drag state remain consistent.
- Link menus support opening in the current or a new group. New tabs from the configurable toolbar shortcut and subsequent URLs/searches retain their group relationship; homepage submissions stay in the current tab. Partial-delete undo restores membership, and undo after deleting all members restores the group.
- The group selection toolbar matches content width and handles system-bar insets, light/dark themes, menu direction, layering, and animation. Preserves upstream continued long-press dragging without switching interaction mode synchronously in gesture callbacks. Uses the upstream top-toolbar new-tab action and removes the duplicate floating button.
- Opening All Tabs from a grouped tab expands and locates the current tab; group sheets always skip the half-expanded state and open fully, with no window-size or tab-count height calculation.
- Automatic group expansion skips the opening animation, while manual opening retains it. Moving a standalone tab into a group keeps the destination visible and correctly selected.
- System Back first clears non-empty tab/group selection and its toolbar. With no selection, it shows the focused tab if that tab belongs to the expanded group, or collapses the group otherwise; root Back shows the focused tab. Manual dismissal only collapses the group and clears selection, without opening a tab. Group content handles system Back even with no selection.
- Clearing the last ungrouped tab removes its snapshot, pinned list item, and drag state; the group new-tab toolbar and search widget remain usable on phones, tablets, portrait, and landscape.

#### Release and validation

- Publishes only the `arm64-v8a` APK using the official 155.0.1 multi-locale GeckoView and exact official `versionCode 2016182530`; no local GeckoView compilation or packaging was performed.
- The release process verifies the official baseline, ABI, all 99 Gecko locales (including `zh-CN`), `assets/omni.ja`, Gecko native libraries, application ID, version, signature, and checksums.
- `fenix:ktlint`, the Release build, and lintVital passed. Release bytecode confirms both toolbar layouts call the Fenix-branded component; the final APK rabbit resource, package name, version, ABI, signature, and checksum were checked. This icon-only change adds no tests and does not rerun unit tests. No Android device was connected, so no physical-device or emulator UI verification was performed. `mach try auto` could not start remote CI without Mozilla Auth0 authorization and is not counted as passing.
- The upstream baseline is unchanged and retains the existing full documentation baseline gate. The autodoc-enabled Fenix documentation build for this revision exited 0 with `Failures: 0` and `Known Failures: 15`; no warning suppression rules were added.
- APK: `Fenix-155.0.1-r8-arm64-v8a-release.apk`, size `130793410` bytes, SHA-256: `A722C2D351086A9572E0E641BCDE7BA334B039587E4B66F4C85936B63EC89A7D`.
- Complete corresponding source: [fenix-155.0.1-r8](https://github.com/aenerv7/fenix/tree/fenix-155.0.1-r8). Fenix is an independent unofficial modified build, not sponsored or endorsed by Mozilla; this project provides maintenance and support. MPL 2.0 and third-party licenses are retained; Firefox is a trademark of the Mozilla Foundation.
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
- 本次仅改动 Fenix Kotlin、资源和构建校验逻辑，复用已验证的 155.0 多语言 GeckoView。现行发布约定要求上游包匹配基线、Gecko 修订、ABI、完整语言集和原生库 SHA-256；校验失败必须停止，禁止回退本地编译或打包。

### English

Upstream baseline: `FIREFOX-ANDROID_155_0_RELEASE`

#### Tab groups

- Fixed stale display state after closing the last ungrouped tab while an open group remains; both the data source and list interaction state are now cleared.
- Fixed duplicate navigation from the expanded-group long-press toolbar: the first Back press closes only the selection toolbar and does not collapse the group.

#### Branding and release

- The launcher now uses a transparent rabbit foreground scaled to 80% of its original size, fixing the missing launcher icon; the background-free in-app rabbit resource is unchanged.
- Published the `arm64-v8a` APK only and kept the upstream `versionCode 2016180970`.
- `.idsig` is retained locally for verification and is not published as a GitHub Release asset.
- This revision changes only Fenix Kotlin, resources, and build validation logic, so it reuses the validated 155.0 multi-locale GeckoView package. Current release policy requires the upstream package to match the baseline, Gecko revision, ABI, complete locale set, and native-library SHA-256. Failed validation must stop the release; falling back to local compilation or packaging is prohibited.

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
