# dsh-android-app

<p align="center">
  <strong>把你自己的 DeepSeek Harness 带到 Android 手机上。</strong>
</p>

<p align="center">
  中文 · <a href="./README.en.md">English</a>
</p>

<p align="center">
  <img alt="Version" src="https://img.shields.io/badge/version-v1.0.0-087f8c">
  <img alt="Android" src="https://img.shields.io/badge/Android-8.0%2B-3ddc84">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/UI-Jetpack_Compose_%2B_Material_3-6750a4">
  <img alt="License" src="https://img.shields.io/badge/license-AGPL--3.0-2da44e">
</p>

`dsh-android-app` 是 DeepSeek Harness 的原生 Android 客户端。它通过 [dsh-workspace](https://github.com/Hakunm/dsh-workspace) 连接你自己的 DSH WebUI，让手机与网页端使用同一套工作区、会话、模型和文件。

你可以在手机上继续聊天、查看任务进度、处理审批、切换权限与模型，也可以直接浏览和编辑服务器允许访问的文件。App 默认中文，可在设置中切换 English。

> **DSH 0.2.x 兼容性**：本分支在上游 `v1.0.0` 的基础上修复了与 DSH 0.2.x 的枚举契约不匹配问题。
> 0.2.x 的 `AgentPreset` 不再返回 `trust` 字段，而客户端严格解码要求该字段必填，
> 导致 Agent 预设选择器报 `Field 'trust' is required ... missing at path: $.items[0]` 并整体打不开。
> 修复方式是给可选字段补默认值，使**单个字段缺失不再拖垮整个列表**。
> 改动细节与测试结果见 [开发日志](./docs/project/CHANGELOG-DEV.md)。

## 界面预览

<p align="center">
  <img src="./assets/screenshots/app-navigation.jpg" width="47%" alt="App 导航侧栏">
  <img src="./assets/screenshots/chat-demo.jpg" width="47%" alt="聊天与实时回复">
</p>
<p align="center">
  <img src="./assets/screenshots/file-browser.jpg" width="47%" alt="授权根目录文件浏览">
  <img src="./assets/screenshots/file-editor.jpg" width="47%" alt="带行号和缩放的文本编辑器">
</p>
<p align="center">
  <img src="./assets/screenshots/workspace-create.jpg" width="47%" alt="创建工作区并开始会话">
  <img src="./assets/screenshots/model-providers.jpg" width="47%" alt="模型供应商设置">
</p>

## 下载与安装

从 [GitHub Releases](https://github.com/Hakunm/dsh-android-app/releases/latest) 下载：

```text
DeepSeek-Harness-v1.0.0.apk
```

安装要求：

- Android 8.0（API 26）或更高版本
- 已运行的 DeepSeek Harness WebUI
- DSH WebUI 已安装 `dsh-workspace` v1.0.0 或兼容版本
- 插件中至少添加了一个授权根，并已启用远程访问
- 手机可以访问插件配置的 IP、域名和端口

正式 APK 使用项目独立的发布证书签名，不使用 Android 测试签名。后续版本需要使用同一证书，才能直接覆盖更新。

## 连接你的 DSH

### 服务器端

1. 在 DSH WebUI 打开 `dsh-workspace` 的“工作区设置”。
2. 添加至少一个授权根目录。
3. 在“远程访问”页填写绑定 IP 和端口，并保存监听设置。
4. 点击“启用并创建配对”，取得十分钟内有效的一次性配对码。

### 手机端

1. 输入服务器地址，例如 `http://192.168.1.20:3090` 或 `https://dsh.example.com`。
2. 输入配对码和设备名称。
3. 点击“配对并连接”。

设备令牌由 Android Keystore 支持的加密存储保存。App 重启后会恢复连接；在 App 中断开会清除本机令牌，在 WebUI 撤销设备会让令牌立即失效。

## 聊天不只是收发消息

- 查看设备有权访问的 DSH 会话和实时运行状态。
- 以流式增量显示助手正文和思考，结束后与服务器历史校准。
- 正确排版 Markdown 标题、列表、引用、代码块和表格。
- 发送普通消息，在运行中追加引导，或取消当前任务。
- 查看 DSH TODO 模块、完成进度和正在处理的事项。
- 输入 `/` 浏览并执行服务器提供的 host 斜杠命令。
- 在输入区切换“只读”“工作区写入”“完整访问”。完整访问会再次提示风险。
- 查看待审批工具调用的脱敏信息，选择“允许一次”或“拒绝”。
- 切换模型和思考强度。空白会话还可以在首条消息前选择 Agent。

## 工作区与会话

新建会话时，可以选择 DSH WebUI 已登记的工作区，也可以从设备获准访问的根目录中挑选一个现有目录，将它登记为新的 DSH 工作区。

工作区管理支持：

- 查看设备可访问的 DSH 工作区。
- 重命名工作区。
- 移除工作区登记。

移除登记不会删除服务器目录、文件或会话日志。

每个会话的菜单支持：

- **重命名**：修改 DSH WebUI 中显示的标题。
- **分叉**：继承已有聊天历史、工作目录和模型配置，进入新的子会话。
- **归档**：从默认列表隐藏会话，同时保留服务器日志。

App 只显示工作目录位于设备授权根内的工作区和会话，接口不会向手机暴露服务器绝对路径。

## 文件管理与编辑

- 懒加载浏览授权根和子目录。
- 新建文件或文件夹，上传、下载和替换文件。
- 重命名、移动，以及将内容移入插件回收站。
- 查看回收站并恢复项目，发生路径冲突时不会覆盖现有文件。
- 使用带同步行号的 UTF-8 文本编辑器。
- 在 Markdown 源码与渲染预览之间切换。
- 使用按钮或双指手势将源码和预览缩放到 `75%-250%`。
- 使用 ETag 检测外部修改，拒绝静默覆盖较新的文件。
- 对二进制文件只提供元数据、下载和替换。

App 不能添加服务器授权根，也不能永久清空插件回收站。这些高权限操作只在服务器本机 DSH WebUI 中提供。

## 模型供应商

拥有 `settings.read` 权限时，App 可以查看 DSH 的有效供应商配置、模型列表以及凭据是否已配置。服务器不会回传 API 密钥正文。

拥有 `settings.write` 权限时，还可以：

- 编辑供应商名称、Base URL、协议、模型和思考设置。
- 写入或清除供应商凭据。
- 从兼容服务发现模型。
- 创建 DSH 支持的完全自定义供应商和模型路由。

## 权限如何生效

App 会根据服务器授予的 scope 自动显示或禁用功能：

| Scope | App 中允许的操作 |
| --- | --- |
| `chat.read` | 查看工作区、会话、消息、TODO、命令和审批 |
| `chat.write` | 管理工作区与会话、发消息、切换配置、执行命令和处理审批 |
| `files.read` | 浏览、预览、下载和查看回收站 |
| `files.write` | 新建、编辑、上传、移动和重命名 |
| `files.delete` | 将文件或目录移入插件回收站 |
| `settings.read` | 查看供应商和模型配置 |
| `settings.write` | 修改或创建供应商，写入凭据 |

每台设备还需要单独的根目录授权。服务器管理员可以在 `dsh-workspace` 的“设备”页随时修改权限或撤销设备。

## HTTP 安全提示

App 支持 `http://` 和 `https://`，不会强制 HTTPS。HTTP 适合可信局域网、VPN 或临时测试，但会明文传输设备令牌、聊天内容和文件内容。

跨公网或不可信网络使用时，建议在插件前配置 Caddy/Nginx HTTPS，或通过 Tailscale/WireGuard、其他 VPN 或可信隧道连接。不要把配对码、设备令牌或 APK 签名材料分享给其他人。

## 隐私

- App 不包含广告或第三方分析 SDK。
- 设备令牌保存在 Android Keystore 支持的加密存储中。
- API 密钥只会写入 DSH 服务器凭据库，不会由 App 读取回显。
- 聊天和文件数据只发送到你配置的 DSH 地址。

## 常见问题

**新建会话时没有可选工作区**

切换到“新建工作区”，从授权根中选择目录；也可以先在 DSH WebUI 中登记工作区。

**某些按钮不可用**

当前设备缺少对应 scope。请在插件的“设备”页调整权限。

**无法连接服务器**

确认远程访问已经启用，服务器防火墙和云安全组允许该端口，手机填写的地址也不是服务器自己的 `127.0.0.1`。

**会话长时间没有继续**

查看聊天页是否出现审批面板。需要授权的操作不会在后台自动批准。

**保存文件时提示冲突**

服务器文件已经被其他程序修改。重新载入并合并内容后再保存。

## 从源码构建

需要 JDK 17 和 Android SDK 36：

```sh
./gradlew testDebugUnitTest lintDebug assembleRelease
```

Release 构建必须提供独立签名配置：

```properties
storeFile=/absolute/path/to/release-signing.p12
storePassword=...
keyAlias=...
keyPassword=...
```

通过 `-PdshSigningProperties=/path/to/signing.properties` 指定配置。签名文件和密码不应提交到仓库。

## 项目信息

- 当前版本：`v1.0.0`
- 包名：`io.github.hakunm.deepseekharness`
- 作者：[Github@Hakunm](https://github.com/Hakunm)
- 许可证：[GNU Affero General Public License v3.0](./LICENSE)
- 服务端插件：[dsh-workspace](https://github.com/Hakunm/dsh-workspace)
