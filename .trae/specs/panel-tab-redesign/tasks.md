# Tasks

- [x] Task 1: 全局配色替换 — 绿色系 → 泰蓝系
  - [x] 1.1 FpsOverlayService 中所有 Green/绿改为 Teal（#00897B），琥珀（#FFA000），红保持
  - [x] 1.2 FpsLineChartView FPS 曲线色改为 #00897B，参考线保持灰色
  - [x] 1.3 CpuFreqChartView 底色适配白色面板
  - [x] 1.4 收起态灵动岛胶囊色改为泰蓝半透明
  - [x] 1.5 展开面板背景保持白色

- [x] Task 2: Tab 布局框架 — 展开面板顶部 Tab 栏 + 内容区切换
  - [x] 2.1 创建四个 Tab 按钮（设备 / 图表 / 温度 / 控制），水平排列
  - [x] 2.2 创建四个内容容器，默认选中"图表"，点击 Tab 切换显示
  - [x] 2.3 Tab 选中态指示器（下划线或颜色变化）
  - [x] 2.4 每个 Tab 按钮用文字

- [x] Task 3: Tab 0 — 设备信息卡片
  - [x] 3.1 通过 ShellExecutor 读取设备型号、制造商、CPU 型号、架构、RAM
  - [x] 3.2 通过 DisplayMetrics 获取屏幕分辨率和刷新率
  - [x] 3.3 在泰蓝主题卡片中展示这些信息，排版清晰

- [x] Task 4: Tab 1 — 实时图表面板
  - [x] 4.1 FPS 实时折线图（重用 FpsLineChartView，配色适配）
  - [x] 4.2 新增帧耗时柱状图（最近 30 帧的 frameTimeMs 数据）
  - [x] 4.3 CPU 多核频率曲线图（重用 CpuFreqChartView）

- [x] Task 5: Tab 2 — 温度面板
  - [x] 5.1 CPU 温度大字显示（颜色按阈值：<40 Teal, 40-60 Amber, >60 Red）
  - [x] 5.2 GPU 温度（如有）和电池温度显示
  - [x] 5.3 温度趋势小图（最近 30 个采样点的 CPU 温度曲线，单色 #00897B）

- [x] Task 6: Tab 3 — 频率控制面板
  - [x] 6.1 顶部大字显示当前屏幕刷新率
  - [x] 6.2 CPU 核心列表：频率 + 调速器按钮 + 自定频率按钮
  - [x] 6.3 调速器 PopupWindow 选择（已有，确认可用）
  - [x] 6.4 自定义频率 PopupWindow 输入（已有，确认可用）

- [x] Task 7: 验证与修复编译问题
  - [x] 7.1 推送 GitHub Actions 触发 CI
  - [x] 7.2 确认编译通过，APK 可下载

# Task Dependencies
- Task 2 依赖 Task 1（Tab 布局需要新配色）
- Task 3、4、5、6 依赖 Task 2（Tab 框架就绪后填入内容）
- Task 3、4、5、6 可并行开发
- Task 7 在所有 Task 完成后执行