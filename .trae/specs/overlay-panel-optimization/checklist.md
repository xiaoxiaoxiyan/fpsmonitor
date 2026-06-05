# Checklist

- [x] CpuCoreInfo 数据类包含 coreId、freqMHz、governor、availableGovernors 字段
- [x] HardwareData 包含 cpuCores 和 cpuTemp 字段
- [x] ShellExecutor 支持 writeFile 写入 sysfs 节点
- [x] HardwareMonitor 能读取每个核心的调速器和可用调速器列表
- [x] HardwareMonitor 能通过 Root Shell 设置 CPU 调速器
- [x] FpsMonitor 暴露 fpsChartData 保留最近 60 个采样点
- [x] 悬浮窗展开面板使用白色半透明背景，文字为深色
- [x] 顶部状态栏显示 FPS 数字、录制状态、CPU 温度、电池温度
- [x] CPU 温度根据阈值显示不同颜色（绿/橙/红）
- [x] 左侧 FPS 折线图使用蓝色曲线，包含目标帧率参考线
- [x] 右侧 CPU 核心控制面板显示各核心频率和调速器
- [x] 点击调速器可弹出选择列表并切换
- [x] 设备不支持调速器时显示"不支持"并禁用
- [x] 底部 CPU 频率曲线图每个核心使用不同颜色
- [x] 收起状态悬浮窗保持原有暗色风格不变
- [x] 代码编译无错误，APK 构建成功