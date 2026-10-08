# 开发日志

## 2026-10-08 · 兼容 DSH 0.2.x 的字符串枚举契约（下游分支）

- **背景**：上游 `Hakunm/dsh-android-app` v1.0.0 使用 kotlinx.serialization 严格解码。
  `AgentPreset.trust` 为无默认值的必填字段；而 DSH 0.2.x 的 `AgentPreset` / `AgentPresetRow`
  已移除 system/user 二分，**服务端不再返回该字段**。客户端因此对整个列表报错：
  `Field 'trust' is required for type with serial name '...AgentPreset', but it was missing at path: $.items[0]`，
  表现为 Agent 预设选择器完全打不开。
- **做法**：给 `AgentPreset.trust` 加默认值 `"system"`，并同时给 `isDefault` / `available`
  补上安全默认值。这样**服务端缺少任一可选字段时，一个字段的缺失不会再拖垮整个列表**。
  同批在服务端（`dsh-workspace` 的兼容层）也补了该字段，两端互为保险。
- **为什么改客户端而不是只改服务端**：严格解码在遇到未知/缺失字段时是「全有或全无」的。
  客户端不应假定某个具体内核版本一定返回某个可选字段；给可选字段默认值是更稳的防线。
- **构建环境说明**：本次构建在无外网代理的环境下完成，构建时临时使用了国内镜像
  （`settings.gradle.kts` 指向阿里云与 `dl.google.com`，Gradle wrapper 指向腾讯镜像）。
  **这些改动未包含在本分支中**——本分支相对上游只保留与修复直接相关的文件，
  以便差异最小、便于审阅。
- **验证**：
  - `./gradlew assembleRelease` 构建成功，产物 `app-release.apk` 2,561,512 字节，
    与上游 v1.0.0 产物同尺寸（R8 优化后恰好一致）。
  - 逐 dex 比对确认改动**确实编译入包**：`classes.dex` 由 3,709,572 字节变为 3,709,696 字节
    （+124，即新增默认值产生的字节码），`AgentPreset` 符号在包内存在。
  - APK 已用自签密钥签名并验签通过（`CN=DSH Pocket Client`，
    SHA-256 `4e7fa1db395c4a2436e7a96fcdf6c606e20a14bad55dfcd60c80892f4f92deb3`）。
  - 装机实测：连接 DSH 0.2.x + `dsh-workspace` 兼容层后，Agent 预设列表可正常打开，
    会话模型、历史记录、流式输出、命令菜单均正常。
- 产物 `DeepSeek-Harness-compat02-release.apk`（SHA-256 `4df87f47…`）随分支提供；
  签名密钥 `dsh-release.jks` **不进入仓库**。

## 2026-08-15 · v1.0.0

- 完成 `DOC-002` 与 `REL-004`：README 扩展为 6 张用户提供的真实手机截图，新增文件浏览与新建工作区界面，删除中英文截图来源说明；GitHub topics 加入 `dsh-plugin`，公开分支和标签继续保持单一根提交。
- 完成 `BRAND-002` 与 `LICENSE-001`：App 用户可见简称统一为 `DSH`，项目自身许可证改为 `AGPL-3.0-only`；上游鲸鱼图标与 Markdown 渲染器继续按各自许可证保留告知。
- 完成 `DOC-001`：README 改用用户在真实手机上提供的导航、聊天、文本编辑和模型供应商截图；连接地址已打码，模拟器截图不进入正式仓库。
- Oracle 演示服务器已清除发布前的会话历史和截图过程产生的临时会话，只保留一个本次发布创建的“你好”演示会话；DSH WebUI 与远程 listener 均恢复正常。
- 完成 `REL-003`：v1.0.0 正式签名 APK、README、截图和许可证统一后，以单一根提交重写公开分支与标签，并替换 Release 产物。
- 完成 `WORKSPACE-001`：会话列表页新增 DSH 工作区管理入口，可重命名和移除登记，删除确认明确保留服务器目录、文件和会话日志。
- 完成 `SESSION-001`：每个会话子项增加重命名、分叉和归档；分叉后自动进入子会话，归档后刷新默认列表并保留日志。
- 正式项目名调整为 `dsh-android-app`，版本升级为 `1.0.0` / versionCode `10000`，设置页展示运行版本；新增 GitHub Android CI 和中英双语用户 README。
- `docsCheck testDebugUnitTest lintDebug assembleRelease` 通过；正式 APK 为 2,561,512 字节，SHA-256 `823344D6CEAFF9FE030F625335191D52ABE97FB6D02AFD2B9555B12680E4A266`，v2 签名和既有独立 4096 位 RSA 证书验证通过。
- 完成 `REL-002`：在保留用户手动删改的基础上使用 Humanizer-zh 重写中文 README，拆出完整 `README.en.md`，把安装、连接和核心操作前置，并加入会话、会话菜单与工作区管理三张真实 App 截图。
- 截图不包含设备令牌、API 密钥或服务器连接地址；包含连接 IP 的抽屉采样未进入仓库。
- 创建 `Hakunm/dsh-android-app` 公开仓库并推送 `main`，设置 `android`、`deepseek-harness`、`jetpack-compose`、`material3`、`vibe-coding` topics。
- 发布 GitHub `v1.0.0` Release，上传正式签名 `DeepSeek-Harness-v1.0.0.apk` 与 `SHA256SUMS.txt`。
- 完成 `CI-001`：Release 签名校验从 Gradle 配置阶段移到 Release 打包任务前。公开 CI 可在不持有正式私钥的情况下验证 Debug；`packageRelease` 与 `bundleRelease` 仍依赖专用签名检查，修复后的 hosted run 已成功。

## 2026-08-15

- 完成 `TODO-001`、`COMMAND-001` 和 `PERMISSION-001`：聊天页展示 DSH TODO 完成进度与任务状态，输入 `/` 时提供当前会话 host 命令补全并走受控 BFF 执行，输入区增加只读/工作区写入/完整访问权限选择器；完整访问必须二次确认。
- 完成 `BRAND-001`：设置页的“关于”分区以中英文展示 `Github@Hakunm`。
- 正式签名 Release 已覆盖安装到 Android 15 尺寸设备，并连接更新后的 Oracle listener 完成权限选择器和 `/` 命令候选视觉验收；APK v2 与既有独立证书保持一致。
- Oracle 服务器已载入审批版插件并重启 DSH；App 下一次收到审批请求即可通过稳定 REST/WS 展示并决定，不再依赖旧插件。旧运行随用户授权的重启终止。

## 2026-08-14

- 完成 `APPROVAL-001`：App 通过插件稳定 BFF 恢复待审批状态，会话列表改为“等待审批”，审批面板接管输入区并显示脱敏原因/命令；提供“拒绝/允许一次”，`danger-full-access` 必须勾选风险确认，不提供永久允许。WebSocket 审批变化会刷新 REST 状态，决定过程使用显式 UI 状态而非通用 busy。
- 记录 `APPROVAL-001`：通过 App 发起的真实 Skill 安装停在 DSH `approval/asked`，证明当前移动端缺少审批交互会将等待授权误呈现为持续运行；服务器与命令均未异常，恢复需在 WebUI 允许一次或拒绝。
- 完成 `STREAM-001`、`AGENT-001` 与 `IME-001`：App 直接呈现 token 级文本/思考增量并在完成后以历史正文收口；会话配置加入仅空白会话可用的 Agent 切换；聊天输入区移除与窗口 `adjustResize` 重复的 IME padding，修复键盘上方整块空白。
- 用户实机确认完全移除聊天 IME inset 会让键盘覆盖输入框，`IME-001` 因此重新打开：父级 `Scaffold` 现在消费已应用的系统 inset，聊天列只应用剩余 IME inset，避免“完全不抬升”和“重复抬升”两个极端；最终状态等待用户实际键盘复验。
- 使用 Android 15 发布版连接真实 Oracle listener 完成流式视觉采样：连续帧中的助手正文从 `1...34` 增长到 `1...120`，最终 REST 历史正常接管且无 429/崩溃；已开始会话的 Agent 面板同步完成锁定状态验收。
- 扩展 `APP-004`：文字源码与 Markdown 预览新增共用的 75%–250% 缩放状态，支持缩小、百分比显示、重置、放大和双指捏合；字体与行高同步缩放，行号沟槽随字号调整，单指滚动、编辑和 ETag 保存路径保持不变。

- 扩展 `APP-005`：新建会话增加“已有工作区/新建工作区”模式；新建模式从授权根浏览工作目录，经插件登记 DSH 工作区后直接创建会话，仍不会把文件根误当作 WebUI 工作区。
- 扩展 `MODEL-001`：现有供应商编辑显示 DSH 有效配置、凭据引用和继承模型；新增完全自定义供应商表单，协议选项由服务器 schema 提供。
- 扩展 `MD-001`：文字文件编辑加入同步行号，Markdown 文件支持源码/渲染预览切换。
- 完成 `UI-004`：聊天 Markdown 正文与列表统一到紧凑正文层级，缩小消息留白、用户气泡、宽屏会话栏和输入正文区，保留关键按钮的触控热区。
- Android 15 最终复验发现新建工作区长表单会把创建按钮挤出屏幕；现已收紧段间距与目录列表高度，使根目录、目录选择、Agent 和底部创建按钮在同一手机视口内完整可操作，并重新完成 lint、Release、签名和覆盖安装。

- 创建独立 Android 工程并锁定应用名、包名、SDK、Compose/Material 3 和双语基线。
- 从 DSH WebUI 上游 favicon 提取黑色鲸鱼路径作为 adaptive/monochrome launcher icon，并保留 MIT 许可。
- 在仓库外生成独立 4096 位 RSA PKCS12 签名，未使用 Android debug 测试签名。
- 建立持久化项目追踪与 DSH WebUI 功能对等矩阵。
- 完成配对/恢复、Android Keystore 令牌保护、scope 感知和 HTTP/HTTPS 客户端；WebSocket 使用封顶 30 秒的指数退避重连。
- 完成自适应会话与文件界面：创建/历史/发送/steer/取消，以及 ETag 编辑、新建、移动、上传下载、替换、软删除和恢复。
- 添加 OkHttp 合约单测并通过 lint、R8 Release 构建、APK 包名与独立证书签名核验；真机端到端仍待执行。
- 对本机真实插件 listener 完成临时设备/根回归，验证一次性配对、scope、ETag 文件更新、移动、软删除/恢复和会话授权过滤，并在结束后撤销测试授权。
- 文件上传下载改为 Okio 流式传输，补充目录分页、操作菜单替换文件的 ETag 获取、纯 CR 换行保留和 NUL 二进制识别；Compose 默认中文首屏测试 APK 编译通过。
- 在 Android 15 上运行发布版并通过真实插件完成健康检查、一次性配对、设备/scope/根加载、应用重启恢复和主导航验收；临时设备与根已撤销清理。
- 真机配对发现 WebSocket 请求提前改写 `ws://` 导致 OkHttp 拒绝，现改为传入 HTTP(S) 握手 URL 让 OkHttp 执行 Upgrade，并新增第 6 项回归单测。
- Compose 仪器测试首次因真实配对状态残留而无法进入连接首屏；清理测试前置状态后重跑通过，测试文档明确要求干净安装。
- 完成 `APP-005`：聊天历史增加强类型事件 presentation 层，隐藏 `step/end`、`turn/end`、token 数组等内部原始 JSON，只保留正文、思考、上下文注入与工具摘要；新建会话改为读取并选择 DSH 工作区和 Agent Preset，不再把文件授权根当作工作区，也无需手填路径或模式 ID。
- Android 15 发布版完成 `APP-005` 视觉验收：空工作区、临时非空 DSH 工作区、默认 Agent 和四种模式列表均按真实 listener 数据渲染；测试工作区与设备随后清理，重新生成并核验正式签名 APK。
- 完成 `UI-001`：安装并采用 UI/UX Pro Max 的 Compose 设计建议，重构浅色/深色主题、应用栏、自适应导航、连接表单、会话列表与消息、文件浏览与编辑操作、设置分区及弹窗；所有业务 ViewModel、网络协议、scope 判断和文件回调保持不变。Android 15 的 1080×1920 实际截图复核通过。
- 完成 `UI-002`：分析用户提供的参考 APKS 及实际运行层级，将内容优先留白、移动端抽屉、宽屏固定侧栏、行式列表、底部面板与克制过渡应用到 DSH 自有品牌界面；没有复制第三方商标、资源或字体，并持久化设计系统。
- 完成 `MODEL-001`：设置页新增供应商配置、凭据状态、只写密钥和模型发现；聊天页新增真实可路由模型与思考强度底部面板，均由 `settings.read/settings.write` 和 session/root 授权保护。
- 完成 `MD-001`：集成 `multiplatform-markdown-renderer-m3` 0.41.0，助手正文及展开思考支持标题、列表、引用、代码和表格；补充 NOTICE 与 Apache-2.0 全文。
- 完成 `RATE-001`：聊天增量事件改为合并刷新，会话列表只在生命周期或本地主动动作后刷新，429 静默退避；所有错误从阻塞对话框改为 Snackbar。真实发送与 24 秒观察无 429 或崩溃。
- ARM64 Oracle 上的插件 tarball 已更新并重载；真实 API、Android 15 模型/供应商/Markdown/消息发送、10 项单测、lint、R8 Release、正式签名和最终覆盖安装均通过。
- 完成 `UI-003`：将全局标题、正文、会话行、消息留白、用户气泡和聊天输入容器收紧；为 Markdown 增加独立紧凑 H1-H6 映射，避免聊天正文标题使用库的超大默认 display 样式。Android 15 实际会话截图确认内容密度提升，44/48dp 关键触控区域不变。
