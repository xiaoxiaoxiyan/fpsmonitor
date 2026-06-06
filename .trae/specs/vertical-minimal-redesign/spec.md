# 竖版极简悬浮窗 Spec

## Why
当前悬浮窗为横版胶囊 + 多 Tab 布局，泰蓝配色不协调，UI 符号过多。用户要求：竖版布局、纯白配色、保留所有硬件功能（有 Root 权限）、现代化简洁设计无符号、配色可自定义。

## What Changes
- **悬浮窗方向改为竖版**：收起态为竖版圆角矩形，展开面板为竖版单页滚动布局
- **纯白配色**：所有 UI 元素使用纯白 + 浅灰 + 黑色，去除彩色
- **保留所有硬件功能**：设备信息、CPU 频率图、温度显示、调速器控制、自定义频率全部保留，依赖 Root 正常工作
- **去除 Tab 栏**：展开面板改为竖版单页滚动，所有内容垂直排列，无需切换
- **现代化设计**：去除所有 Emoji/符号，使用简洁文字，圆角卡片，细边框
- **配色可自定义**：在设置页面提供颜色选择，用户可调整背景/文字/图表色
- **触及文件**：
  - `FpsOverlayService.kt` — 竖版布局重写
  - `FpsLineChartView.kt` — 配色适配纯白
  - `CpuFreqChartView.kt` — 配色适配纯白
  - `FrameTimeChartView.kt` — 配色适配纯白
  - `TempTrendChartView.kt` — 配色适配纯白
  - `SettingsManager.kt` — 新增颜色配置项
  - `MainActivity.kt` — 新增颜色选择器

## Impact
- Affected specs: panel-tab-redesign（布局推翻、配色推翻）
- Affected code: FpsOverlayService, FpsLineChartView, CpuFreqChartView, FrameTimeChartView, TempTrendChartView, SettingsManager, MainActivity

## ADDED Requirements

### Requirement: 竖版悬浮窗布局
系统 SHALL 将悬浮窗改为竖版（竖向）布局。

- 收起态：竖版圆角矩形（宽约 48dp，高约 80dp），深黑背景，白色 FPS 数字居中
- 展开态：竖版卡片（宽约 260dp），ScrollView 包裹，从上到下依次排列所有内容
- 悬浮窗默认位置：屏幕右侧居中

#### Scenario: 收起态竖版显示
- **WHEN** 悬浮窗处于收起状态
- **THEN** 显示为竖版圆角矩形，内部显示 FPS 数字

### Requirement: 纯白配色方案
系统 SHALL 使用纯白 + 浅灰 + 黑色配色，去除所有彩色。

**配色规范：**
- 面板背景：`#FFFFFF`（纯白）
- 卡片背景：`#F5F5F5`（浅灰）
- 文字主色：`#1A1A1A`（深黑）
- 文字辅色：`#888888`（中灰）
- 边框/分割线：`#E0E0E0`（浅灰）
- 收起态胶囊背景：`#1A1A1A`（深黑），FPS 文字：`#FFFFFF`（白色）
- 图表线：`#1A1A1A`（深黑）
- 图表参考线：`#CCCCCC`（浅灰虚线）
- 图表文字：`#888888`（中灰）
- 按钮背景：`#1A1A1A`（深黑），按钮文字：`#FFFFFF`（白色）
- 温度颜色阈值：不区分颜色，统一使用 `#1A1A1A`

#### Scenario: 纯白配色一致性
- **WHEN** 悬浮窗展开
- **THEN** 所有元素使用纯白/浅灰/黑色配色，无彩色

### Requirement: 展开面板竖版布局
系统 SHALL 在展开面板中按垂直顺序排列以下内容区，ScrollView 包裹。

**从上到下排列：**
1. **FPS 大字**（顶部居中，48sp，深黑文字）
2. **帧率统计**（水平排列：平均 | 最小 | 最大 | 卡顿，浅灰背景卡片）
3. **FPS 折线图**（FpsLineChartView，高度 200dp）
4. **帧耗时柱状图**（FrameTimeChartView，高度 160dp）
5. **CPU 频率曲线图**（CpuFreqChartView，高度 160dp）
6. **设备信息区**（浅灰圆角卡片，显示型号/CPU/内存/分辨率/刷新率）
7. **温度区**（浅灰圆角卡片，CPU/GPU/电池温度 + 温度趋势图）
8. **CPU 控制区**（浅灰圆角卡片，每个核心：频率 + 调速器按钮 + 自定义频率按钮）

#### Scenario: 展开面板滚动
- **WHEN** 悬浮窗展开且内容超出屏幕高度
- **THEN** 面板内可垂直滚动查看所有内容

### Requirement: 保留所有硬件功能
系统 SHALL 保留并确保所有硬件功能通过 Root 正常工作。

- 设备信息：型号、制造商、CPU 型号、架构、核心数、内存、分辨率、刷新率
- 温度：CPU 温度、GPU 温度、电池温度、温度趋势图
- CPU 频率：多核频率曲线图
- 调速器：可切换各核心调速器
- 自定义频率：可设置各核心频率

#### Scenario: 硬件功能正常
- **WHEN** 设备已 Root 且展开悬浮窗
- **THEN** 所有硬件数据实时显示，调速器和频率设置可正常操作

### Requirement: 现代化 UI 设计
系统 SHALL 使用现代化简洁设计，无 Emoji 或特殊符号。

- 所有文字使用纯文本，无 Emoji，无特殊符号
- 圆角卡片（12dp 圆角）
- 细边框（1dp，颜色 #E0E0E0）
- 字体使用系统默认 + 合适的字号层级
- 按钮使用深色背景 + 白色文字，圆角
- 收起态使用深色圆角矩形，简洁现代

#### Scenario: 现代化设计
- **WHEN** 用户查看悬浮窗
- **THEN** 界面简洁、无符号、无彩色、纯文字 + 图表

### Requirement: 用户自定义配色
系统 SHALL 提供颜色配置选项，用户可自定义悬浮窗配色。

**可配置项：**
- 收起态背景色
- FPS 文字颜色
- 图表线颜色
- 面板背景色

**配置方式：**
- 在 SettingsManager 中保存颜色值（默认纯白配色）
- 在主界面设置页面提供预设颜色列表
- 颜色变更后实时生效

#### Scenario: 颜色自定义
- **WHEN** 用户在设置中修改颜色
- **THEN** 悬浮窗实时应用新颜色

## MODIFIED Requirements

### Requirement: 收起态悬浮窗
**修改**：方向从横版胶囊改为竖版圆角矩形，配色从泰蓝半透明改为深黑。
尺寸：宽 48dp，高 80dp，圆角 24dp，背景 `#1A1A1A`，FPS 文字 `#FFFFFF`。

### Requirement: 展开面板
**修改**：从多 Tab 布局改为竖版单页滚动布局，所有内容垂直排列。
面板宽度 260dp，白色背景，ScrollView 包裹。

### Requirement: 图表配色
**修改**：所有图表（FpsLineChartView、CpuFreqChartView、FrameTimeChartView、TempTrendChartView）配色从泰蓝系改为纯白系。
背景 `#F5F5F5`，线条 `#1A1A1A`，文字 `#888888`，参考线 `#CCCCCC`。

### Requirement: 按钮配色
**修改**：所有按钮（调速器、自定义频率）配色从泰蓝改为深黑。
背景 `#1A1A1A`，文字 `#FFFFFF`，圆角。

## REMOVED Requirements

### Requirement: Tab 栏
**Reason**: 改为竖版单页滚动，无需 Tab 切换
**Migration**: 删除 Tab 栏，所有内容垂直排列在 ScrollView 中

### Requirement: 泰蓝/彩色配色
**Reason**: 用户要求纯白配色
**Migration**: 所有彩色替换为纯白/浅灰/黑色