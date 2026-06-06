# 面板 UI 配色重设计 + 多 Tab 布局 Spec

## Why
当前悬浮窗展开面板采用绿色配色且布局混乱（图表和控制混在一起），用户反馈不好看且功能分区不清晰。需要重新设计配色方案（泰蓝/青蓝主题），并将展开面板改造为多 Tab 分栏布局，各功能模块清晰独立。

## What Changes
- **配色方案重设计**：全局配色从绿色系改为泰蓝/青蓝系（teal/cyan），FPS 阈值颜色也同步调整
- **展开面板 Tab 布局**：收起态保持灵动岛胶囊不变，展开后面板改为顶部 Tab 栏 + 内容区结构
- **Tab 0 — 设备信息**：泰蓝主题卡片，显示设备型号、CPU 型号、核心数、架构、RAM、屏幕分辨率/刷新率
- **Tab 1 — 实时图表**：FPS 折线图 + 帧耗时分布图 + CPU 多核频率曲线图
- **Tab 2 — 温度面板**：CPU 温度（大字）、GPU 温度、电池温度，温度曲线图
- **Tab 3 — 频率控制**：当前屏幕刷新率显示 + CPU 调速器列表 + 自定义频率输入
- **触及文件**：
  - `FpsOverlayService.kt` — 展开面板 UI 完全重写
  - `FpsLineChartView.kt` — 配色适配新主题
  - `CpuFreqChartView.kt` — 配色适配新主题

## Impact
- Affected specs: overlay-panel-optimization（面板布局 + 配色更新）
- Affected code:
  - `app/src/main/java/com/fpsmonitor/service/FpsOverlayService.kt` — Tab 布局 + 设备信息 + 温度面板 + 配色
  - `app/src/main/java/com/fpsmonitor/service/FpsLineChartView.kt` — 曲线配色
  - `app/src/main/java/com/fpsmonitor/service/CpuFreqChartView.kt` — 曲线配色
  - `app/src/main/java/com/fpsmonitor/core/HardwareMonitor.kt` — 可能需要读取 GPU 温度、设备信息

## ADDED Requirements

### Requirement: 泰蓝配色方案
系统 SHALL 将全局 UI 配色从绿色系改为泰蓝/青蓝系。

**配色规范：**
- 主色调：`#00897B`（Teal 600）— 主按钮、强调元素
- 辅色调：`#00ACC1`（Cyan 600）— 次要强调、图表线条
- 背景：`#F0F4F8`（浅灰蓝）— 面板背景
- 卡片：`#FFFFFF`（纯白）— 卡片底色
- 文字主色：`#263238`（深灰蓝）— 标题和正文
- 文字辅色：`#607D8B`（蓝灰）— 说明文字
- FPS 优（≥50）：`#00897B`（Teal）
- FPS 中（30-49）：`#FFA000`（琥珀）
- FPS 差（<30）：`#E53935`（红）
- 温度低（<40°C）：`#00897B`（Teal）
- 温度中（40-60°C）：`#FFA000`（琥珀）
- 温度高（>60°C）：`#E53935`（红）
- 图表线条：FPS `#00897B`，参考线 `#B0BEC5`，CPU 核心线保持现有 8 色不变

#### Scenario: 配色一致性
- **WHEN** 悬浮窗展开面板
- **THEN** 所有 UI 元素（文字、按钮、卡片、图表）统一使用泰蓝系配色

### Requirement: 展开面板 Tab 布局
系统 SHALL 在展开面板顶部显示 Tab 栏，支持切换以下四个内容区。

**Tab 列表：**
| 序号 | Tab 名称 | 图标 | 内容 |
|------|----------|------|------|
| 0 | 设备 | ⓘ | 设备信息卡片 |
| 1 | 图表 | 📊 | FPS 图 + 帧耗时 + CPU 频率曲线 |
| 2 | 温度 | 🌡 | 温度数据 + 温度曲线 |
| 3 | 控制 | ⚙ | 刷新率 + 调速器 + 频率设置 |

#### Scenario: Tab 切换
- **WHEN** 用户点击顶部不同 Tab
- **THEN** 内容区切换为对应面板，Tab 指示器高亮当前选中项

### Requirement: Tab 0 — 设备信息面板
系统 SHALL 在"设备"Tab 中显示泰蓝主题的设备信息卡片。

**显示内容：**
- 设备型号（Model）：`ro.product.model`
- 制造商（Brand）：`ro.product.manufacturer`
- CPU 型号：`/proc/cpuinfo` 解析 Hardware 字段
- 核心数：已有 `CPU_COUNT`
- CPU 架构：`ro.product.cpu.abi`
- 总内存（RAM）：`/proc/meminfo` 解析 MemTotal
- 屏幕分辨率：`resources.displayMetrics`
- 屏幕刷新率：`display.refreshRate`

#### Scenario: 设备信息卡片
- **WHEN** 用户切换到"设备"Tab
- **THEN** 显示泰蓝主题卡片，列出设备型号、CPU、核心数、内存、分辨率、刷新率

### Requirement: Tab 1 — 实时图表面板
系统 SHALL 在"图表"Tab 中显示三类实时曲线图。

**图表内容：**
- FPS 实时折线图（泰蓝曲线 `#00897B`，目标帧率灰色虚线）
- 帧耗时柱状/折线图（最近 30 帧耗时，单位 ms，柱状图 `#00ACC1`）
- CPU 多核频率曲线图（保持 8 色不变）

#### Scenario: 图表面板刷新
- **WHEN** 监测运行中且用户在"图表"Tab
- **THEN** 三张图表每秒实时更新

### Requirement: Tab 2 — 温度面板
系统 SHALL 在"温度"Tab 中显示温度数据和温度趋势。

**显示内容：**
- CPU 温度（大字醒目，颜色按阈值变化）
- GPU 温度（如有）
- 电池温度
- 近期温度趋势小图（最近 30 个采样点的 CPU 温度曲线）

#### Scenario: 温度面板
- **WHEN** 用户切换到"温度"Tab
- **THEN** 显示 CPU/GPU/电池温度数值和温度趋势曲线

### Requirement: Tab 3 — 频率控制面板
系统 SHALL 在"控制"Tab 中显示当前屏幕刷新率、CPU 各核心调速器和自定义频率设置。

**显示内容：**
- 当前屏幕刷新率（大字号显示，如 "120Hz"）
- CPU 各核心列表：核心 ID + 当前频率 + 调速器（可点击切换）+ 自定义频率按钮
- 可用调速器列表
- 自定义频率输入（MHz）

#### Scenario: 刷新率显示
- **WHEN** 用户切换到"控制"Tab
- **THEN** 顶部显示当前屏幕刷新率

#### Scenario: 调速器切换
- **WHEN** 用户点击某核心的调速器
- **THEN** 弹出可选调速器列表，选择后通过 Root Shell 生效

## MODIFIED Requirements

### Requirement: 收起态悬浮窗
**修改**：收起态灵动岛胶囊配色从暗色（`#141416`）改为泰蓝半透明（`Color.argb(200, 0, 137, 123)`），与展开面板配色一致。

### Requirement: FPS 颜色阈值
**修改**：FPS 优（绿）改为 Teal，FPS 中（黄）改为 Amber，FPS 差（红）保持红色。

## REMOVED Requirements

无