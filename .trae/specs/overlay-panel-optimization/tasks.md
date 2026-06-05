# Tasks

- [x] Task 1: 扩展数据模型，新增 CPU 核心信息与调速器数据
  - [x] 1.1 在 FpsData.kt 新增 `CpuCoreInfo` 数据类（coreId, freqMHz, governor, availableGovernors）
  - [x] 1.2 在 HardwareData 中增加 `cpuCores: List<CpuCoreInfo>` 和 `cpuTemp: Float` 字段
  - [x] 1.3 在 ShellExecutor 中新增 `writeFile(path, content)` 方法

- [x] Task 2: 扩展 HardwareMonitor，支持读取/设置 CPU 调速器
  - [x] 2.1 新增 `readCpuGovernors()` 方法：读取每个核心的调速器及可用调速器列表
  - [x] 2.2 新增 `setCpuGovernor(coreIndex, governor)` 方法：通过 Root Shell 写入调速器
  - [x] 2.3 在 `updateHardwareData()` 中集成 governor 读取，更新 HardwareData

- [x] Task 3: 扩展 FpsMonitor，暴露历史 FPS 数据供图表使用
  - [x] 3.1 新增 `fpsHistory: StateFlow<List<FpsRecord>>` 保留最近 60 个 FPS 采样点
  - [x] 3.2 在采样循环中填充 fpsHistory

- [x] Task 4: 重写悬浮窗面板 UI（白色风格 + 完整布局）
  - [x] 4.1 去掉旧版简单 TextView 展开面板，改为白色面板布局
  - [x] 4.2 实现顶部状态栏：FPS 数字 + 录制状态 + CPU 温度 + 电池温度
  - [x] 4.3 实现左侧 FPS 实时折线图（Canvas 自绘制，蓝色曲线，目标帧率参考线）
  - [x] 4.4 实现右侧 CPU 核心控制面板（频率 + 调速器，点击切换）
  - [x] 4.5 实现底部 CPU 频率曲线图（每条核心线不同颜色）
  - [x] 4.6 收起状态保持原有暗色风格 FPS 数字不变化

- [ ] Task 5: 验证与修复编译问题
  - [ ] 5.1 检查所有文件编译无错误
  - [ ] 5.2 推送到 GitHub Actions 触发 CI 编译
  - [ ] 5.3 确认 APK 构建成功且产物可下载

# Task Dependencies
- Task 2 依赖 Task 1（需要 CpuCoreInfo 数据类和 writeFile 方法）
- Task 3 可并行于 Task 1、Task 2
- Task 4 依赖 Task 1、Task 2、Task 3（需要所有数据模型和采集方法）
- Task 5 在 Task 4 之后