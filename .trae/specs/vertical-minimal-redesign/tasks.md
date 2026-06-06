# Tasks

- [ ] Task 1: 竖版悬浮窗布局重构
  - [ ] 1.1 收起态改为竖版圆角矩形（48dp x 80dp，深黑背景，白色 FPS 文字）
  - [ ] 1.2 展开面板改为竖版单页布局（宽 200dp）
  - [ ] 1.3 悬浮窗默认位置改为屏幕右侧居中
  - [ ] 1.4 展开面板内容：FPS 大字（顶部）→ FPS 折线图（中间）→ 帧率统计（底部）

- [ ] Task 2: 移除无效硬件功能
  - [ ] 2.1 删除 FrameTimeChartView.kt、TempTrendChartView.kt、CpuFreqChartView.kt
  - [ ] 2.2 FpsOverlayService 移除所有硬件相关代码（Tab栏、设备信息、温度、控制、CPU核心列表）
  - [ ] 2.3 FpsData.kt 移除 DeviceInfo、gpuTemp 等无效字段
  - [ ] 2.4 HardwareMonitor.kt 简化，移除 gpuTemp/deviceInfo 读取，仅保留 CPU 频率基础读取

- [ ] Task 3: 纯白配色方案
  - [ ] 3.1 FpsOverlayService 配色改为纯白 + 浅灰 + 黑色
  - [ ] 3.2 FpsLineChartView 配色改为纯白背景 + 黑色线条
  - [ ] 3.3 收起态胶囊：深黑背景 `#1A1A1A` + 白色 FPS 文字

- [ ] Task 4: 现代化 UI 细节
  - [ ] 4.1 去除所有 Emoji/符号，使用纯文字
  - [ ] 4.2 圆角卡片（12dp）+ 细边框（1dp #E0E0E0）
  - [ ] 4.3 帧率统计区显示：平均 FPS | 最小 FPS | 最大 FPS | 卡顿次数

- [ ] Task 5: 用户自定义配色
  - [ ] 5.1 SettingsManager 新增颜色配置字段（收起态背景色、FPS 文字色、图表线色、面板背景色）
  - [ ] 5.2 MainActivity 设置页新增颜色选择区（预设颜色列表）
  - [ ] 5.3 FpsOverlayService 读取颜色配置并应用

- [ ] Task 6: 验证与推送
  - [ ] 6.1 推送 GitHub Actions 触发 CI
  - [ ] 6.2 确认编译通过，APK 可下载

# Task Dependencies
- Task 2 依赖 Task 1（先重构布局，再删除无用代码）
- Task 3 依赖 Task 1（布局就绪后配色）
- Task 4 依赖 Task 3（配色就绪后 UI 细节调整）
- Task 5 依赖 Task 4（UI 就绪后添加自定义配色）
- Task 6 在所有 Task 完成后执行