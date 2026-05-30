# XToolbox 全功能完善 Spec

## Why
当前 XToolbox 项目仅为空壳，各功能页面只有 UI 骨架，核心逻辑未实现。需要基于 KernelSU 最新版本（v4.0+，SukiSU-Ultra 风格）UI 重新设计主页，使用 MIUI X Compose UI 库，并完善所有功能模块的实际实现。每个功能必须对应到 GitHub 上可查证的开源项目，无对应开源项目的功能直接舍弃。

## What Changes
- **UI 框架升级**：引入 MIUI X Compose UI 库（`top.yukonga.miuix.kmp:miuix-ui-android`），替换 Material3 为 MIUI X 风格
- **主页 UI 重构**：照抄 KernelSU v4.0+（SukiSU-Ultra）主页风格，无 Root 显示红色叉号状态，有 Root 显示 MIUI X 风格状态卡片
- **授权卡片**：新增授权方式及工作状态卡片（KernelSU/Magisk/APatch 检测 + 工作模式显示）
- **脚本模块完善**：从云端下载脚本 → 本地缓存 → 选择执行，完整流程实现
- **SUWebUI 集成**：集成 KernelSU 模块 WebUI X 功能，支持模块的 Web 界面
- **文件管理器完善**：MT 管理器风格双栏布局，实现压缩/解压/复制/移动/删除/权限修改等完整操作
- **终端完善**：基于 libsu 的 root 终端，多会话、ANSI 颜色、快捷键
- **更多功能筛选**：对 MoreScreen 中的功能逐一在 GitHub 搜索对应开源项目，有则实现，无则移除
- **自动权限获取**：有 Root 权限时自动授予所有存储权限
- **APK 签名**：Root 环境下修改 APK 后重新打包签名
- **竞品显示修复**：修复 MoreScreen 网格布局文字显示不全的问题

## Impact
- Affected specs: 所有现有功能模块
- Affected code:
  - `app/src/main/java/com/xtoolbox/ui/` - 全部 UI 层需从 Material3 迁移到 MIUI X
  - `app/src/main/java/com/xtoolbox/ui/theme/` - 主题系统重写
  - `app/src/main/java/com/xtoolbox/ui/screen/home/` - 主页重构
  - `app/src/main/java/com/xtoolbox/ui/screen/script/` - 脚本模块
  - `app/src/main/java/com/xtoolbox/ui/screen/module/` - 模块管理 + WebUI
  - `app/src/main/java/com/xtoolbox/ui/screen/filemanager/` - 文件管理器
  - `app/src/main/java/com/xtoolbox/ui/screen/terminal/` - 终端
  - `app/src/main/java/com/xtoolbox/ui/screen/more/` - 更多功能
  - `app/src/main/java/com/xtoolbox/core/` - 所有核心逻辑
  - `app/src/main/java/com/xtoolbox/ui/navigation/` - 导航栏 MIUI X 风格
  - `app/src/main/AndroidManifest.xml` - 权限声明
  - `app/build.gradle.kts` - 依赖（添加 miuix 库）

## ADDED Requirements

### Requirement: MIUI X UI 框架
系统 SHALL 使用 MIUI X Compose UI 库作为主要 UI 框架：
- 引入 `top.yukonga.miuix.kmp:miuix-ui-android` 依赖（最新版本 v0.8.8）
- 使用 MiuixTheme 替换 Material3 主题
- 使用 MIUI X 风格的 Scaffold、Card、NavigationBar 等组件
- 支持深色/浅色主题自动切换
- 参考：https://github.com/miuix-kotlin-multiplatform/miuix

参考开源项目：miuix (https://github.com/miuix-kotlin-multiplatform/miuix) - MIUI X 风格 Compose UI 库

#### Scenario: MIUI X 主题应用
- **WHEN** 应用启动
- **THEN** 使用 MiuixTheme 渲染所有界面，组件风格为 MIUI X

### Requirement: KernelSU v4.0+ 风格主页 UI
系统 SHALL 提供照抄 KernelSU v4.0+（SukiSU-Ultra）主页的 UI 设计：
- 无 Root 时：显示红色叉号图标 + "未 Root" 文字 + 提示信息，整个页面为空壳状态
- 有 Root 时：显示 MIUI X 风格的状态卡片，包含：
  - Root 方式（KernelSU / Magisk / APatch）及版本号
  - 工作模式（GKI / LKM）
  - 内核版本
  - Android 版本及 API Level
  - 安全补丁日期
  - SuSFS 状态（如可用）
- 下方快捷操作卡片（重启/软重启/Recovery/Bootloader）
- 下方设备信息卡片
- 整体使用 MIUI X 风格的卡片、圆角、配色

参考开源项目：
- SukiSU-Ultra (https://github.com/SukiSU-Ultra/SukiSU-Ultra) - KSU v4.0+ 分支，MIUI X 主题
- KernelSU-Next (https://github.com/KernelSU-Next/KernelSU-Next) - KSU Next 分支

#### Scenario: 无 Root 授权
- **WHEN** 用户打开应用且设备未 Root
- **THEN** 主页显示红色叉号状态卡片，文字为"未 Root"，提示"未检测到 Root 权限，部分功能不可用"，所有需要 Root 的功能按钮置灰

#### Scenario: 有 Root 授权
- **WHEN** 用户打开应用且设备已 Root
- **THEN** 主页显示绿色勾号 MIUI X 风格状态卡片，显示 Root 方式、工作模式、内核版本等信息，所有功能可用

### Requirement: 授权卡片
系统 SHALL 在主页提供授权卡片，显示：
- 当前 Root 授权方式（KernelSU / KernelSU-Next / SukiSU-Ultra / Magisk / APatch / 未知）
- 授权方式版本号
- 工作状态（正常工作 / 未安装 / 异常）
- SuperUser 数量统计
- suCompat 状态（如为 KernelSU 系列）

#### Scenario: 检测到 KernelSU 系列
- **WHEN** 设备使用 KernelSU / KernelSU-Next / SukiSU-Ultra 作为 Root 方案
- **THEN** 授权卡片显示对应名称图标、版本号、工作状态为"正常"、suCompat 状态

#### Scenario: 检测到 Magisk
- **WHEN** 设备使用 Magisk 作为 Root 方案
- **THEN** 授权卡片显示"Magisk"图标、版本号、工作状态

#### Scenario: 检测到 APatch
- **WHEN** 设备使用 APatch 作为 Root 方案
- **THEN** 授权卡片显示"APatch"图标、版本号、工作状态

### Requirement: 脚本云端下载与执行
系统 SHALL 提供完整的脚本管理功能：
- 从配置的云端服务器获取脚本列表
- 支持全部下载和单个下载
- 下载后本地缓存，显示下载状态
- 选择脚本执行，实时显示执行输出
- 执行完成后显示退出码

#### Scenario: 下载并执行脚本
- **WHEN** 用户点击"全部更新"按钮
- **THEN** 系统从云端下载所有脚本到本地缓存目录
- **WHEN** 用户点击某脚本的"执行"按钮
- **THEN** 系统以 Root 权限执行该脚本，实时显示输出

### Requirement: SUWebUI X 集成
系统 SHALL 集成 KernelSU 模块的 WebUI X 功能：
- 模块列表中，有 WebUI 的模块显示"WebUI"按钮
- 点击按钮打开 WebView Activity 加载模块的 WebUI 页面
- 支持 KernelSU WebUI X 规范（基于 WebView 的模块管理界面）
- WebView 与 Native 层交互（KSU 接口桥接）

参考开源项目：
- KernelSU (https://github.com/tiann/KernelSU) - manager 模块中的 WebUI 实现
- KernelSU-Next (https://github.com/KernelSU-Next/KernelSU-Next) - WebUIActivity + WebViewInterface 实现

#### Scenario: 打开模块 WebUI
- **WHEN** 用户点击已安装模块的"WebUI"按钮
- **THEN** 系统打开 WebView Activity，加载该模块的 WebUI 页面，支持 Native 桥接

### Requirement: MT 管理器风格双栏文件管理器
系统 SHALL 提供类似 MT 管理器的双栏文件管理器：
- 左右双栏布局，可独立导航
- 支持文件/文件夹操作：复制、移动、删除、重命名、新建文件夹
- 支持压缩（ZIP）和解压（ZIP/TAR.GZ/TAR）
- 支持权限修改
- 支持 Root 文件系统访问（通过 libsu）
- 文件多选操作
- 粘贴板功能
- APK 文件右键菜单增加"重签名"选项

参考开源项目：AmazeFileManager (https://github.com/TeamAmaze/AmazeFileManager) - 双窗格文件管理器实现

#### Scenario: 压缩文件
- **WHEN** 用户选择文件/文件夹后点击压缩按钮
- **THEN** 系统将选中项压缩为 ZIP 文件到当前目录

#### Scenario: 解压文件
- **WHEN** 用户点击压缩文件
- **THEN** 系统解压该文件到当前目录

### Requirement: Root 终端
系统 SHALL 提供完整的 Root 终端：
- 基于 libsu 的 Root Shell
- 多会话管理（新建/切换/关闭）
- ANSI 颜色解析
- 字体缩放
- 快捷键（Ctrl+C、Ctrl+D、Tab、历史、clear）
- 命令输入与执行

参考开源项目：Termux (https://github.com/termux/termux-app) - 终端模拟器实现思路

#### Scenario: 执行 Root 命令
- **WHEN** 用户在终端输入命令并按发送
- **THEN** 系统以 Root 权限执行命令，实时显示输出，支持 ANSI 颜色

### Requirement: 更多功能筛选与实现
系统 SHALL 对 MoreScreen 中的功能逐一验证，仅保留有对应开源项目的功能：

**保留的功能（有对应开源项目）：**
1. **AK3 内核刷写** → 参考 AnyKernel3 (https://github.com/osm0sis/AnyKernel3) - 内核刷入脚本模板
2. **分区备份** → 通过 dd 命令实现分区镜像备份/恢复（无需外部项目，libsu 即可实现）
3. **过检测工具箱** → 参考 PlayIntegrityFix (https://github.com/chiteroman/PlayIntegrityFix) + TrickyStore (https://github.com/5ec1cff/TrickyStore)
4. **隐藏应用** → 参考 Hide My Applist (https://github.com/Dr-TSNG/Hide-My-Applist) - 应用列表隐藏
5. **设备清理/改 ID** → 参考 DeviceIDChanger (https://github.com/sidex15/deviceidchanger) - SSAID/DeviceID 修改
6. **密钥配置** → 参考 TrickyStore (https://github.com/5ec1cff/TrickyStore) - keybox.xml 配置管理
7. **模块管理** → 已有 ModuleScreen，完善安装/卸载/开关功能

**移除的功能（无对应开源项目或无法在 APK 内实现）：**
- ~~调度中心~~ → 无对应开源项目，移除
- ~~伪装机型~~ → 无完整开源项目（PlayIntegrityFix 部分覆盖，但独立伪装机型功能无开源项目），移除
- ~~刷机工具~~ → ADB/Fastboot 需要PC端配合，无法在 APK 内独立实现，移除
- ~~DTBO 工具箱~~ → 无对应开源项目，移除
- ~~剪贴板解锁~~ → 无对应开源项目，移除
- ~~游戏清理~~ → 无对应开源项目，移除
- ~~TG 过验证~~ → 无对应开源项目，移除
- ~~资源下载~~ → 无对应开源项目，移除
- ~~一键隐藏~~ → 被 Hide My Applist 功能覆盖，移除

#### Scenario: 功能筛选
- **WHEN** 用户进入"更多功能"页面
- **THEN** 仅显示有对应开源项目的功能项，每个功能可点击进入对应操作界面

### Requirement: 自动权限获取
系统 SHALL 在检测到 Root 权限时自动授予所有存储权限：
- 通过 Root Shell 执行 `pm grant` 命令授予 READ_EXTERNAL_STORAGE、WRITE_EXTERNAL_STORAGE、MANAGE_EXTERNAL_STORAGE 权限
- 无需用户手动授权
- 修改后重新打包并签名

#### Scenario: Root 环境自动授权
- **WHEN** 应用检测到 Root 权限且存储权限未授予
- **THEN** 系统自动通过 Root Shell 授予所有存储权限

### Requirement: APK 修改后重打包签名
系统 SHALL 提供在 Root 环境下修改 APK 后重新打包签名的功能：
- 解压 APK → 修改内容 → 重新打包 → 签名
- 使用 apksigner 或自定义签名逻辑
- 集成到文件管理器中，对 APK 文件提供"重打包签名"操作

#### Scenario: 修改并重签名 APK
- **WHEN** 用户在文件管理器中选择 APK 文件并点击"重签名"
- **THEN** 系统解压 APK，使用内置签名密钥重新签名，生成新的 APK 文件

### Requirement: 竞品显示修复
系统 SHALL 修复 MoreScreen 网格布局中功能卡片文字显示不全的问题：
- 改用 MIUI X 风格的列表布局（BasicComponent）替代 Grid 布局
- 确保功能名称和描述完整显示
- 每个功能项显示图标 + 名称 + 描述 + 箭头

#### Scenario: 功能卡片完整显示
- **WHEN** 用户进入"更多功能"页面
- **THEN** 每个功能项的名称和描述完整显示，不被截断，使用 MIUI X 列表风格

## MODIFIED Requirements

### Requirement: UI 框架（原 Material3）
原 Material3 主题和组件全部替换为 MIUI X 风格（miuix 库），包括 Scaffold、Card、NavigationBar、Button 等。

### Requirement: 主页 UI（原 HomeScreen）
原 HomeScreen 的简单状态卡片替换为 KernelSU v4.0+（SukiSU-Ultra）风格的完整状态卡片，使用 MIUI X 组件，增加授权方式检测和工作状态显示。

### Requirement: 导航栏（原 MainNavigation）
原 Material3 NavigationBar 替换为 MIUI X 风格底部导航栏。

### Requirement: 文件管理器（原 FileManagerScreen）
原双栏布局保留，但需完善所有文件操作的实际实现（原为 TODO 状态），增加压缩/解压功能，UI 迁移到 MIUI X 风格。

### Requirement: 终端（原 TerminalScreen）
原终端基本框架保留，完善多会话管理和命令执行逻辑，UI 迁移到 MIUI X 风格。

### Requirement: 更多功能（原 MoreScreen）
从 16 个功能项精简为 7 个有开源项目支撑的功能项，布局从 Grid 改为 MIUI X 列表风格，每个功能需实现对应的操作界面。

## REMOVED Requirements

### Requirement: Material3 主题
**Reason**: 替换为 MIUI X 风格（miuix 库）
**Migration**: 使用 MiuixTheme 和 MIUI X 组件

### Requirement: 调度中心
**Reason**: 无对应开源项目
**Migration**: 直接移除，不提供替代

### Requirement: 伪装机型
**Reason**: 无完整独立开源项目
**Migration**: PlayIntegrityFix 的指纹伪装功能部分覆盖此需求

### Requirement: 刷机工具（ADB/Fastboot）
**Reason**: 需要PC端配合，无法在 APK 内独立实现
**Migration**: 直接移除

### Requirement: DTBO 工具箱
**Reason**: 无对应开源项目
**Migration**: 直接移除

### Requirement: 剪贴板解锁
**Reason**: 无对应开源项目
**Migration**: 直接移除

### Requirement: 游戏清理
**Reason**: 无对应开源项目
**Migration**: 直接移除

### Requirement: TG 过验证
**Reason**: 无对应开源项目
**Migration**: 直接移除

### Requirement: 资源下载
**Reason**: 无对应开源项目
**Migration**: 直接移除

### Requirement: 一键隐藏
**Reason**: 被 Hide My Applist 功能覆盖
**Migration**: 使用"隐藏应用"功能替代
