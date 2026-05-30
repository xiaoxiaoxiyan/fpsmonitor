# Tasks

- [ ] Task 1: 主页 UI 重构 - KernelSU 风格
  - [ ] SubTask 1.1: 重写 HomeScreen，实现 KernelSU 最新版本风格的状态卡片（无 Root 显示红色叉号，有 Root 显示绿色勾号 + MIUI X 风格卡片）
  - [ ] SubTask 1.2: 新增授权卡片组件，检测 KernelSU/Magisk/APatch 授权方式及版本号，显示工作状态
  - [ ] SubTask 1.3: 完善 RootMethod 检测逻辑，支持 KernelSU/Magisk/APatch 三种 Root 方案识别
  - [ ] SubTask 1.4: 更新 HomeViewModel，整合授权信息、SuperUser 数量等数据

- [ ] Task 2: 脚本模块完善
  - [ ] SubTask 2.1: 完善 ScriptDownloader 云端脚本列表获取和下载逻辑
  - [ ] SubTask 2.2: 完善 ScriptRunner 执行逻辑，支持实时输出流
  - [ ] SubTask 2.3: 完善 ScriptViewModel，实现下载状态管理、执行状态管理
  - [ ] SubTask 2.4: 完善 ScriptScreen UI，修复输出区域显示

- [ ] Task 3: SUWebUI 集成
  - [ ] SubTask 3.1: 完善 WebUIActivity，实现 KernelSU WebUI X 规范的 WebView 加载
  - [ ] SubTask 3.2: 完善 ModuleScreen 中 WebUI 按钮点击逻辑
  - [ ] SubTask 3.3: 实现 ModuleScanner 扫描已安装模块及其 WebUI 路径

- [ ] Task 4: 文件管理器完善（MT 管理器风格）
  - [ ] SubTask 4.1: 完善 FileOperationEngine 所有文件操作（复制/移动/删除/重命名/新建/权限修改）
  - [ ] SubTask 4.2: 实现 ArchiveHelper 压缩/解压完整逻辑（ZIP/TAR.GZ/TAR）
  - [ ] SubTask 4.3: 完善 FileManagerViewModel，实现剪贴板、多选、活动面板切换
  - [ ] SubTask 4.4: 完善 FileManagerScreen 双栏布局交互，修复文件点击/导航逻辑
  - [ ] SubTask 4.5: 实现文件多选模式和批量操作

- [ ] Task 5: 终端完善
  - [ ] SubTask 5.1: 完善 TerminalEngine 多会话管理（新建/切换/关闭）
  - [ ] SubTask 5.2: 完善 TerminalViewModel 多 Tab 管理和命令执行
  - [ ] SubTask 5.3: 完善 TerminalScreen UI，确保 ANSI 颜色解析和快捷键正常工作

- [ ] Task 6: 更多功能筛选与实现
  - [ ] SubTask 6.1: 精简 MoreScreen 功能列表，移除无开源项目支撑的功能项
  - [ ] SubTask 6.2: 修复 MoreScreen 网格布局文字显示不全问题（改为列表布局或调整卡片）
  - [ ] SubTask 6.3: 实现 AK3 内核刷写功能页面（参考 AnyKernel3）
  - [ ] SubTask 6.4: 实现分区备份/恢复功能页面（dd 命令）
  - [ ] SubTask 6.5: 实现过检测工具箱功能页面（PlayIntegrityFix + TrickyStore 配置管理）
  - [ ] SubTask 6.6: 实现隐藏应用功能页面（参考 Hide My Applist）
  - [ ] SubTask 6.7: 实现设备清理/改 ID 功能页面（参考 DeviceIDChanger）
  - [ ] SubTask 6.8: 实现密钥配置功能页面（TrickyStore keybox.xml 管理）

- [ ] Task 7: 自动权限获取
  - [ ] SubTask 7.1: 在 App.kt 或 MainActivity 中实现 Root 环境自动授予存储权限
  - [ ] SubTask 7.2: 在 AndroidManifest.xml 中确认所有必要权限声明

- [ ] Task 8: APK 重打包签名
  - [ ] SubTask 8.1: 实现 ApkSigner 工具类，支持 APK 解压/修改/重打包/签名
  - [ ] SubTask 8.2: 在文件管理器中为 APK 文件添加"重签名"操作

- [ ] Task 9: 编译验证
  - [ ] SubTask 9.1: 确保项目在沙箱环境中可以成功编译生成 APK

# Task Dependencies
- Task 1 (主页 UI) 独立，可先执行
- Task 2 (脚本) 独立，可与 Task 1 并行
- Task 3 (SUWebUI) 依赖 Task 1 中的 Root 检测逻辑
- Task 4 (文件管理器) 独立，可与其他任务并行
- Task 5 (终端) 独立，可与其他任务并行
- Task 6 (更多功能) 依赖 Task 1 中的 Root 检测，SubTask 6.3-6.8 可并行
- Task 7 (自动权限) 依赖 Task 1 中的 Root 检测
- Task 8 (APK 签名) 依赖 Task 4 (文件管理器)
- Task 9 (编译验证) 依赖所有其他任务完成
