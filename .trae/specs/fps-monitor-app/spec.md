# 帧率监测应用 Spec

## Why
基于开源安卓帧率检测项目（Takt、TinyDancer、fpsviewer、Scene 等），创建一个独立的全新帧率监测 APK 应用。核心技术方案与这些开源项目保持一致：使用 Choreographer.FrameCallback 进行帧率采集，悬浮窗显示实时 FPS，通过 Root 权限获取系统级硬件数据（CPU/GPU/温度/电池），完全无需 ADB 或 Shizuku。

## What Changes
- **全新独立项目**：创建独立的 Android 应用项目 `fpsmonitor`，与 XToolbox 无关
- **Choreographer 帧率监测**：照搬 Takt / TinyDancer / fpsviewer 的 Choreographer.FrameCallback 方案
- **Root 悬浮窗服务**：通过 Root 权限自动授予 SYSTEM_ALERT_WINDOW 权限，启动前台 Service 显示悬浮窗
- **系统级硬件监控**：通过 Root Shell 读取 /sys/class 节点获取 CPU 频率、GPU 负载、温度、电池信息
- **Jank 丢帧检测**：参考 fpsviewer 的帧耗时阈值检测方案
- **帧率记录与图表**：参考 TakoStats 和 Scene 的记录功能，保存历史帧率数据并绘制折线图
- **APK 可打包**：完整 Gradle 构建配置，可直接编译生成 APK

## Impact
- Affected specs: 无（全新项目）
- Affected code: 全新创建 `fpsmonitor/` 项目目录
  - `fpsmonitor/app/src/main/java/com/fpsmonitor/` - 应用主代码
  - `fpsmonitor/app/src/main/AndroidManifest.xml` - 权限声明
  - `fpsmonitor/app/build.gradle.kts` - 构建配置
  - `fpsmonitor/build.gradle.kts` - 根构建配置
  - `fpsmonitor/settings.gradle.kts` - 项目设置

## ADDED Requirements

### Requirement: Choreographer 帧率监测核心
系统 SHALL 使用 Choreographer.FrameCallback 实现帧率监测，与 Takt / TinyDancer / fpsviewer 开源项目保持一致的技术方案。

参考开源项目：
- Takt (https://github.com/wasabeef/takt) - Choreographer 帧率测量库
- TinyDancer (https://github.com/friendlyrobotnyc/TinyDancer) - 悬浮窗 FPS 显示
- fpsviewer (https://github.com/SilenceDut/fpsviewer) - 卡顿检测与帧率可视化

**实现要点：**
- 注册 Choreographer.FrameCallback，在 doFrame() 回调中计算帧间隔
- 每秒统计帧数计算 FPS（参考 Takt 的间隔采样方式）
- 支持设置采样间隔（默认 250ms，参考 Takt 的 interval 配置）
- 帧耗时超过 16.67ms（60fps 基准）标记为掉帧（参考 fpsviewer 的阈值检测）

#### Scenario: 实时 FPS 采集
- **WHEN** 启动帧率监测服务
- **THEN** 系统开始通过 Choreographer.FrameCallback 采集帧率数据，每秒计算并更新 FPS 值

#### Scenario: 掉帧检测
- **WHEN** 某帧耗时超过 16.67ms（60fps 对应 1 帧时间）
- **THEN** 系统记录该帧为掉帧（Jank），累计掉帧计数

### Requirement: Root 悬浮窗显示
系统 SHALL 通过 Root 权限自动配置悬浮窗权限，并使用前台 Service + WindowManager 实现悬浮窗 FPS 显示。

参考开源项目：
- Scene (http://vtools.omarea.com/) - Root 模式悬浮窗
- TakoStats (https://play.google.com/store/apps/details?id=rikka.fpsmonitor) - 悬浮窗性能监控
- TinyDancer - 可拖拽悬浮窗

**实现要点：**
- 通过 Root Shell 执行 `appops set <package> SYSTEM_ALERT_WINDOW allow` 自动授予悬浮窗权限
- 使用前台 Service 保持悬浮窗存活（参考 TinyDancer 的 Service 模式）
- WindowManager.LayoutParams 配置悬浮窗参数（TYPE_APPLICATION_OVERLAY）
- 悬浮窗可拖拽移动（参考 TinyDancer 的 onTouchListener 拖拽实现）
- 悬浮窗显示：实时 FPS 数值 + 颜色编码（绿 >= 50 / 黄 30-49 / 红 < 30，参考 TinyDancer 的颜色编码）
- 悬浮窗可展开/收起：收起时仅显示 FPS 数字，展开时显示完整信息

#### Scenario: 悬浮窗显示 FPS
- **WHEN** 用户启动帧率监测
- **THEN** 屏幕显示悬浮窗，实时刷新当前 FPS 数值，颜色根据帧率高低变化，悬浮窗可拖拽移动

#### Scenario: 悬浮窗展开完整信息
- **WHEN** 用户点击悬浮窗
- **THEN** 悬浮窗展开显示 FPS + CPU 使用率 + GPU 负载 + 温度 + 电池电量

### Requirement: Root 系统硬件监控
系统 SHALL 通过 Root Shell 读取 sysfs 节点获取系统硬件数据，参考 Scene 的 Root 模式实现。

参考开源项目：
- Scene (http://vtools.omarea.com/) - Root 模式硬件监控（CPU/GPU/温度/功耗）
- TakoStats - 通过 Shizuku 获取系统数据（本应用改用 Root 直接读取）

**实现要点：**
- CPU 频率：读取 `/sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq`
- CPU 使用率：通过 `/proc/stat` 计算
- GPU 负载：读取 `/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage`（高通）或 `/sys/kernel/gpu/gpu_usage`（其他）
- 电池温度：读取 `/sys/class/power_supply/battery/temp`
- 电池电量：读取 `/sys/class/power_supply/battery/capacity`
- CPU 温度：读取 `/sys/class/thermal/thermal_zone*/temp`（遍历查找 CPU 相关 zone）
- 所有读取通过 Root Shell 执行 `cat` 命令（参考 Scene 的 daemon 模式）

#### Scenario: 读取 CPU 频率
- **WHEN** 悬浮窗展开显示硬件信息
- **THEN** 系统通过 Root Shell 读取 CPU 各核心频率，显示在悬浮窗中

#### Scenario: 读取温度和电池
- **WHEN** 悬浮窗展开显示硬件信息
- **THEN** 系统通过 Root Shell 读取电池温度、电量、CPU 温度，显示在悬浮窗中

### Requirement: 帧率记录与历史图表
系统 SHALL 提供帧率数据记录和可视化图表功能，参考 TakoStats 的 Records 和 Scene 的帧率记录功能。

参考开源项目：
- TakoStats - Records 功能，记录 FPS 变化并图表展示
- Scene - 帧率记录曲线（最高 240Hz）

**实现要点：**
- 记录 FPS 历史数据（时间戳 + FPS 值），保存到内存和本地文件
- 使用 Canvas 或 MPAndroidChart 绘制折线图
- 显示统计信息：平均 FPS、最低 FPS、最高 FPS、总掉帧数
- 支持导出 CSV 格式数据
- 主界面提供"开始记录"/"停止记录"按钮

#### Scenario: 记录帧率会话
- **WHEN** 用户点击"开始记录"
- **THEN** 系统持续记录 FPS 数据到内存列表，同时悬浮窗显示录制中状态

#### Scenario: 查看历史图表
- **WHEN** 用户停止记录并查看结果
- **THEN** 显示 FPS 折线图 + 统计信息（平均/最低/最高 FPS、掉帧数）

### Requirement: 主界面 UI
系统 SHALL 提供简洁的主界面，不依赖 MIUI X 等第三方 UI 库，使用原生 Android 组件实现。

**实现要点：**
- 使用 Jetpack Compose 或原生 XML 布局（Material3 风格，保持简洁）
- 顶部状态卡片：Root 状态 + 设备信息
- 中间实时 FPS 大数字显示
- 操作按钮：开始监测 / 停止监测 / 开始记录 / 停止记录
- 底部导航：实时监测 | 历史记录 | 设置
- 设置页面：采样间隔、悬浮窗颜色、目标帧率、悬浮窗大小等

#### Scenario: 主界面展示
- **WHEN** 用户打开应用
- **THEN** 显示 Root 状态卡片和实时 FPS 大数字，提供监测/记录控制按钮

### Requirement: APK 构建与打包
系统 SHALL 提供完整的 Gradle 构建配置，可直接编译生成 APK。

**实现要点：**
- 最小 SDK：Android 8.0 (API 26)
- 目标 SDK：Android 14 (API 34)
- 编译 SDK：Android 34
- Kotlin 语言
- 依赖：libsu (Root Shell 执行)、Compose (UI)
- 签名：debug 签名即可
- 输出：`fpsmonitor/app/build/outputs/apk/debug/fpsmonitor-debug.apk`

#### Scenario: 编译 APK
- **WHEN** 执行 `./gradlew assembleDebug`
- **THEN** 成功生成 debug APK 文件

## MODIFIED Requirements

无（全新项目，无修改需求）

## REMOVED Requirements

无（全新项目，无移除需求）