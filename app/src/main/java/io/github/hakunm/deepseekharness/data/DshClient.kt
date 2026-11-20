package io.github.hakunm.deepseekharness.data

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.BufferedSink
import okio.source

class DshClient(
    endpoint: String,
    private val token: String? = null,
    private val http: OkHttpClient = defaultHttpClient,
) {
    val endpoint: String = normalizeEndpoint(endpoint)
    private val base: HttpUrl = (this.endpoint.trimEnd('/') + "/").toHttpUrl()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun health(): HealthView = get("healthz", HealthView.serializer(), authenticated = false)

    fun pair(code: String, deviceName: String): PairingResult = post(
        "pairings/exchange",
        PairingRequest(code.trim(), deviceName.trim()),
        PairingRequest.serializer(),
        PairingResult.serializer(),
        authenticated = false,
    )

    fun currentDevice(): DeviceView = get("devices/self", DeviceView.serializer())

    fun roots(): List<RootView> = get("roots", RootList.serializer()).items

    fun entries(rootId: String, path: String = "", cursor: String? = null): DirectoryPage {
        val url = pathUrl("roots", rootId, "entries").newBuilder()
            .addQueryParameter("path", path)
            .apply { if (cursor != null) addQueryParameter("cursor", cursor) }
            .build()
        return executeJson(Request.Builder().url(url).get(), DirectoryPage.serializer())
    }

    fun searchFiles(
        rootId: String,
        query: String,
        path: String = "",
        limit: Int? = null,
        includeHidden: Boolean = false,
    ): FileSearchResult {
        val url = pathUrl("roots", rootId, "search").newBuilder()
            .addQueryParameter("q", query.trim())
            .apply {
                if (path.isNotBlank()) addQueryParameter("path", path.trim('/'))
                if (limit != null) addQueryParameter("limit", limit.toString())
                if (includeHidden) addQueryParameter("includeHidden", "true")
            }
            .build()
        return executeJson(Request.Builder().url(url).get(), FileSearchResult.serializer())
    }

    fun readFile(rootId: String, path: String): FileContent {
        val url = pathUrl("roots", rootId, "content").newBuilder().addQueryParameter("path", path).build()
        execute(Request.Builder().url(url).get()).use { response ->
            ensureSuccess(response)
            return FileContent(
                response.body?.bytes() ?: ByteArray(0),
                response.header("ETag").orEmpty(),
                response.header("Content-Type"),
            )
        }
    }

    fun downloadFile(rootId: String, path: String, output: OutputStream) {
        val url = pathUrl("roots", rootId, "content").newBuilder().addQueryParameter("path", path).build()
        execute(Request.Builder().url(url).get()).use { response ->
            ensureSuccess(response)
            response.body?.byteStream()?.use { input -> output.use { destination -> input.copyTo(destination) } }
        }
    }

    fun writeFile(rootId: String, path: String, bytes: ByteArray, etag: String? = null): String {
        return writeFile(rootId, path, bytes.toRequestBody(OCTET_STREAM), etag)
    }

    fun writeFile(
        rootId: String,
        path: String,
        length: Long,
        openInput: () -> InputStream,
        etag: String? = null,
    ): String = writeFile(rootId, path, object : okhttp3.RequestBody() {
        override fun contentType() = OCTET_STREAM
        override fun contentLength() = length
        override fun writeTo(sink: BufferedSink) {
            openInput().source().use(sink::writeAll)
        }
    }, etag)

    private fun writeFile(rootId: String, path: String, body: okhttp3.RequestBody, etag: String?): String {
        val url = pathUrl("roots", rootId, "content").newBuilder().addQueryParameter("path", path).build()
        val request = Request.Builder().url(url).put(body).apply {
            if (etag == null) header("If-None-Match", "*") else header("If-Match", etag)
        }
        execute(request).use { response ->
            ensureSuccess(response)
            return response.header("ETag").orEmpty()
        }
    }

    fun createEntry(rootId: String, path: String, directory: Boolean) {
        postUnit(
            pathUrl("roots", rootId, "entries"),
            CreateEntryRequest(path, if (directory) "directory" else "file"),
            CreateEntryRequest.serializer(),
        )
    }

    fun moveEntry(rootId: String, path: String, destinationPath: String) {
        patchUnit(pathUrl("roots", rootId, "entries"), MoveEntryRequest(path, destinationPath), MoveEntryRequest.serializer())
    }

    fun trashEntry(rootId: String, path: String) {
        val url = pathUrl("roots", rootId, "entries").newBuilder().addQueryParameter("path", path).build()
        execute(Request.Builder().url(url).delete()).use(::ensureSuccess)
    }

    fun trash(): List<TrashEntry> = get("trash", TrashList.serializer()).items

    fun restoreTrash(id: String) {
        execute(Request.Builder().url(pathUrl("trash", id, "restore")).post(EMPTY_BODY)).use(::ensureSuccess)
    }

    fun sessions(includeArchived: Boolean = false): List<ChatSession> {
        val url = pathUrl("chat", "sessions").newBuilder().apply {
            if (includeArchived) addQueryParameter("includeArchived", "true")
        }.build()
        return get(url, SessionList.serializer()).items
    }

    fun searchSessions(query: String, includeArchived: Boolean = false): SessionSearchResult {
        val url = pathUrl("chat", "sessions", "search").newBuilder()
            .addQueryParameter("q", query.trim())
            .apply { if (includeArchived) addQueryParameter("includeArchived", "true") }
            .build()
        return get(url, SessionSearchResult.serializer())
    }

    fun permissionCatalog(): PermissionCatalog = get("chat/permissions", PermissionCatalog.serializer())

    fun chatWorkspaces(): List<ChatWorkspace> = get("chat/workspaces", ChatWorkspaceList.serializer()).items

    fun createChatWorkspace(rootId: String, path: String): ChatWorkspace = post(
        "chat/workspaces",
        CreateWorkspaceRequest(rootId, path),
        CreateWorkspaceRequest.serializer(),
        CreatedWorkspaceEnvelope.serializer(),
    ).workspace

    fun renameChatWorkspace(workspaceId: String, title: String): ChatWorkspace = patch(
        pathUrl("chat", "workspaces", workspaceId),
        RenameRequest(title),
        RenameRequest.serializer(),
        CreatedWorkspaceEnvelope.serializer(),
    ).workspace

    fun deleteChatWorkspace(workspaceId: String) {
        execute(Request.Builder().url(pathUrl("chat", "workspaces", workspaceId)).delete()).use(::ensureSuccess)
    }

    fun agentPresets(): List<AgentPreset> = get("chat/agent-presets", AgentPresetList.serializer()).items

    fun selectAgentPreset(sessionId: String, agentPreset: String): String = put(
        pathUrl("chat", "sessions", sessionId, "agent-preset"),
        AgentPresetSelection(agentPreset),
        AgentPresetSelection.serializer(),
        SelectedAgentPresetEnvelope.serializer(),
    ).agentPreset

    fun sessionModels(sessionId: String): SessionModels =
        get(pathUrl("chat", "sessions", sessionId, "models"), SessionModels.serializer())

    fun selectModel(sessionId: String, selection: ModelSelection): ModelSelection = put(
        pathUrl("chat", "sessions", sessionId, "model"),
        selection,
        ModelSelection.serializer(),
        SelectedModelEnvelope.serializer(),
    ).selected

    fun providerSettings(): ProviderSettings = get("settings/providers", ProviderSettings.serializer())

    fun updateProvider(providerId: String, patch: ProviderPatch): ProviderSettings = patch(
        pathUrl("settings", "providers", providerId),
        patch,
        ProviderPatch.serializer(),
        ProviderSettings.serializer(),
    )

    fun createCustomProvider(provider: CustomProviderCreate): ProviderSettings = post(
        "settings/providers",
        provider,
        CustomProviderCreate.serializer(),
        ProviderSettings.serializer(),
    )

    fun removeProvider(providerId: String, expectedRevision: Int? = null) {
        val url = pathUrl("settings", "providers", providerId).newBuilder().apply {
            expectedRevision?.let { addQueryParameter("expectedRevision", it.toString()) }
        }.build()
        execute(Request.Builder().url(url).delete()).use(::ensureSuccess)
    }

    fun discoverModels(providerId: String, baseURL: String?, api: String?, apiKey: String?): List<ProviderModel> = post(
        pathUrl("settings", "providers", providerId, "discover"),
        DiscoverModelsRequest(baseURL, api, apiKey),
        DiscoverModelsRequest.serializer(),
        DiscoveredModelsEnvelope.serializer(),
    ).models

    fun createSession(workspaceId: String, agentPreset: String? = null): CreatedSession = post(
        "chat/sessions",
        CreateSessionRequest(workspaceId, agentPreset, UUID.randomUUID().toString()),
        CreateSessionRequest.serializer(),
        CreatedSessionEnvelope.serializer(),
    ).session

    fun renameSession(sessionId: String, title: String): RenamedSession = patch(
        pathUrl("chat", "sessions", sessionId),
        RenameRequest(title),
        RenameRequest.serializer(),
        RenamedSession.serializer(),
    )

    fun forkSession(sessionId: String, atSeq: Int? = null): String = post(
        pathUrl("chat", "sessions", sessionId, "fork"),
        ForkSessionRequest(atSeq),
        ForkSessionRequest.serializer(),
        ForkedSessionEnvelope.serializer(),
    ).sessionId

    fun archiveSession(sessionId: String) {
        execute(Request.Builder().url(pathUrl("chat", "sessions", sessionId, "archive")).post(EMPTY_BODY)).use(::ensureSuccess)
    }

    fun unarchiveSession(sessionId: String) {
        execute(Request.Builder().url(pathUrl("chat", "sessions", sessionId, "unarchive")).post(EMPTY_BODY)).use(::ensureSuccess)
    }

    fun pinSession(sessionId: String) {
        execute(Request.Builder().url(pathUrl("chat", "sessions", sessionId, "pin")).post(EMPTY_BODY)).use(::ensureSuccess)
    }

    fun unpinSession(sessionId: String) {
        execute(Request.Builder().url(pathUrl("chat", "sessions", sessionId, "pin")).delete()).use(::ensureSuccess)
    }

    fun sessionState(sessionId: String): SessionState =
        get(pathUrl("chat", "sessions", sessionId, "state"), SessionState.serializer())

    fun updateQueue(sessionId: String, itemId: String, kind: String, text: String? = null) {
        postUnit(
            pathUrl("chat", "sessions", sessionId, "queue", itemId),
            QueueMutationRequest(kind, text),
            QueueMutationRequest.serializer(),
        )
    }

    fun history(sessionId: String, beforeSeq: Int? = null, maxMessages: Int? = null): ChatHistory {
        val url = pathUrl("chat", "sessions", sessionId, "messages").newBuilder().apply {
            beforeSeq?.let { addQueryParameter("beforeSeq", it.toString()) }
            maxMessages?.let { addQueryParameter("maxMessages", it.toString()) }
        }.build()
        return get(url, ChatHistory.serializer())
    }

    fun pendingApprovals(sessionId: String): List<PendingApproval> =
        get(pathUrl("chat", "sessions", sessionId, "approvals"), PendingApprovalList.serializer()).items

    fun decideApproval(sessionId: String, approvalId: String, allowOnce: Boolean) {
        postUnit(
            pathUrl("chat", "sessions", sessionId, "approvals", approvalId, "decision"),
            ApprovalDecision(if (allowOnce) "allowed-once" else "rejected"),
            ApprovalDecision.serializer(),
        )
    }

    fun pendingQuestions(sessionId: String): List<PendingQuestion> =
        get(pathUrl("chat", "sessions", sessionId, "questions"), PendingQuestionList.serializer()).items

    fun answerQuestion(sessionId: String, questionId: String, answers: List<QuestionAnswerItem>) {
        postUnit(
            pathUrl("chat", "sessions", sessionId, "questions", questionId, "answer"),
            AnswerQuestionRequest(answers),
            AnswerQuestionRequest.serializer(),
        )
    }

    fun sessionCommands(sessionId: String): List<CommandDescriptor> =
        get(pathUrl("chat", "sessions", sessionId, "commands"), CommandDescriptorList.serializer()).items

    fun executeCommand(sessionId: String, line: String): CommandExecution = post(
        pathUrl("chat", "sessions", sessionId, "commands"),
        CommandRequest(line),
        CommandRequest.serializer(),
        CommandExecutionEnvelope.serializer(),
    ).execution

    fun sendMessage(
        sessionId: String,
        text: String,
        steer: Boolean,
        attachments: List<ImageAttachment> = emptyList(),
    ) {
        postJsonElement(
            pathUrl("chat", "sessions", sessionId, "messages"),
            SendMessageRequest(
                text = text,
                mode = if (steer) "steer" else "queue",
                clientTimeZone = TimeZone.getDefault().id,
                clientRequestId = UUID.randomUUID().toString(),
                attachments = attachments.takeIf { it.isNotEmpty() },
            ),
            SendMessageRequest.serializer(),
        )
    }

    fun cancel(sessionId: String) {
        execute(Request.Builder().url(pathUrl("chat", "runs", sessionId, "cancel")).post(EMPTY_BODY)).use(::ensureSuccess)
    }

    fun events(listener: (WorkspaceEvent) -> Unit, connection: (Boolean) -> Unit): Closeable {
        val request = authenticated(Request.Builder().url(eventsUrl())).build()
        return ReconnectingEvents(http, request, json, listener, connection).apply { start() }
    }

    internal fun eventsUrl(): HttpUrl = base.newBuilder().addPathSegment("events").build()

    private fun <T> get(relative: String, serializer: KSerializer<T>, authenticated: Boolean = true): T =
        get(resolve(relative), serializer, authenticated)

    private fun <T> get(url: HttpUrl, serializer: KSerializer<T>, authenticated: Boolean = true): T =
        executeJson(Request.Builder().url(url).get(), serializer, authenticated)

    private fun <B, T> post(
        relative: String,
        body: B,
        bodySerializer: KSerializer<B>,
        responseSerializer: KSerializer<T>,
        authenticated: Boolean = true,
    ): T {
        val requestBody = wireJson.encodeToString(bodySerializer, body).toRequestBody(JSON_MEDIA)
        return executeJson(Request.Builder().url(resolve(relative)).post(requestBody), responseSerializer, authenticated)
    }

    private fun <B, T> post(
        url: HttpUrl,
        body: B,
        bodySerializer: KSerializer<B>,
        responseSerializer: KSerializer<T>,
    ): T {
        val requestBody = wireJson.encodeToString(bodySerializer, body).toRequestBody(JSON_MEDIA)
        return executeJson(Request.Builder().url(url).post(requestBody), responseSerializer)
    }

    private fun <B, T> put(
        url: HttpUrl,
        body: B,
        bodySerializer: KSerializer<B>,
        responseSerializer: KSerializer<T>,
    ): T {
        val requestBody = wireJson.encodeToString(bodySerializer, body).toRequestBody(JSON_MEDIA)
        return executeJson(Request.Builder().url(url).put(requestBody), responseSerializer)
    }

    private fun <B, T> patch(
        url: HttpUrl,
        body: B,
        bodySerializer: KSerializer<B>,
        responseSerializer: KSerializer<T>,
    ): T {
        val requestBody = wireJson.encodeToString(bodySerializer, body).toRequestBody(JSON_MEDIA)
        return executeJson(Request.Builder().url(url).patch(requestBody), responseSerializer)
    }

    private fun <B> postJsonElement(url: HttpUrl, body: B, serializer: KSerializer<B>): JsonElement {
        val requestBody = wireJson.encodeToString(serializer, body).toRequestBody(JSON_MEDIA)
        return executeJson(Request.Builder().url(url).post(requestBody), JsonElement.serializer())
    }

    private fun <B> postUnit(url: HttpUrl, body: B, serializer: KSerializer<B>) {
        val requestBody = wireJson.encodeToString(serializer, body).toRequestBody(JSON_MEDIA)
        execute(Request.Builder().url(url).post(requestBody)).use(::ensureSuccess)
    }

    private fun <B> patchUnit(url: HttpUrl, body: B, serializer: KSerializer<B>) {
        val requestBody = wireJson.encodeToString(serializer, body).toRequestBody(JSON_MEDIA)
        execute(Request.Builder().url(url).patch(requestBody)).use(::ensureSuccess)
    }

    private fun <T> executeJson(
        builder: Request.Builder,
        serializer: KSerializer<T>,
        authenticated: Boolean = true,
    ): T {
        execute(builder, authenticated).use { response ->
            ensureSuccess(response)
            return json.decodeFromString(serializer, response.body?.string() ?: "{}")
        }
    }

    private fun execute(builder: Request.Builder, authenticated: Boolean = true): Response =
        http.newCall(if (authenticated) authenticated(builder).build() else builder.build()).execute()

    private fun authenticated(builder: Request.Builder): Request.Builder =
        builder.header("Accept", "application/json").header("Authorization", "Bearer ${requireNotNull(token)}")

    private fun ensureSuccess(response: Response) {
        if (response.isSuccessful) return
        val raw = response.body?.string().orEmpty()
        val error = runCatching { json.decodeFromString(ErrorEnvelope.serializer(), raw).error }.getOrNull()
        throw DshApiException(response.code, error?.code ?: "HTTP_ERROR", error?.message ?: "HTTP ${response.code}")
    }

    private fun resolve(relative: String): HttpUrl = base.resolve(relative)
        ?: throw IllegalArgumentException("Invalid API path: $relative")

    private fun pathUrl(vararg segments: String): HttpUrl = base.newBuilder().apply {
        segments.forEach(::addPathSegment)
    }.build()

    companion object {
        @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
        private val wireJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }
        private val JSON_MEDIA = "application/json".toMediaType()
        private val OCTET_STREAM = "application/octet-stream".toMediaType()
        private val EMPTY_BODY = ByteArray(0).toRequestBody(OCTET_STREAM)
        private val defaultHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .build()

        fun normalizeEndpoint(value: String): String {
            val input = value.trim().trimEnd('/')
            require(input.startsWith("http://") || input.startsWith("https://")) {
                "Endpoint must start with http:// or https://"
            }
            val url = input.toHttpUrl()
            return if (url.encodedPath.trimEnd('/').endsWith("/api/v1")) input else "$input/api/v1"
        }
    }
}

private class ReconnectingEvents(
    private val http: OkHttpClient,
    private val request: Request,
    private val json: Json,
    private val listener: (WorkspaceEvent) -> Unit,
    private val connection: (Boolean) -> Unit,
) : Closeable {
    private val executor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "dsh-events").apply { isDaemon = true }
    }
    @Volatile private var closed = false
    @Volatile private var socket: WebSocket? = null
    private var retries = 0

    fun start() = connect()

    private fun connect() {
        if (closed) return
        val terminal = AtomicBoolean(false)
        socket = http.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                retries = 0
                connection(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching { json.decodeFromString(WorkspaceEvent.serializer(), text) }.onSuccess(listener)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = reconnectOnce(terminal)
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = reconnectOnce(terminal)
        })
    }

    private fun reconnectOnce(terminal: AtomicBoolean) {
        if (!terminal.compareAndSet(false, true)) return
        connection(false)
        if (closed) return
        val delay = (1L shl retries.coerceAtMost(5)).coerceAtMost(30L)
        retries += 1
        executor.schedule(::connect, delay, TimeUnit.SECONDS)
    }

    override fun close() {
        closed = true
        connection(false)
        socket?.close(1000, "App closed")
        socket = null
        executor.shutdownNow()
    }
}

@Serializable private data class RootList(val items: List<RootView>)
@Serializable private data class TrashList(val items: List<TrashEntry>)
@Serializable private data class SessionList(val items: List<ChatSession>)
@Serializable private data class PendingApprovalList(val items: List<PendingApproval>)
@Serializable private data class ApprovalDecision(val outcome: String)
@Serializable private data class PendingQuestionList(val items: List<PendingQuestion>)
@Serializable private data class AnswerQuestionRequest(val answers: List<QuestionAnswerItem>)
@Serializable private data class QueueMutationRequest(val kind: String, val text: String? = null)
@Serializable private data class CommandDescriptorList(val items: List<CommandDescriptor>)
@Serializable private data class CommandRequest(val line: String)
@Serializable private data class CommandExecutionEnvelope(val execution: CommandExecution)
@Serializable private data class ChatWorkspaceList(val items: List<ChatWorkspace>)
@Serializable private data class CreatedWorkspaceEnvelope(val workspace: ChatWorkspace)
@Serializable private data class RenameRequest(val title: String)
@Serializable private data class ForkSessionRequest(val atSeq: Int? = null)
@Serializable private data class ForkedSessionEnvelope(val sessionId: String)
@Serializable private data class AgentPresetList(val items: List<AgentPreset>)
@Serializable private data class AgentPresetSelection(val agentPreset: String)
@Serializable private data class SelectedAgentPresetEnvelope(val agentPreset: String)
@Serializable private data class SelectedModelEnvelope(val selected: ModelSelection)
@Serializable private data class DiscoveredModelsEnvelope(val models: List<ProviderModel>)
@Serializable private data class DiscoverModelsRequest(
    val baseURL: String? = null,
    val api: String? = null,
    val apiKey: String? = null,
)
@Serializable private data class PairingRequest(val code: String, val deviceName: String)
@Serializable private data class CreateEntryRequest(val path: String, val kind: String)
@Serializable private data class MoveEntryRequest(val path: String, val destinationPath: String)
@Serializable private data class CreateWorkspaceRequest(val rootId: String, val path: String)
@Serializable private data class CreateSessionRequest(
    val workspaceId: String,
    val agentPreset: String? = null,
    val clientRequestId: String,
)
@Serializable private data class CreatedSessionEnvelope(val session: CreatedSession)
@Serializable private data class SendMessageRequest(
    val text: String,
    val mode: String,
    val clientTimeZone: String,
    val clientRequestId: String,
    val attachments: List<ImageAttachment>? = null,
)
@Serializable private data class ErrorEnvelope(val error: ApiErrorBody)
@Serializable private data class ApiErrorBody(val code: String, val message: String, val requestId: String? = null)
