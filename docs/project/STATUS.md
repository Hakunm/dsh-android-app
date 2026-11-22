# 当前状态

- 日期：2026-11-22
- 阶段：`dsh-android-app` v1.2.1 正式发布（DSH 桌面客户端/WebUI 兼容 + 受保护盘符根目录安全提示优化 + 双模式极简界面）
- 当前任务：`COMPAT-001`、`FEATURE-001`、`UI-005`、`SEC-001`、`REL-006`、`IME-001`、`PARITY-001`
- 插件基线：`dsh-workspace@1.3.0`（兼容 `1.2.0+`，DeepSeek Harness `0.2.x`）
- 包名：`io.github.hakunm.deepseekharness`
- Android 基线：min SDK 26、target SDK 36、compile SDK 37、JDK 17
- 已完成：清透冰瓷（浅色）/ 静谧夜枭（深色）/ 跟随系统三档自动双模式主题、左侧无框导航抽屉、95% 留白极简对话主页、`+` 添加到对话圆角面板、独立工作区页、会话置顶/归档恢复/全文搜索、递归文件搜索、排队消息编辑/插队/删除、Agent 交互式提问卡片、单次越界命令审批卡片、受保护盘符根目录（如 `C:\`、`/`）非阻塞进入与中英双语安全提示、断网容错重连与中英双语
- 本次刷新：保留服务端系统锁文件与符号链接拦截边界不变，修复挂载受保护根目录时阻塞配对/重连进入主界面的问题，在顶部通知、Code 文件页与新建工作区选择器展示明确安全指引并优化配对码失效提示，发布 `v1.2.1`
- 最近验证：Temurin 17 下 `docsCheck testDebugUnitTest lintDebug assembleRelease` 通过，8 份文档与 39 个任务一致；R8 Release 与专用签名校验通过
- 正式产物：`artifacts/DeepSeek-Harness-v1.2.1.apk`，2,675,916 字节，SHA-256 `20EDB6CF8AE6D3735E9ABDA29CF5CC88BE78EB76DFC2A38F1E464DDDA7E427B5`
- 签名：APK Signature Scheme v2，单一 Hakunm 4096 位 RSA 证书；证书 SHA-256 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`（与 v1.0.0 / v1.2.0 一致，支持直接覆盖升级）
- GitHub：`https://github.com/Hakunm/dsh-android-app`，topic 包含 `dsh-plugin`
- Release：`https://github.com/Hakunm/dsh-android-app/releases/tag/v1.2.1`
- 阻塞项：`PARITY.md` 中目标/子代理/技能/后台任务仍待服务端 BFF；`IME-001` 持续关注不同厂商输入法适配
- 下一步：继续跟进 DSH 后续版本的新能力并在真机矩阵补充 HTTPS 与网络切换验证
