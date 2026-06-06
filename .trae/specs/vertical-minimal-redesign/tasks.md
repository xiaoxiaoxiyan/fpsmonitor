# Tasks

- [ ] Task 1: 竖版布局重构 + 纯白配色
  - [ ] 1.1 收起态改为竖版圆角矩形（48dp x 80dp，深黑背景 #1A1A1A，白色 FPS 文字）
  - [ ] 1.2 展开面板改为竖版单页 ScrollView 布局（宽 260dp，白色背景）
  - [ ] 1.3 悬浮窗默认位置改为屏幕右侧居中
  - [ ] 1.4 所有颜色从泰蓝系改为纯白/浅灰/黑色系

- [ ] Task 2: 展开面板内容垂直排列
  - [ ] 2.1 移除 Tab 栏，改为单页垂直滚动
  - [ ] 2.2 从上到下依次排列：FPS 大字 → 帧率统计 → FPS 折线图 → 帧耗时图 → CPU 频率图 → 设备信息 → 温度区 → CPU 控制区
  - [ ] 2.3 每个区域用浅灰圆角卡片包裹（#F5F5F5，12dp 圆角，1dp 边框 #E0E0E0）

- [ ] Task 3: 图表配色适配纯白
  - [ ] 3.1 FpsLineChartView：背景 #F5F5F5，线条 #1A1A1A，参考线 #CCCCCC 虚线，文字 #888888
  - [ ] 3.2 CpuFreqChartView：背景 #F5F5F5，核心线保持 8 色不变（用于区分核心），文字 #888888
  - [ ] 3.3 FrameTimeChartView：背景 #F5F5F5，柱体 #1A1A1A，文字 #888888
  - [ ] 3.4 TempTrendChartView：背景 #F5F5F5，线条 #1A1A1A，文字 #888888

- [ ] Task 4: 现代化 UI 细节
  - [ ] 4.1 去除所有 Emoji/符号，使用纯文字标签
  - [ ] 4.2 按钮统一深色背景 #1A1A1A + 白色文字 + 圆角
  - [ ] 4.3 帧率统计区：平均 FPS | 最小 FPS | 最大 FPS | 卡顿次数
  - [ ] 4.4 温度不区分颜色阈值，统一黑色文字

- [ ] Task 5: 用户自定义配色
  - [ ] 5.1 SettingsManager 新增颜色配置字段
  - [ ] 5.2 MainActivity 设置页新增颜色选择区
  - [ ] 5.3 FpsOverlayService 读取并应用颜色配置

- [ ] Task 6: 验证与推送
  - [ ] 6.1 推送 GitHub Actions 触发 CI
  - [ ] 6.2 确认编译通过，APK 可下载

# Task Dependencies
- Task 2 依赖 Task 1（布局就绪后填入内容）
- Task 3 依赖 Task 1（配色基础就绪后适配图表）
- Task 4 依赖 Task 2、3（UI 细节调整）
- Task 5 依赖 Task 4（UI 就绪后添加自定义配色）
- Task 6 在所有 Task 完成后执行