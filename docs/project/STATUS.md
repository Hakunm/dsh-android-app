# 当前状态

- 日期：2026-11-21
- 阶段：`dsh-android-app` v1.2.0 正式发布（DSH 0.2.x 兼容 + `dsh-workspace@1.2.0` 全特性 + Apple Liquid Glass UI 重构）
- 当前任务：`COMPAT-001`、`FEATURE-001`、`UI-005`、`REL-005`、`IME-001`、`PARITY-001`
- 插件基线：`dsh-workspace@1.2.0`（DeepSeek Harness `0.2.x`）
- 包名：`io.github.hakunm.deepseekharness`
- Android 基线：min SDK 26、target SDK 36、compile SDK 37、JDK 17
- 已完成：Apple Liquid Glass（`Kyant0/AndroidLiquidGlass` `backdrop 2.0.0` + `shapes 1.2.0`）浅色/深色/跟随系统三档主题、五槽悬浮液态玻璃底栏、独立工作区页、会话置顶/归档恢复/全文搜索、递归文件搜索、排队消息编辑/插队/删除、Agent 交互式提问卡片、图片附件发送、动态权限目录、自定义供应商删除、断网容错重连与中英双语
- 本次刷新：全部 6 张 README 截图替换为 MuMu Android 15 实测新界面截图（浅色与深色液态玻璃），同步更新中英双语 README、NOTICE 与 CI SDK 37 配置
- 最近验证：Temurin 17 下 `docsCheck testDebugUnitTest lintDebug assembleRelease` 通过，8 份文档与 37 个任务一致；R8 Release、专用签名与 MuMu Android 15 连接真实 `@deepseek-ai/dsh@0.2.2-rc.5` + `dsh-workspace@1.2.0` 端到端验证通过
- 正式产物：`artifacts/DeepSeek-Harness-v1.2.0.apk`，2,633,732 字节，SHA-256 `CC9C5C36F421DAE3C4C82E93F8E23629E69C8A9C13E4BBDC80E7D82112C2C5A4`
- 签名：APK Signature Scheme v2，单一 Hakunm 4096 位 RSA 证书；证书 SHA-256 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`（与 v1.0.0 一致，支持直接覆盖升级）
- GitHub：`https://github.com/Hakunm/dsh-android-app`，topic 包含 `dsh-plugin`
- Release：`https://github.com/Hakunm/dsh-android-app/releases/tag/v1.2.0`
- 阻塞项：`PARITY.md` 中目标/子代理/技能/后台任务仍待服务端 BFF；`IME-001` 持续关注不同厂商输入法适配
- 下一步：继续跟进 DSH 后续版本的新能力并在真机矩阵补充 HTTPS 与网络切换验证
