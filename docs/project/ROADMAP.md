# 路线图

| ID | 状态 | 验收条件 |
| --- | --- | --- |
| APP-001 | done | Compose/Material 3 独立工程、包名、双语、图标与正式签名 Release 构建通过 |
| APP-002 | in-progress | HTTP/HTTPS 健康检查、配对、Keystore 令牌保存、自动恢复和断开可用 |
| APP-003 | in-progress | 会话列表/创建、历史、实时增量、发送/steer/取消可用 |
| APP-004 | in-progress | 授权根、目录、带行号文本编辑、Markdown 源码/预览、75%–250% 按钮与双指缩放、ETag、新建、移动、上传、下载、回收站可用 |
| APP-005 | done | 会话历史只呈现正文、思考、上下文和工具摘要；新会话可选择已有 DSH 工作区，或从授权根选择工作目录登记新工作区，再选择 Agent 模式 |
| UI-001 | done | 使用 UI/UX Pro Max 基线完整重构 Compose 表现层；浅色/深色主题、连接、会话、文件、设置、弹窗与自适应导航通过真实设备尺寸验收，业务回调与权限逻辑不变 |
| UI-002 | done | 参考授权 APKS 的内容优先层级、留白、抽屉、底部面板与过渡动效完成第二轮视觉重构；保留 DSH 品牌、业务动作和权限逻辑 |
| UI-003 | done | 收紧全局标题比例、会话列表节奏、聊天消息间距与输入框高度，同时保留至少 44/48dp 的关键触控区域并通过真机截图验收 |
| UI-004 | done | 聊天正文、Markdown 段落/列表/表格、消息留白和输入区再缩小一档，视觉控件更紧凑但操作热区仍符合 Android 触控要求 |
| MODEL-001 | done | 设置页显示 DSH 模板与用户覆盖合并后的有效配置、继承模型和凭据状态，可编辑现有供应商或创建完全自定义供应商；聊天页可选择模型和思考强度 |
| MD-001 | done | 助手正文和展开思考按 Markdown 渲染；文件编辑器带同步行号，Markdown 文件可切换源码与渲染预览 |
| RATE-001 | done | WebSocket 聊天事件按时间窗合并刷新，发送/取消不再立即重复请求会话列表，429 静默退避且不弹阻塞错误框 |
| STREAM-001 | done | App 直接累积插件稳定 `chat.message.delta` 文本/思考增量，完成后以 REST 历史原子收口，不再等待整段回复 |
| AGENT-001 | done | 当前会话显示 Agent；空白会话可切换可用 Preset，首条消息后按 DSH 规则锁定并提供中英文说明 |
| APPROVAL-001 | done | App 显示 DSH 待审批状态，可查看脱敏命令与原因、允许一次或拒绝，高风险操作需二次确认；等待期间会话不得只显示为“运行中” |
| TODO-001 | done | App 从 DSH `todos` 会话投影显示当前任务列表、进行中状态与完成进度，并随实时事件刷新 |
| COMMAND-001 | done | 输入 `/` 时显示当前会话可用命令及说明，支持补全和受控执行，斜杠命令不得作为普通消息发送给模型 |
| PERMISSION-001 | done | 聊天输入区显示当前会话权限预设并可切换 Read Only、Workspace Write、Full access；Full access 必须二次确认 |
| BRAND-001 | done | 设置页以中英双语展示作者信息 `Github@Hakunm` |
| BRAND-002 | done | App 用户可见文案统一使用社区常用简称 DSH，并随 v1.0.0 重新生成正式签名 APK |
| DOC-001 | done | 中英双语用户 README 使用真实手机截图展示导航、聊天、文件编辑和模型供应商，连接地址完成打码 |
| DOC-002 | done | README 扩展为 6 张用户提供的真实手机截图，补充文件浏览和新建工作区界面，并按发布要求删除截图来源说明 |
| LICENSE-001 | done | 项目自身许可证从 MIT 切换为 AGPL-3.0-only，并保留上游图标与 Markdown 渲染器原许可证告知 |
| WORKSPACE-001 | done | App 可查看、重命名和移除 DSH 工作区；移除时明确不会删除服务器目录或文件 |
| SESSION-001 | done | App 会话子项可重命名、分叉和归档；分叉后进入新会话，归档后从默认列表隐藏 |
| IME-001 | in-progress | `Scaffold` 消费已应用的系统 inset，聊天窗口只补剩余 IME inset；输入框在不同厂商键盘上均贴近键盘上沿且列表保留可滚动空间 |
| PARITY-001 | in-progress | `PARITY.md` 所列 DSH WebUI 能力全部完成或有明确版本计划 |
| TEST-001 | done | 单元测试、Compose UI、lint、Release 签名、真机/模拟器端到端通过 |
| CI-001 | done | GitHub Actions 在不持有正式签名密钥时通过 Debug 门禁，同时任何 Release 打包仍必须提供项目专用签名 |
| REL-001 | done | `dsh-android-app` v1.0.0 用户文档、版本展示、正式签名 APK、独立 Git 仓库和 `v1.0.0` 标签完成 |
| REL-002 | done | 创建 GitHub 公开仓库并推送源码；参考高关注 DSH 项目重构中英双语 README，加入真实脱敏截图并发布含正式签名 APK 的 v1.0.0 Release |
| REL-003 | done | v1.0.0 使用最终 DSH 文案、AGPLv3、真实手机截图和正式签名 APK，以单一根提交重写公开 `main` 与标签并替换 Release 产物 |
| REL-004 | done | App 加入 `dsh-plugin` topic；App 与插件 README 刷新后继续以各自单一根提交发布，不保留中间修改历史 |
| COMPAT-001 | done | 适配 DeepSeek Harness `0.2.x` 与 `dsh-workspace` `v1.2.0`：兼容裸 `SessionWireEvent` 历史、`AgentPreset` 默认值、流式增量与对象参数工具摘要，修复断网重连误清本机令牌问题 |
| FEATURE-001 | done | 补全 `dsh-workspace` `v1.2.0` 全部新增能力：会话置顶/取消置顶、归档恢复、全文搜索、递归文件搜索、排队消息编辑/插队/删除、Agent 交互式提问卡片、图片附件发送、动态权限目录、健康能力与自定义供应商删除 |
| UI-005 | done | 基于 `Kyant0/AndroidLiquidGlass`（`backdrop 2.0.0` + `shapes 1.2.0`）重构高密度通透 Apple Liquid Glass 浅色/深色玻璃界面、五槽悬浮液态玻璃底栏、独立工作区页与悬浮玻璃聊天/编辑器控件 |
| REL-005 | done | 发布 `dsh-android-app` `v1.2.0`（versionCode `10200`、compileSdk `37`），更新全部 6 张液态玻璃截图、中英文 README、项目文档、正式签名 APK 与 GitHub Release |
| SEC-001 | done | 保持服务端系统锁文件与符号链接安全拦截边界不变的前提下，App 配对/重连不再因受保护盘符根目录（如 `C:\`、`/`）阻塞进入，并在顶部通知、文件浏览与工作区目录选择器给出明确中英双语安全提示与处理指引 |
| REL-006 | done | 发布 `dsh-android-app` `v1.2.1`（versionCode `10201`），补充桌面客户端连接说明、受保护根目录错误分类单测与正式签名 APK |
