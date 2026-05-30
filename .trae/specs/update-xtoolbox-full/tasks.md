# Tasks

- [ ] Task 1: MIUI X UI 框架集成
  - [ ] SubTask 1.1: 在 build.gradle.kts 中添加 miuix 依赖（`top.yukonga.miuix.kmp:miuix-ui-android`）
  - [ ] SubTask 1.2: 重写 Theme.kt，使用 MiuixTheme 替换 Material3 主题
  - [ ] SubTask 1.3: 重写 MainNavigation.kt，使用 MIUI X 风格底部导航栏
  - [ ] SubTask 1.4: 更新 MainActivity.kt 和 App.kt，应用 MiuixTheme

- [ ] Task 2: 主页 UI 重构 - KernelSU v4.0+ 风格
  - [ ] SubTask 2.1: 重写 HomeScreen，使用 MIUI X 组件实现 KernelSU v4.0+（SukiSU-Ultra）风格状态卡片
  - [ ] SubTask 2.2: 新增授权卡片组件，检测 KernelSU/KernelSU-Next/SukiSU-Ultra/Magisk/APatch 授权方式及版本号
  - [ ] SubTask 2.3: 完善 RootMethod 检测逻辑，支持 KSU 系列/Magisk/APatch 识别及版本获取
  - [ ] SubTask 2.4: 更新 HomeViewModel，整合授权信息、SuperUser 数量、suCompat 状态等数据
  - [ ] SubTask 2.5: 无 Root 时显示红色叉号空壳状态，有 Root 时显示绿色勾号 MIUI X 卡片

- [ ] Task 3: 脚本模块完善
  - [ ] SubTask 3.1: 完善 ScriptDownloader 云端脚本列表获取和下载逻辑
  - [ ] SubTask 3.2: 完善 ScriptRunner 执行逻辑，支持实时输出流
  - [ ] SubTask 3.3: 完善 ScriptViewModel，实现下载状态管理、执行状态管理
  - [ ] SubTask 3.4: 重写 ScriptScreen UI，迁移到 MIUI X 风格，修复输出区域显示

- [ ] Task 4: SUWebUI X 集成
  - [ ] SubTask 4.1: 重写 WebUIActivity，实现 KernelSU WebUI X 规范的 WebView 加载和 Native 桥接
  - [ ] SubTask 4.2: 完善 ModuleScreen 中 WebUI 按钮点击逻辑，迁移到 MIUI X 风格
  - [ ] SubTask 4.3: 实现 ModuleScanner 扫描已安装模块及其 WebUI 路径

- [ ] Task 5: 文件管理器完善（MT 管理器风格）
  - [ ] SubTask 5.1: 完善 FileOperationEngine 所有文件操作（复制/移动/删除/重命名/新建/权限修改）
  - [ ] SubTask 5.2: 实现 ArchiveHelper 压缩/解压完整逻辑（ZIP/TAR.GZ/TAR）
  - [ ] SubTask 5.3: 完善 FileManagerViewModel，实现剪贴板、多选、活动面板切换
  - [ ] SubTask 5.4: 重写 FileManagerScreen UI，迁移到 MIUI X 风格双栏布局
  - [ ] SubTask 5.5: 实现文件多选模式和批量操作

- [ ] Task 6: 终端完善
  - [ ] SubTask 6.1: 完善 TerminalEngine 多会话管理（新建/切换/关闭）
  - [ ] SubTask 6.2: 完善 TerminalViewModel 多 Tab 管理和命令执行
  - [ ] SubTask 6.3: 重写 TerminalScreen UI，迁移到 MIUI X 风格

- [ ] Task 7: 更多功能筛选与实现
  - [ ] SubTask 7.1: 精简 MoreScreen 功能列表为 7 项，移除无开源项目支撑的功能
  - [ ] SubTask 7.2: 重写 MoreScreen 布局，从 Grid 改为 MIUI X 列表风格（BasicComponent），修复文字显示不全
  - [ ] SubTask 7.3: 实现 AK3 内核刷写功能页面（参考 AnyKernel3）
  - [ ] SubTask 7.4: 实现分区备份/恢复功能页面（dd 命令）
  - [ ] SubTask 7.5: 实现过检测工具箱功能页面（PlayIntegrityFix + TrickyStore 配置管理）
  - [ ] SubTask 7.6: 实现隐藏应用功能页面（参考 Hide My Applist）
  - [ ] SubTask 7.7: 实现设备清理/改 ID 功能页面（参考 DeviceIDChanger）
  - [ ] SubTask 7.8: 实现密钥配置功能页面（TrickyStore keybox.xml 管理）

- [ ] Task 8: 自动权限获取
  - [ ] SubTask 8.1: 在 App.kt 或 MainActivity 中实现 Root 环境自动授予存储权限
  - [ ] SubTask 8.2: 在 AndroidManifest.xml 中确认所有必要权限声明

- [ ] Task 9: APK 重打包签名
  - [ ] SubTask 9.1: 实现 ApkSigner 工具类，支持 APK 解压/修改/重打包/签名
  - [ ] SubTask 9.2: 在文件管理器中为 APK 文件添加"重签名"操作

- [ ] Task 10: 编译验证
  - [ ] SubTask 10.1: 确保项目在沙箱环境中可以成功编译生成 APK

# Task Dependencies
- Task 1 (MIUI X 框架) 必须先完成，所有其他 UI 任务依赖此任务
- Task 2 (主页 UI) 依赖 Task 1
- Task 3 (脚本) 依赖 Task 1
- Task 4 (SUWebUI) 依赖 Task 1 和 Task 2 中的 Root 检测逻辑
- Task 5 (文件管理器) 依赖 Task 1
- Task 6 (终端) 依赖 Task 1
- Task 7 (更多功能) 依赖 Task 1，SubTask 7.3-7.8 可并行
- Task 8 (自动权限) 依赖 Task 2 中的 Root 检测
- Task 9 (APK 签名) 依赖 Task 5 (文件管理器)
- Task 10 (编译验证) 依赖所有其他任务完成
