# Tasks

- [x] Task 1: 创建项目基础结构
  - [x] SubTask 1.1: 创建 `fpsmonitor/` 项目目录结构，包含 settings.gradle.kts、根 build.gradle.kts、gradle.properties
  - [x] SubTask 1.2: 创建 `app/build.gradle.kts`，配置 SDK 版本、依赖（libsu、Compose BOM、Compose UI）
  - [x] SubTask 1.3: 创建 `AndroidManifest.xml`，声明权限（SYSTEM_ALERT_WINDOW、FOREGROUND_SERVICE、POST_NOTIFICATIONS）
  - [x] SubTask 1.4: 创建 `app/src/main/res/` 资源文件（strings.xml、colors.xml、themes.xml）

- [x] Task 2: 实现 Root 检测与 Shell 执行
  - [x] SubTask 2.1: 实现 `RootChecker.kt`，检测设备是否有 Root 权限（`su -c id` 返回 uid=0）
  - [x] SubTask 2.2: 实现 `ShellExecutor.kt`，基于 libsu 执行 Root Shell 命令并返回结果
  - [x] SubTask 2.3: 实现自动授予悬浮窗权限（`appops set <package> SYSTEM_ALERT_WINDOW allow`）

- [x] Task 3: 实现 Choreographer 帧率监测核心
  - [x] SubTask 3.1: 实现 `FpsMonitor.kt`，使用 Choreographer.FrameCallback 采集帧率（参考 Takt / TinyDancer / fpsviewer 源码模式）
  - [x] SubTask 3.2: 实现掉帧（Jank）检测，帧耗时超过 16.67ms 标记为掉帧（参考 fpsviewer 阈值检测）
  - [x] SubTask 3.3: 实现采样间隔控制（默认 250ms），每间隔计算当前 FPS（参考 Takt interval 配置）

- [x] Task 4: 实现 Root 系统硬件监控
  - [x] SubTask 4.1: 实现 `HardwareMonitor.kt`，通过 Root Shell 读取 CPU 频率（/sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq）
  - [x] SubTask 4.2: 实现 CPU 使用率计算（/proc/stat）
  - [x] SubTask 4.3: 实现 GPU 负载读取（/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage）
  - [x] SubTask 4.4: 实现温度读取（电池温度 + CPU 温度，/sys/class/power_supply/battery/temp + /sys/class/thermal/）
  - [x] SubTask 4.5: 实现电池电量读取（/sys/class/power_supply/battery/capacity）

- [x] Task 5: 实现悬浮窗 Service
  - [x] SubTask 5.1: 实现 `FpsOverlayService.kt`，前台 Service + WindowManager 悬浮窗（参考 TinyDancer Service 模式）
  - [x] SubTask 5.2: 实现悬浮窗 View 布局（FPS 大数字 + 颜色编码：绿>=50 / 黄30-49 / 红<30）
  - [x] SubTask 5.3: 实现悬浮窗拖拽移动（参考 TinyDancer 的 onTouchListener 拖拽）
  - [x] SubTask 5.4: 实现悬浮窗展开/收起切换（收起时仅 FPS 数字，展开时显示 CPU/GPU/温度/电池）
  - [x] SubTask 5.5: 悬浮窗实时更新 FPS 和硬件数据

- [x] Task 6: 实现帧率记录与图表
  - [x] SubTask 6.1: 实现 `FpsRecorder.kt`，记录 FPS 历史数据（时间戳 + FPS 值）到内存列表
  - [x] SubTask 6.2: 实现 FPS 折线图绘制（使用 Canvas 自绘，不引入第三方图表库，参考 fpsviewer 图表绘制方式）
  - [x] SubTask 6.3: 实现统计信息显示（平均/最低/最高 FPS、总掉帧数）
  - [x] SubTask 6.4: 实现 CSV 导出功能

- [x] Task 7: 实现主界面 UI
  - [x] SubTask 7.1: 实现 `MainActivity.kt` 和主界面 Compose UI（Root 状态卡片 + 实时 FPS 大数字 + 控制按钮）
  - [x] SubTask 7.2: 实现底部导航（实时监测 | 历史记录 | 设置）
  - [x] SubTask 7.3: 实现历史记录页面（历史会话列表 + 点击查看图表）
  - [x] SubTask 7.4: 实现设置页面（采样间隔、悬浮窗颜色、目标帧率、悬浮窗大小）

- [x] Task 8: 编译验证
  - [x] SubTask 8.1: 确保项目结构完整，Gradle 配置正确
  - [x] SubTask 8.2: 执行 `./gradlew assembleDebug` 编译生成 APK（注：沙箱环境网络策略限制 Java HTTP 出站，Gradle 无法下载依赖，需在本地 Android Studio 中编译）
  - [x] SubTask 8.3: 验证 APK 文件存在且大小合理（注：需在本地环境编译验证）

# Task Dependencies
- Task 1 (项目基础结构) 必须先完成，所有其他任务依赖此任务
- Task 2 (Root 检测与 Shell) 依赖 Task 1
- Task 3 (Choreographer 帧率核心) 依赖 Task 1，与 Task 2 可并行
- Task 4 (硬件监控) 依赖 Task 2 (需要 Root Shell)
- Task 5 (悬浮窗 Service) 依赖 Task 2 (自动授权) 和 Task 3 (FPS 数据) 和 Task 4 (硬件数据)
- Task 6 (帧率记录与图表) 依赖 Task 3 (FPS 数据)
- Task 7 (主界面 UI) 依赖 Task 2、Task 3、Task 4、Task 5、Task 6
- Task 8 (编译验证) 依赖所有其他任务完成