# 架构

单 Activity Compose App 使用 `HarnessViewModel` 持有连接、工作区、会话、排队状态、交互提问与文件状态。`DshClient` 是唯一网络边界，通过插件 `/api/v1` REST 和 WebSocket 工作。阻塞网络操作只在 `Dispatchers.IO` 执行。

端点与加密令牌由 `SecureConnectionStore` 保存。AES-256-GCM 密钥由 Android Keystore 生成且不可导出；偏好中只保存 IV 与密文，应用数据同时排除在云备份和设备迁移之外。启动恢复连接时仅在服务器明确返回 `401/403` 时清除令牌，临时断网或服务器重启保留令牌并在连接页提供一键重试恢复。

Compose 表现层只消费 `HarnessState` 并调用 `HarnessViewModel` 动作，不持有协议或权限决策。`ui/theme/Theme.kt` 定义浅色玻璃与深色玻璃语义色板、紧凑高密度排版比例（20sp 标题 / 14.5sp 卡片标题 / 13.5sp 正文）、多彩圆角方形图标徽章，并显式提供 `LocalContentColor = colorScheme.onBackground` 与持久化的 `ThemeMode`（`SYSTEM` / `LIGHT` / `DARK`）。`ui/glass/LiquidGlass.kt` 基于 `Kyant0/AndroidLiquidGlass`（`io.github.kyant0:backdrop:2.0.0` + `shapes:1.2.0`）封装 `LiquidGlassStage`、`CrispCard`、`LiquidGlassFloatingBar`、`LiquidGlassDock`、`CompactSearchPill` 与 `CompactFilterTabs`。为杜绝 Skia `RenderNode` 自引用循环崩溃，`LiquidGlassStage` 严格通过 `CompositionLocal` 隔离，只在 `overlay` 浮层注入背景 backdrop，内容区强制为 `null`；全屏详情页（聊天页、代码编辑器）各自创建独立的同级 `rememberLayerBackdrop()`。手机端采用对称五槽悬浮液态玻璃底栏（`会话 | 工作区 | + | 文件 | 设置`），宽屏自动切换为自适应侧栏。

`DshClient` 规范化用户输入的 IP/域名为 `/api/v1`，序列化配置为 `encodeDefaults = true; explicitNulls = false` 以确保图片附件 `type = "image"` 始终编码同时省略空字段。WebSocket 握手向 OkHttp 提供同源 `http://` 或 `https://` URL，由 OkHttp 执行协议升级；实时事件连接失败后按 1、2、4 秒递增退避并封顶 30 秒；REST 始终是重连后的状态恢复来源。

聊天历史模型通过 `HistoryEntrySerializer` 同时兼容包装型 `{ event, view }` 与 DSH `0.2.x` 裸 `SessionWireEvent`；`ChatPresentation` 解析正文、图片块、思考摘要、上下文注入、对象参数工具摘要、TODO 与权限目录，过滤内部事件与 token 数组。实时流式输出同时消费 `chat.message.delta` 与 `agent/assistant-stream` 增量，完成后以 REST 历史原子替换，避免闪烁。聊天页集成图片附件预览、排队消息编辑/插队/移除、Agent 交互式提问卡片（单选/多选/自定义输入）、待审批面板、斜杠命令与动态权限目录。

工作区页独立管理 DSH 工作区列表、重命名、移除登记与新建会话；会话页提供 `全部 / 置顶 / 运行中 / 归档` 标签、工作区筛选芯片、服务端全文搜索以及置顶/重命名/分叉/归档/取消归档菜单。

文件页支持授权根、分页目录、顶部递归文件名搜索、UTF-8/BOM/换行保留、2 MiB 移动编辑上限、同步滚动行号、Markdown 源码/渲染切换、右下角悬浮液态玻璃 `75%–250%` 缩放胶囊与双指缩放、ETag 冲突检测、新建、移动、Okio 流式上传下载、替换、软删除与恢复。

设置页提供三档玻璃外观、中英双语切换、服务端能力列表与供应商管理：读取合并后的有效配置、默认 Base URL、配置错误与凭据状态，支持编辑、发现模型、写入/清除凭据以及创建和删除自定义供应商。
