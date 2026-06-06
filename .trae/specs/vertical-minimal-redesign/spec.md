# 竖版极简悬浮窗 Spec

## Why
当前悬浮窗为横版胶囊布局，Tab 面板中的硬件功能（CPU 频率、温度、调速器）依赖 Root 权限，在大部分设备上无法正常工作，沦为摆设。用户需要：竖版悬浮窗、纯白配色、只保留可用的帧率监测功能、UI 现代化无符号、配色可自定义。

## What Changes
- **悬浮窗方向改为竖版**：收起态为竖版圆角矩形，展开面板也改为竖版布局
- **移除无效硬件功能**：删除温度 Tab、控制 Tab、设备信息 Tab、CPU 频率图、帧耗时图
- **只保留 FPS 监测**：收起态显示实时 FPS，展开面板显示 FPS 折线图和帧率统计
- **纯白配色**：所有 UI 元素使用纯白 + 浅灰 + 黑色文字，去除泰蓝/绿色等彩色
- **现代化设计**：去除所有 Emoji/符号，使用简洁文字标签，圆角卡片，细边框
- **配色可自定义**：在设置页面提供颜色选择，用户可调整 FPS 文字/图表/背景色
- **触及文件**：
  - `FpsOverlayService.kt` — 竖版布局重写，移除无效功能
  - `FrameTimeChartView.kt` — 可删除
  - `TempTrendChartView.kt` — 可删除
  - `CpuFreqChartView.kt` — 可删除
  - `FpsLineChartView.kt` — 保留，配色适配
  - `FpsData.kt` — 移除 DeviceInfo/gpuTemp
  - `HardwareMonitor.kt` — 简化为仅 CPU 频率读取
  - `SettingsManager.kt` — 新增颜色配置项
  - `MainActivity.kt` — 新增颜色选择器

## Impact
- Affected specs: panel-tab-redesign（全部推翻）
- Affected code: FpsOverlayService, FpsLineChartView, FpsData, HardwareMonitor, SettingsManager, MainActivity

## ADDED Requirements

### Requirement: 竖版悬浮窗布局
系统 SHALL 将悬浮窗改为竖版（竖向）布局。

- 收起态：竖版圆角矩形（宽约 48dp，高约 80dp），显示 FPS 数字
- 展开态：竖版卡片（宽约 200dp），从上到下依次显示 FPS 大字、FPS 折线图、帧率统计
- 悬浮窗默认位置：屏幕右侧居中

#### Scenario: 收起态竖版显示
- **WHEN** 悬浮窗处于收起状态
- **THEN** 显示为竖版圆角矩形，内部显示 FPS 数字（白色文字）

### Requirement: 纯白配色方案
系统 SHALL 使用纯白 + 浅灰配色，去除所有彩色。

**配色规范：**
- 背景：`#FFFFFF`（纯白）
- 卡片背景：`#F5F5F5`（浅灰）
- 文字主色：`#1A1A1A`（深黑）
- 文字辅色：`#888888`（中灰）
- 边框/分割线：`#E0E0E0`（浅灰）
- 收起态胶囊：`#1A1A1A`（深黑背景，白色 FPS 文字）
- FPS 图表线：`#1A1A1A`（深黑）
- 目标帧率参考线：`#CCCCCC`（浅灰虚线）

#### Scenario: 纯白配色一致性
- **WHEN** 悬浮窗展开
- **THEN** 所有元素使用纯白/浅灰/黑色配色，无彩色

### Requirement: 移除无效硬件功能
系统 SHALL 移除所有依赖 Root 的硬件功能（温度、调速器、设备信息），只保留帧率监测。

**移除内容：**
- 设备信息 Tab（设备型号、CPU、内存等）
- 温度 Tab（CPU/GPU/电池温度、温度趋势图）
- 控制 Tab（刷新率、调速器、自定义频率）
- CPU 频率曲线图
- 帧耗时柱状图
- 所有 Tab 栏

**保留内容：**
- FPS 实时监测（Choreographer）
- FPS 折线图
- 帧率统计：平均 FPS、最小 FPS、最大 FPS、卡顿次数

#### Scenario: 展开面板内容
- **WHEN** 悬浮窗展开
- **THEN** 显示 FPS 大字 + FPS 折线图 + 帧率统计（avg/min/max/jank），无硬件信息

### Requirement: 现代化 UI 设计
系统 SHALL 使用现代化简洁设计，无 Emoji 或特殊符号。

- 所有文字使用纯文本（中文或英文），无 Emoji
- 圆角卡片（12dp 圆角）
- 细边框（0.5dp 或 1dp）
- 字体使用系统默认 + 合适的字号层级
- 收起态使用深色圆角矩形，简洁现代

#### Scenario: 现代化设计
- **WHEN** 用户查看悬浮窗
- **THEN** 界面简洁、无符号、无彩色、纯文字 + 图表

### Requirement: 用户自定义配色
系统 SHALL 提供颜色配置选项，用户可自定义 FPS 相关颜色。

**可配置项：**
- 收起态背景色
- FPS 文字颜色
- 图表线颜色
- 展开面板背景色

**配置方式：**
- 在 SettingsManager 中保存颜色值（默认纯白配色）
- 在主界面设置页面提供颜色选择入口
- 使用简单的预设颜色列表供选择

#### Scenario: 颜色自定义
- **WHEN** 用户在设置中修改颜色
- **THEN** 悬浮窗实时应用新颜色

## MODIFIED Requirements

### Requirement: 收起态悬浮窗
**修改**：方向从横版胶囊改为竖版圆角矩形，配色从泰蓝半透明改为深黑半透明。
尺寸：宽 48dp，高 80dp，圆角 24dp。

### Requirement: 展开面板
**修改**：从多 Tab 布局改为单页竖版布局，只显示 FPS 相关内容。
布局：FPS 大字（顶部）→ FPS 折线图（中间）→ 帧率统计（底部）。

## REMOVED Requirements

### Requirement: Tab 布局
**Reason**: 所有硬件功能无效，无需 Tab 切换
**Migration**: 展开面板改为单页竖版布局

### Requirement: 设备信息 Tab
**Reason**: 依赖 Root 读取，无效
**Migration**: 移除

### Requirement: 温度 Tab
**Reason**: 依赖 Root 读取，无效
**Migration**: 移除

### Requirement: 频率控制 Tab
**Reason**: 依赖 Root 写入，无效
**Migration**: 移除