# 悬浮窗面板优化 Spec

## Why
当前悬浮窗展开后仅显示简单的文本信息（CPU/GPU/温度/电池），展示方式简陋，无法查看实时帧率趋势图，也无法对 CPU 核心频率和调速器进行控制。需要将展开面板升级为功能更丰富的控制面板，提供实时帧率图、CPU 频率图、CPU 核心频率/调速器调节能力，并使用简洁的白色风格。

## What Changes
- **悬浮窗面板重新设计**：点击悬浮窗后展开为一个完整面板，白色简洁风格，半透明背景
- **实时帧率折线图**：面板左侧显示最近 N 秒的 FPS 变化曲线
- **CPU 频率折线图**：每个核心用不同颜色绘制频率变化曲线
- **CPU 核心控制面板**：面板右侧显示各核心当前频率和调速器（governor），支持点击切换调速器
- **CPU 温度显示**：面板中显示主要处理器温度
- **触及文件**：
  - `FpsOverlayService.kt` — 悬浮窗 UI 重写，从简单 TextView 改为面板布局
  - `FpsData.kt` — 新增 CpuCoreInfo 数据类，HardwareData 增加 governors 字段
  - `HardwareMonitor.kt` — 新增读取/设置 CPU 调速器的方法
  - `ShellExecutor.kt` — 新增写入文件的方法

## Impact
- Affected specs: fps-monitor-app（悬浮窗展开功能升级）
- Affected code:
  - `app/src/main/java/com/fpsmonitor/service/FpsOverlayService.kt` — 重写悬浮窗面板 UI
  - `app/src/main/java/com/fpsmonitor/core/FpsData.kt` — 新增数据模型
  - `app/src/main/java/com/fpsmonitor/core/HardwareMonitor.kt` — 新增 governor 读写
  - `app/src/main/java/com/fpsmonitor/core/ShellExecutor.kt` — 新增 writeFile
  - `app/src/main/java/com/fpsmonitor/core/FpsMonitor.kt` — 可能需要暴露历史 FPS 数据

## ADDED Requirements

### Requirement: 悬浮窗面板白色风格重设计
系统 SHALL 将悬浮窗展开面板重新设计为简洁白色风格，与原悬浮窗的暗色风格区分。

**实现要点：**
- 面板背景：白色半透明（`Color.argb(230, 255, 255, 255)`）
- 文字颜色：深色（`Color.rgb(50, 50, 50)` 或 `Color.DKGRAY`）
- 卡片/区块背景：浅灰（`Color.argb(30, 0, 0, 0)` 或 `#F5F5F5`）
- 圆角边框：12dp 圆角
- 图表线条和文字适配白色背景
- 收起状态悬浮窗保持原有暗色风格不变，仅展开面板为白色

#### Scenario: 展开面板白色风格
- **WHEN** 用户点击悬浮窗展开面板
- **THEN** 面板以白色半透明背景展示，所有文字和图表在白色背景上清晰可读

### Requirement: 实时帧率折线图
系统 SHALL 在展开面板中显示最近 N 秒的 FPS 实时变化折线图。

**实现要点：**
- 在面板左侧区域绘制 FPS 折线图，使用 Canvas 绘制
- 保留最近 60 个采样点（约 15 秒，按 250ms 采样间隔）
- 图表尺寸：约 200dp × 150dp
- 显示目标帧率参考线（虚线，60fps）
- Y 轴显示 FPS 范围，X 轴显示时间
- 颜色：FPS 曲线使用蓝色（`#2196F3`）
- 参考线使用浅灰虚线

#### Scenario: 折线图实时更新
- **WHEN** 帧率监测服务运行中
- **THEN** 展开面板中的 FPS 折线图每秒更新，曲线随 FPS 变化实时滚动

### Requirement: CPU 频率折线图（多核心颜色区分）
系统 SHALL 在展开面板中显示各 CPU 核心的频率变化曲线，每个核心使用不同颜色。

**实现要点：**
- 检测可用核心数（最多 8 核）
- 每个核心用不同颜色绘制频率曲线：
  - 核心 0：`#F44336`（红）
  - 核心 1：`#2196F3`（蓝）
  - 核心 2：`#4CAF50`（绿）
  - 核心 3：`#FF9800`（橙）
  - 核心 4：`#9C27B0`（紫）
  - 核心 5：`#00BCD4`（青）
  - 核心 6：`#E91E63`（粉）
  - 核心 7：`#795548`（棕）
- 保留最近 30 个采样点
- Y 轴显示频率范围（MHz），每核独立 Y 轴或统一最大频率
- 图表尺寸：约 200dp × 120dp
- 图例标注各颜色对应核心

#### Scenario: 多核频率曲线
- **WHEN** 展开面板且设备有 8 核 CPU
- **THEN** 面板中显示 8 条不同颜色的频率曲线，每条曲线对应一个核心

### Requirement: CPU 核心频率与调速器控制
系统 SHALL 在展开面板右侧提供 CPU 各核心的频率和调速器查看与调节功能。

**实现要点：**
- 读取调速器路径：`/sys/devices/system/cpu/cpu{N}/cpufreq/scaling_governor`
- 可用调速器列表：`/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors`
- 显示每个核心的当前频率和调速器
- 点击调速器弹出可选调速器列表（performance / powersave / ondemand / conservative / schedutil / interactive / userspace）
- 通过 Root Shell 写入调速器：`echo <governor> > /sys/devices/system/cpu/cpu{N}/cpufreq/scaling_governor`
- 如果设备不支持调速器切换（如某些新机型），显示"不支持"并禁用点击
- 频率显示为大核/中核/小核分组，每组显示当前频率

#### Scenario: 切换调速器
- **WHEN** 用户点击某个核心的调速器并选择 "performance"
- **THEN** 系统通过 Root Shell 将对应核心的调速器设置为 performance，界面立即更新显示

#### Scenario: 设备不支持调速器
- **WHEN** 设备没有 scaling_available_governors 文件
- **THEN** 调速器区域显示"不支持"，不可点击

### Requirement: CPU 温度显示
系统 SHALL 在展开面板中显示主要处理器的温度。

**实现要点：**
- 读取 CPU 温度（已有 `HardwareMonitor.readCpuTemp()`）
- 在面板显眼位置显示温度，大字号
- 温度颜色根据阈值变化：
  - < 40°C：绿色
  - 40-60°C：橙色
  - > 60°C：红色
- 同时显示电池温度

#### Scenario: 温度显示
- **WHEN** 展开面板
- **THEN** 面板中显示 CPU 温度和电池温度，颜色根据温度值变化

### Requirement: 面板布局结构
系统 SHALL 按照以下布局组织展开面板：

```
┌──────────────────────────────────────┐
│  FPS: 60  [●录制中]  CPU: 42°C 🔋45°C │  ← 顶部状态栏
├────────────────────┬─────────────────┤
│                    │  CPU 核心控制    │
│   FPS 实时折线图    │  ┌─────────────┐│
│   (蓝色曲线)       │  │ 核心0: 1800  ││
│                    │  │ 调速器: perf ││
│                    │  ├─────────────┤│
│                    │  │ 核心1: 1800  ││
│                    │  │ 调速器: perf ││
│                    │  ├─────────────┤│
│                    │  │ ...         ││
│                    │  └─────────────┘│
├────────────────────┴─────────────────┤
│  CPU 频率曲线（多色）                 │
│  ─ 核心0  ─ 核心1  ─ 核心2 ...      │
└──────────────────────────────────────┘
```

#### Scenario: 面板布局
- **WHEN** 用户点击悬浮窗展开面板
- **THEN** 面板按上述布局显示，顶部状态栏 + 左侧 FPS 图 + 右侧 CPU 控制 + 底部 CPU 频率图

## MODIFIED Requirements

### Requirement: 悬浮窗可展开/收起
**原需求**（来自 fps-monitor-app）：悬浮窗点击展开显示简单文本信息。

**修改为**：悬浮窗点击展开显示完整控制面板，包含 FPS 折线图、CPU 频率图、CPU 核心调速器控制、温度显示。收起时仅显示 FPS 数字。

#### Scenario: 展开完整面板
- **WHEN** 用户点击悬浮窗
- **THEN** 悬浮窗展开为白色面板，显示 FPS 折线图、CPU 频率图、核心控制、温度信息

## REMOVED Requirements

无