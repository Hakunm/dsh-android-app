# 测试记录

- 2026-11-21 `v1.2.0` 正式发布验证：
  - Temurin 17 + Android SDK 37 下 `docsCheck testDebugUnitTest lintDebug assembleRelease` 全部通过，8 份文档 / 37 个任务一致，R8 Release 与专用签名验证通过。
  - 单元测试覆盖 `dsh-workspace@1.2.0` 全部新增契约（递归文件搜索、归档/置顶/取消置顶、会话全文搜索、动态权限目录、会话排队状态更新、交互提问读取与提交、图片附件发送、自定义供应商删除、DSH `0.2.x` 裸 `SessionWireEvent` 与流式增量、缺省 `AgentPreset.trust` 字段）。
  - 正式签名 APK：`artifacts/DeepSeek-Harness-v1.2.0.apk`，2,652,224 字节，SHA-256 `4D11A171C73176D2F2B01140CE0CF38533EFDF13C26F65AA0D173E5CCE89EAB4`；`apksigner verify --verbose` 验证 APK Signature Scheme v2，单一 Hakunm 4096 位 RSA 正式证书（SHA-256 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`）；`aapt` 确认包名 `io.github.hakunm.deepseekharness`、versionCode `10200`、versionName `1.2.0`、min SDK 26、target SDK 36、compile SDK 37。
  - MuMu Android 15 模拟器端到端验收：连接真实 `@deepseek-ai/dsh@0.2.2-rc.5` + `dsh-workspace@1.2.0` 验证一次性配对、WebSocket 实时同步、新建工作区与会话、Agent 模式、动态权限目录、授权根目录浏览、Markdown 预览与源码编辑器、模型供应商与浅色/深色液态玻璃切换，`logcat` 零崩溃；并完成 6 张全新液态玻璃界面截图逐张人工复核，确认无敏感路径、令牌或个人 IP。
- 2026-08-15 README 六图复验：中英文 README 均引用 6 张存在的 JPG；新增图片与用户原始真机截图 SHA-256 一致，截图来源说明已删除。
- 2026-08-15 最终发布刷新：Temurin 17 下 `docsCheck testDebugUnitTest lintDebug assembleRelease` 通过，8 份文档/31 个任务一致，lint、R8 Release 与专用签名门禁全部成功。正式 APK 为 2,561,512 字节，SHA-256 `80940602D79ACB68D5805122FEC4E6678F39ADEF02DEDAEE4DD7B4856CD7DBBE`。
- 2026-08-15 README 图片复验：使用用户提供的 4 张真实手机截图展示导航、聊天、带行号/缩放的文本编辑器和模型供应商；连接地址已打码，未采用模拟器截图。Oracle 会话存储与工作区注册表均确认只保留 1 个本次发布演示会话。
- 2026-08-15 v1.0.0：`docsCheck testDebugUnitTest lintDebug assembleRelease` 全部通过；MockWebServer 新增工作区重命名/移除和会话重命名/分叉/归档请求契约。APK 包名 `io.github.hakunm.deepseekharness`、versionCode `10000`、versionName `1.0.0`、min SDK 26、target SDK 36；2,561,512 字节，SHA-256 `823344D6CEAFF9FE030F625335191D52ABE97FB6D02AFD2B9555B12680E4A266`。APK Signature Scheme v2、单一 Hakunm 4096 位 RSA 正式证书，证书 SHA-256 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`。
- 2026-08-15 README 发布复验：Temurin 17 下 `docsCheck testDebugUnitTest` 通过，8 份恢复文档/25 个任务一致；三张已入库 App 截图逐张检查，无设备令牌、API 密钥或服务器连接地址。
- 2026-08-15 `CI-001`：首个 GitHub hosted run 暴露 Release 签名校验发生在 Gradle 配置阶段，导致无私钥的 `assembleDebug` 也失败。本地以不存在的 `dshSigningProperties` 复现后修复：`docsCheck testDebugUnitTest lintDebug assembleDebug` 通过；`assembleRelease --dry-run` 包含 `verifyDedicatedReleaseSigning -> packageRelease`，验证任务在缺少签名时以 `Dedicated signing config is required` 失败，且配置缓存可正常保存。修复提交的 [GitHub Actions run 31834143207](https://github.com/Hakunm/dsh-android-app/actions/runs/31834143207) 在 Ubuntu hosted runner 成功。

必须通过：

```powershell
.\gradlew.bat docsCheck testDebugUnitTest lintDebug assembleRelease
apksigner verify --verbose app\build\outputs\apk\release\app-release.apk
```

## 2026-08-15

- 完整门禁 `docsCheck testDebugUnitTest lintDebug assembleRelease` 通过，8 份文档/22 个任务一致；Release APK 为 2,538,256 字节，SHA-256 `E9FD6DEBD8B5DAD094273E9EDEBB49BF551D73103941B28C77FDC0A153DD5C65`。`apksigner` 验证 APK v2、单一签名者、4096 位 RSA 正式证书，证书 SHA-256 仍为 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`。
- Android 15 尺寸设备覆盖安装后连接真实 Oracle 新版 listener：空白会话显示当前“工作区写入”权限选择器；输入 `/` 后列出 `compact/export/feedback/goal/permission` 等当前 host 命令，已知内置命令说明按 App 语言显示中文或英文，截图保存为 `.tmp/feature-permission-chat.png` 和 `.tmp/feature-slash-commands-localized.png`。当前服务器没有含 TODO 的运行，任务模块由 projection 单测覆盖，真实实时状态留待下一次产生 TODO 的任务。
- `TODO-001`、`COMMAND-001`、`PERMISSION-001` 与 `BRAND-001` JVM/Lint 门禁通过：`testDebugUnitTest lintDebug` 成功；投影单测覆盖 TODO 状态与权限选项，MockWebServer 验证 `/chat/sessions/{id}/commands` 列举/执行路径和命令正文。Compose 编译与中英双语资源检查通过。
- Oracle root `web` profile 已安装审批版插件并按用户明确要求重启；`3080` WebUI、`3090` listener、健康检查、审批 OpenAPI 和 Host bundle 哈希通过。旧待审批会话因重启结束，真实 App 允许一次/拒绝验收将使用新会话。

## 2026-08-14

- `APPROVAL-001` 门禁通过：DshClient 新增待审批读取和单次决定合约测试；`docsCheck testDebugUnitTest lintDebug assembleRelease` 全部成功，Release 已覆盖安装到 Android 15 测试设备。APK 为 2,510,044 字节，SHA-256 `C59D70F965A65EDAFDC847839E4E954111A736F6C77F1F85E63EA219F622F96B`。真实 Oracle 插件包因活动会话仍在等待审批而只暂存未重载，真实决定验收延后且没有替用户批准/拒绝。
- `APPROVAL-001` 真实故障取证：Oracle DSH、`3080/3090` 和 Node 事件循环均健康；最新会话尾事件为 Bash `approval/asked`，请求把 Skill 安装到工作区外的 `/root/.dsh/skills` 并申请 `danger-full-access`。App 尚无审批 BFF/UI，因此只显示“运行中”且无后续正文。确认安装命令尚未执行、目标 Skill 目录不存在、DSH 下无安装子进程；服务器状态未修改。
- `IME-001` 实机反馈后的第二轮修复：Connected `Scaffold` 在应用 `PaddingValues` 后调用 `consumeWindowInsets`，聊天列再使用 `imePadding`，因此只补父级尚未处理的 IME 高度；Composer 自身 3dp 外边距保留，使输入区位于键盘上沿少许而不贴死。`docsCheck testDebugUnitTest lintDebug assembleRelease` 全部通过并覆盖安装。测试镜像的 Sogou IME 虽报告 `mInputShown=true`，其窗口 frame 仍为 `[0,1920][1080,1920]` 零高，最终键盘视觉间距等待用户实际输入法确认。
- 本轮 Release APK 为 2,471,396 字节，SHA-256 `1369EF2CDC6E37225C258A18EB1CAA6AF98FEF140428B4EC440ADAE6A30D5425`；APK v2、单一 4096 位 RSA 正式证书和覆盖安装验证通过。
- `STREAM-001` Android 15 真实 listener 验收：通过 App 发送 120 个数字的长回复并以约 350 ms 间隔采集 28 帧；正文从空白增长至 `1...34`，随后增长至 `1...120`，证明 Compose 直接消费 `chat.message.delta`，而不是等待 REST 返回完整消息。最终持久正文正常收口，logcat 无 `FATAL EXCEPTION`、`RATE_LIMITED` 或 App 崩溃。
- `AGENT-001` Android 15 界面验收：已开始会话展示当前 `PTC` Agent，其他 preset 保持可查看但不可点击，并显示“会话开始后 Agent 配置由 DSH 锁定”；服务端真实探针确认空白会话可切换，首条消息后返回 `409 AGENT_PRESET_LOCKED`。
- `IME-001` 第一轮方案曾完全移除聊天 `imePadding`，但用户实机确认该方案会使输入框被键盘覆盖，已由本节顶部记录的“父级消费、聊天补剩余 inset”方案取代；测试镜像的 Sogou IME 窗口为零高，无法替代用户实际输入法的最终视觉复验。
- 最终门禁 `docsCheck testDebugUnitTest lintDebug assembleRelease` 通过：8 份恢复文档、17 个任务一致，JVM 单测、lint、R8 Release 全部成功。APK 为 2,471,396 字节，SHA-256 `764FA914AF1B43C5A83B8FA16A27C835A0A8788114E85F699B0FAA32D006A0F6`；APK v2 签名、独立 4096 位 RSA 证书和覆盖安装复验通过。
- `testDebugUnitTest`：通过；新增插件稳定 `chat.message.delta` 的中文分片累积测试，以及现有会话 Agent Preset `PUT` 客户端合约。
- `testDebugUnitTest`：通过，10 项测试覆盖地址规范化、健康检查、配对、Bearer、Unicode wire path、ETag 写入、流式上传下载、历史分页、WebSocket 握手、DSH 工作区/Agent Preset、聊天事件投影、会话模型/思考强度以及供应商读取/更新/模型发现合约。
- `docsCheck`：通过，8 份恢复文档和 12 个稳定任务 ID 一致。
- `lintDebug`：通过，无 error。保留的 `InsecureBaseConfiguration` 是允许 HTTP 直连的产品决策；界面持续显示明文风险，HTTPS/VPN 仍为推荐配置。
- `assembleRelease`：通过 R8、资源压缩和签名配置验证。
- `apksigner verify --verbose --print-certs`：v2 签名通过，唯一签名者为独立 4096 位 RSA 证书，证书 SHA-256 为 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`。
- `aapt dump badging`：确认包名 `io.github.hakunm.deepseekharness`、version `0.1.0`、min SDK 26、target SDK 36。
- `UI-002` 重构后的最终 Release APK：2,442,576 字节，文件 SHA-256 为 `37EC1424D1381AE7E0C7276D17304F024080605BC8A254B304900A3961CEB3BC`。
- `connectedDebugAndroidTest`：Android 15 设备通过 1 项 Compose 测试，验证首次启动默认中文、连接表单和中英文双向切换。一次运行因设备保留了真实配对状态而无法看到连接首屏，撤销临时设备并在干净安装后重跑通过。
- 本机真实插件 listener `http://127.0.0.1:3090/api/v1`：临时根和一次性设备完成配对、5 scope、根过滤、文件创建/读取、ETag 更新、移动、软删除、回收站列表、恢复及会话过滤；随后撤销 2 个测试设备并移除临时根。
- Android 15 发布版通过 `adb reverse tcp:3090 tcp:3090` 连接真实插件：健康检查、一次性配对、令牌恢复、WebSocket 握手、会话空状态、授权根列表及设置页均通过；发现 `unexpected scheme: ws` 后改为由 OkHttp 从 HTTP(S) URL 执行 Upgrade，并增加回归单测。临时设备已撤销、临时根已移除。
- 更新后的真实插件返回 4 个 DSH 系统 Agent 模式；本地 DSH 未登记会话工作区时 `/chat/workspaces` 返回空列表，确认 App 不再把插件文件授权根标签显示为新会话工作区。临时合约设备随后撤销。
- Android 15 发布版实际打开新建会话对话框：空列表显示“没有可用的 DSH 工作区”且禁用创建；临时在 DSH WebUI 登记插件源码工作区后，App 显示标题 `dsh-android-app-workspace` 并启用创建，Agent 下拉完整显示服务器默认、标准、PTC、极简和创造模式及说明。临时工作区、设备授权和 App 数据在验收后清理。
- 真实 DSH 空白会话只含 `permission/preset`、`sandbox/mode`、`approval/policy` 和 `session/end-seed` 初始化事件；Android 15 会话页显示“暂无可显示的正文”，未再回显这些内部事件 JSON。该会话没有可用于视觉验收的用户/助手正文，非空正文与工具投影由单元回归覆盖。
- 已人工检查连接页、会话页、文件页和设置页的 1080×1920 截图，无文字遮挡或横向溢出。
- `UI-001` 历史基线曾在 Android 15 的 1080×1920 视口检查连接、会话、文件、设置和新建会话；该导航结构已由 `UI-002` 的移动端抽屉和宽屏固定侧栏取代。测试只创建临时设备授权，未更改 DSH 原始工作区或插件根。
- `UI-001` 最终验证命令 `docsCheck testDebugUnitTest lintDebug assembleRelease connectedDebugAndroidTest` 通过；文档校验为 8 份文档/8 个任务，Android 15 Compose 测试 1/1 通过。APK v2 证书 SHA-256 仍为 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`。
- `MODEL-001` 真实 Oracle 合约探针通过：返回 37 个供应商、6 个授权会话、当前 `opencode-go/deepseek-v4-pro/max` 和 2 个可路由模型组；Android 15 模型面板显示模型及 Off/High/Max，并成功切换到 High。供应商页显示凭据状态和配置入口，不回显密钥。
- `MD-001` 使用真实 DSH 回复验收：标题和表格按排版/表格渲染，界面未显示原始 `##`、`**` 与管道表格标记。`multiplatform-markdown-renderer-m3` 固定为 `0.41.0`；`0.43.0` 要求 compile SDK 37，与当前 SDK 36 基线不兼容，因此未采用。
- `RATE-001` 使用真实会话发送 `Reply with: streaming refresh OK`，正文正常增量完成；连续观察 24 秒无 429 UI、无 `RATE_LIMITED` 日志、无崩溃。最终又移除发送/取消后的同步会话列表请求，改为合并队列刷新；重新构建并覆盖安装后启动日志仍无 `FATAL EXCEPTION` 或 `RATE_LIMITED`。
- `UI-002` 已在 1080×1920 Android 15 设备检查会话列表、聊天、模型面板、供应商设置、文件页和 Markdown；最终安装截图为 `.tmp/dsh-final.png`，UI 层级为 `.tmp/dsh-final.xml`，没有横向溢出、底部控件裁切或文字遮挡。
- 最终命令 `docsCheck testDebugUnitTest lintDebug assembleRelease` 通过；APK v2 唯一签名者为 4096 位 RSA 独立证书，证书 SHA-256 仍为 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`。
- `UI-003` 在 1080×1920 Android 15 设备验收：全局标题从 30sp 级收紧至 24sp 级，主要聊天正文为 15sp/22sp，消息间距降至 8dp，输入正文区从 74dp 降至 44dp；空输入容器屏幕高度从约 366px 降至约 303px。Markdown 使用独立紧凑排版表，H1-H6 映射至 App 的 24/20/18/15/13sp 层级，“系统环境”等标题不再异常放大。
- `UI-003` 最终截图为 `.tmp/compact-final-list.png` 与 `.tmp/compact-final-chat.png`；发送按钮保持 44dp，导航/新增按钮保持 48dp。最终 APK 为 2,442,576 字节，SHA-256 `7ABD069C7653686E5413B74E3A1862BEF18C0568E06BE3ECCFF0AAFD4B755515`，正式证书与签名方案不变。
- 工作区/供应商/文件编辑扩展通过 `docsCheck testDebugUnitTest lintDebug assembleRelease`：新建会话可从授权根浏览目录并调用插件登记 DSH 工作区；供应商合约覆盖自定义能力/创建；文字编辑器显示同步行号，Markdown 可切换源码与渲染预览。
- `UI-004` 在 1080×1920 Android 15 复验：聊天输入容器由约 101dp 进一步降至 88dp，用户气泡正文与 Markdown 段落统一为 14sp/20sp，消息区间距为 5dp；新建工作区弹窗压缩标题间距和目录列表后，Agent 下拉与“创建工作区并开始会话”按钮完整显示。截图为 `.tmp/final-compact-chat.png`、`.tmp/feature-line-numbers.png`、`.tmp/feature-markdown-preview.png` 和 `.tmp/final-new-workspace-fixed.png`。
- 最终 Release APK 为 2,467,484 字节，SHA-256 `22882C77C519B70FDC65F9FC4CCC54E90A5BA999344C88D014DAA2A217770179`；APK v2 签名通过，唯一 4096 位 RSA 证书 SHA-256 仍为 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1`，覆盖安装成功。
- `APP-004` 文字缩放扩展通过 `docsCheck testDebugUnitTest lintDebug assembleRelease`。Android 15 在 `.bashrc` 源码验证 `100%→150%→100%`，行号沟槽、字体与行高同步变化；Markdown 验收文件验证预览 `100%→150%`，切换回源码仍保持 `150%`。双指处理仅消费两个及以上指针，按钮作为无手势替代。截图为 `.tmp/zoom-source-100.png`、`.tmp/zoom-source-150.png`、`.tmp/zoom-markdown-100.png` 和 `.tmp/zoom-markdown-150.png`；服务器临时文件随后删除。
- 当前 Release APK 为 2,468,808 字节，SHA-256 `EE2676CF422FBDE74730258AA0CF4866CDCF65268DB54716960C9F826A36BD51`；APK v2 签名及唯一 4096 位 RSA 正式证书 SHA-256 `A92938B33F59D90993ADA96437F8A8DEF987A27353E46A018661DDC1587046B1` 复验通过并已覆盖安装。
- 未完成：局域网 HTTPS、steer/取消、文件编辑与陈旧 ETag 冲突、上传下载、WebSocket 网络切换、旋转/进程恢复及平板实机布局仍待端到端执行。
