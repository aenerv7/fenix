# Fenix

<p align="center">
  <img src="mobile/android/fenix/app/src/release/res/mipmap-xxxhdpi/ic_launcher.webp"
       alt="Fenix 白色凤眼海棠侏儒兔图标" width="128">
</p>

Fenix 是一个面向个人使用的 Android 浏览器 QoL 优化 fork。项目以 Mozilla 发布的
Firefox Android 正式版标签为不可变基线，在每个正式版之上维护一组范围明确、可审查、
可迁移的补丁。

- 当前上游基线：`FIREFOX-ANDROID_154_0_1_RELEASE`
- 应用名称：`Fenix`
- Android application ID：`github.aenerv7.fenix`
- 正式版品牌：白色凤眼海棠侏儒兔图标与 `Fenix` wordmark

> Fenix 是独立维护的非官方项目，未获得 Mozilla Foundation 或其关联方的赞助、认可或
> 背书。本仓库中对 Firefox、Mozilla 和 GeckoView 的文字引用仅用于如实说明上游来源和
> 技术兼容关系。

## QoL 改动

- 使用独立兔子品牌替换正式版 launcher、圆形图标、adaptive icon 和主题图标，并在
  新标签页左上角及关于页恢复“兔子 + Fenix”wordmark。
- 补全本版本缺失的简体中文本地化，包括标签页群组和移动收藏夹相关界面。
- 从应用 UI、Android 自动填充服务和默认同步数据范围中移除密码、地址、信用卡及个人
  信息管理功能；系统自动填充提供方列表中不再提供 Fenix。
- 调整标签页群组打开链接的行为：群组页面产生的新标签默认留在当前群组，并提供明确的
  “在标签页群组内打开”或“新建标签页群组打开”长按菜单项。
- 为群组内标签页提供作用域隔离的多选操作、移出群组、删除撤销及空群组恢复逻辑。
- 优化群组多选工具栏、系统导航栏、亮暗主题和溢出菜单的布局、层级与动画。
- 仅构建 Fenix；本项目不构建或发布 Firefox Focus。

详细变更见 [FENIX_CHANGELOG.md](FENIX_CHANGELOG.md)。

## 开发

本项目使用仓库内工具目录，避免把 Android SDK、JDK、Gradle、Cargo、AVD 和构建产物
写到用户全局目录。Windows 下的最小流程：

```powershell
.\tools\fenix\mach-local.ps1 bootstrap
.\tools\fenix\mach-local.ps1 gradle fenix:assembleDebug
```

完整的环境、测试、虚拟机和签名说明见
[FENIX_DEVELOPMENT.md](FENIX_DEVELOPMENT.md)。

### 代码搜索（重要）

`searchfox-cli` 固定安装在仓库的 `searchfox-cli/` 目录，不加入系统 PATH。无论人工操作还是
新的开发代理会话，都应从仓库根目录使用统一包装器：

```powershell
.\tools\fenix\searchfox-local.ps1 --help
.\tools\fenix\searchfox-local.ps1 --path 'mobile/android/*' -q 'TabsTray'
```

首次安装、缓存位置和标识符搜索示例见
[tools/fenix/README.md](tools/fenix/README.md)。根目录 [AGENTS.md](AGENTS.md) 也明确要求后续
会话使用这个入口。

## 同步上游正式版

项目不跟随上游每日开发分支。每次升级都从指定的
`FIREFOX-ANDROID_<VERSION>_RELEASE` 正式版标签建立候选分支，再把 QoL 提交重放到该
标签之上：

```powershell
.\tools\fenix\sync-official-release.ps1 -Version 155.0
```

脚本不会自动发布。冲突处理、验证、合并和打标签步骤见
[FENIX_UPSTREAM_SYNC.md](FENIX_UPSTREAM_SYNC.md)。

## 许可、商标与源码

本仓库保留上游文件的原始许可声明。Firefox 源码主体使用 Mozilla Public License 2.0，
但仓库内部分第三方组件可能使用各自许可，具体以文件头、`LICENSE` 和
`toolkit/content/license.html` 为准。Fenix 对 MPL 覆盖文件的修改同样按 MPL 2.0
提供。

公开分发二进制时，必须向接收者提供对应版本的完整源码位置和精确提交或标签。Mozilla
商标权不由 MPL 授予；修改版二进制不得使用会使人误认为是 Mozilla 官方产品的名称、
图标或其他品牌元素。发布前必须执行 [FENIX_LICENSING.md](FENIX_LICENSING.md) 中的
检查清单。

- [Mozilla Public License 2.0](https://www.mozilla.org/MPL/2.0/)
- [Mozilla MPL 2.0 FAQ](https://www.mozilla.org/MPL/2.0/FAQ/)
- [Mozilla Trademark Guidelines](https://www.mozilla.org/foundation/trademarks/policy/)
- [Mozilla Distribution Policy](https://www.mozilla.org/foundation/trademarks/distribution-policy/)
- [Mozilla Firefox 官方源码仓库](https://github.com/mozilla-firefox/firefox)

Firefox 是 Mozilla Foundation 在美国及其他国家或地区的商标。
