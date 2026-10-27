package io.github.hakunm.deepseekharness

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.hakunm.deepseekharness.data.ChatHistory
import io.github.hakunm.deepseekharness.data.ChatSession
import io.github.hakunm.deepseekharness.data.ChatWorkspace
import io.github.hakunm.deepseekharness.data.CommandDescriptor
import io.github.hakunm.deepseekharness.data.CustomProviderCreate
import io.github.hakunm.deepseekharness.data.DeviceView
import io.github.hakunm.deepseekharness.data.DirectoryPage
import io.github.hakunm.deepseekharness.data.DshApiException
import io.github.hakunm.deepseekharness.data.DshClient
import io.github.hakunm.deepseekharness.data.FileEntry
import io.github.hakunm.deepseekharness.data.LiveChatState
import io.github.hakunm.deepseekharness.data.RootView
import io.github.hakunm.deepseekharness.data.AgentPreset
import io.github.hakunm.deepseekharness.data.ModelSelection
import io.github.hakunm.deepseekharness.data.PendingApproval
import io.github.hakunm.deepseekharness.data.ProviderModel
import io.github.hakunm.deepseekharness.data.ProviderPatch
import io.github.hakunm.deepseekharness.data.ProviderSettings
import io.github.hakunm.deepseekharness.data.SessionModels
import io.github.hakunm.deepseekharness.data.SecureConnectionStore
import io.github.hakunm.deepseekharness.data.TrashEntry
import io.github.hakunm.deepseekharness.data.WorkspaceEvent
import io.github.hakunm.deepseekharness.data.assistantStreamStart
import io.github.hakunm.deepseekharness.data.chatDelta
import io.github.hakunm.deepseekharness.data.sessionTitle
import io.github.hakunm.deepseekharness.data.streamTurnKey
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

data class OpenDocument(
    val entry: FileEntry,
    val text: String? = null,
    val etag: String = "",
    val hasBom: Boolean = false,
    val lineEnding: String = "\n",
    val contentType: String? = null,
)

sealed interface ApprovalUiState {
    data object None : ApprovalUiState
    data object Loading : ApprovalUiState
    data class Pending(val items: List<PendingApproval>) : ApprovalUiState
    data class Deciding(val item: PendingApproval, val items: List<PendingApproval>) : ApprovalUiState
}

data class HarnessState(
    val restoring: Boolean = true,
    val busy: Boolean = false,
    val endpointDraft: String = "http://192.168.1.20:3090",
    val healthOk: Boolean = false,
    val connected: Boolean = false,
    val endpoint: String = "",
    val device: DeviceView? = null,
    val roots: List<RootView> = emptyList(),
    val sessions: List<ChatSession> = emptyList(),
    val chatWorkspaces: List<ChatWorkspace> = emptyList(),
    val agentPresets: List<AgentPreset> = emptyList(),
    val workspacePickerRootId: String? = null,
    val workspacePickerPath: String = "",
    val workspacePickerDirectory: DirectoryPage? = null,
    val sessionModels: SessionModels? = null,
    val commands: List<CommandDescriptor> = emptyList(),
    val providerSettings: ProviderSettings? = null,
    val discoveredModels: List<ProviderModel> = emptyList(),
    val selectedSessionId: String? = null,
    val history: ChatHistory? = null,
    val liveChat: LiveChatState? = null,
    val approvalState: ApprovalUiState = ApprovalUiState.None,
    val eventsConnected: Boolean = false,
    val selectedRootId: String? = null,
    val currentPath: String = "",
    val directory: DirectoryPage? = null,
    val document: OpenDocument? = null,
    val trash: List<TrashEntry> = emptyList(),
    val error: String? = null,
)

class HarnessViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SecureConnectionStore(application)
    private val mutableState = MutableStateFlow(HarnessState())
    val state: StateFlow<HarnessState> = mutableState.asStateFlow()
    private var client: DshClient? = null
    private var eventSubscription: Closeable? = null
    private var chatRefreshJob: Job? = null
    @Volatile private var chatRefreshPending = false
    @Volatile private var chatSessionRefreshPending = false
    @Volatile private var liveFinalizeTarget: LiveKey? = null
    private val assistantTurns = mutableMapOf<String, Pair<Int, Int>>()

    init {
        restoreConnection()
    }

    fun setEndpointDraft(value: String) = update { copy(endpointDraft = value, healthOk = false, error = null) }

    fun testConnection(endpoint: String) = launchOperation {
        val normalized = DshClient.normalizeEndpoint(endpoint)
        DshClient(normalized).health()
        update { copy(endpointDraft = normalized.removeSuffix("/api/v1"), healthOk = true) }
    }

    fun pair(endpoint: String, code: String, deviceName: String) = launchOperation {
        require(code.isNotBlank()) { "Pairing code is required." }
        require(deviceName.isNotBlank()) { "Device name is required." }
        val unauthenticated = DshClient(endpoint)
        unauthenticated.health()
        val pairing = unauthenticated.pair(code, deviceName)
        val paired = DshClient(unauthenticated.endpoint, pairing.token)
        store.save(unauthenticated.endpoint, pairing.token)
        client = paired
        val snapshot = loadSnapshot(paired, pairing.device)
        update { snapshot.copy(restoring = false, busy = true, connected = true, healthOk = true) }
        connectEvents(paired)
    }

    fun disconnect() {
        eventSubscription?.close()
        eventSubscription = null
        client = null
        store.clear()
        mutableState.value = HarnessState(restoring = false)
    }

    fun clearError() = update { copy(error = null) }

    fun refreshDashboard() = withClient { api ->
        val device = api.currentDevice()
        val snapshot = loadSnapshot(api, device)
        update {
            snapshot.copy(
                restoring = false,
                busy = true,
                selectedSessionId = selectedSessionId?.takeIf { id -> snapshot.sessions.any { it.id == id } },
                selectedRootId = selectedRootId?.takeIf { id -> snapshot.roots.any { it.id == id } }
                    ?: snapshot.roots.firstOrNull()?.id,
            )
        }
    }

    fun selectSession(id: String?) {
        update {
            copy(
                selectedSessionId = id,
                history = null,
                liveChat = null,
                sessionModels = null,
                commands = emptyList(),
                approvalState = if (id == null) ApprovalUiState.None else ApprovalUiState.Loading,
            )
        }
        if (id != null) {
            refreshHistory()
            refreshSessionModels()
            refreshPendingApprovals()
            refreshCommands()
        }
    }

    fun refreshHistory() = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        val history = api.history(id)
        update { withSessionHistory(id, history) }
    }

    fun loadOlderHistory() = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        val current = mutableState.value.history ?: return@withClient
        if (!current.hasMore) return@withClient
        val beforeSeq = current.events.minOfOrNull { it.event.seq } ?: return@withClient
        val older = api.history(id, beforeSeq)
        update {
            copy(history = current.copy(
                events = (older.events + current.events).distinctBy { it.event.seq },
                hasMore = older.hasMore,
                projections = current.projections ?: older.projections,
            ))
        }
    }

    fun createSession(workspaceId: String, preset: String?) = withClient { api ->
        val created = api.createSession(workspaceId, preset?.takeIf(String::isNotBlank))
        val sessions = api.sessions()
        val history = api.history(created.id)
        val models = api.sessionModels(created.id)
        val commands = api.sessionCommands(created.id)
        update {
            copy(
                sessions = sessions.preservingTitles(this.sessions),
                selectedSessionId = created.id,
                sessionModels = models,
                commands = commands,
                approvalState = ApprovalUiState.None,
            ).withSessionHistory(created.id, history)
        }
    }

    fun prepareWorkspacePicker() {
        val rootId = mutableState.value.workspacePickerRootId
            ?.takeIf { id -> mutableState.value.roots.any { it.id == id } }
            ?: mutableState.value.selectedRootId
            ?: mutableState.value.roots.firstOrNull()?.id
        update { copy(workspacePickerRootId = rootId, workspacePickerPath = "", workspacePickerDirectory = null) }
        if (rootId != null) loadWorkspacePickerDirectory("")
    }

    fun selectWorkspacePickerRoot(rootId: String) {
        update { copy(workspacePickerRootId = rootId, workspacePickerPath = "", workspacePickerDirectory = null) }
        loadWorkspacePickerDirectory("")
    }

    fun loadWorkspacePickerDirectory(path: String) = withClient { api ->
        val rootId = mutableState.value.workspacePickerRootId ?: return@withClient
        val normalized = path.trim('/')
        val page = api.entries(rootId, normalized)
        update { copy(workspacePickerPath = normalized, workspacePickerDirectory = page) }
    }

    fun createWorkspaceAndSession(rootId: String, path: String, preset: String?) = withClient { api ->
        val workspace = api.createChatWorkspace(rootId, path.trim('/'))
        val created = api.createSession(workspace.id, preset?.takeIf(String::isNotBlank))
        val sessions = api.sessions()
        val workspaces = api.chatWorkspaces()
        update {
            copy(
                chatWorkspaces = workspaces,
                sessions = sessions.preservingTitles(this.sessions),
                selectedSessionId = created.id,
                sessionModels = api.sessionModels(created.id),
                commands = api.sessionCommands(created.id),
                approvalState = ApprovalUiState.None,
            ).withSessionHistory(created.id, api.history(created.id))
        }
    }

    fun renameWorkspace(workspaceId: String, title: String) = withClient { api ->
        api.renameChatWorkspace(workspaceId, title)
        update { copy(chatWorkspaces = api.chatWorkspaces(), sessions = api.sessions().preservingTitles(sessions)) }
    }

    fun deleteWorkspace(workspaceId: String) = withClient { api ->
        api.deleteChatWorkspace(workspaceId)
        update { copy(chatWorkspaces = api.chatWorkspaces(), sessions = api.sessions().preservingTitles(sessions)) }
    }

    fun renameSession(sessionId: String, title: String) = withClient { api ->
        val renamed = api.renameSession(sessionId, title)
        val sessions = api.sessions().map { session ->
            if (session.id == sessionId) session.copy(title = renamed.title) else session
        }
        update { copy(sessions = sessions.preservingTitles(this.sessions)) }
    }

    fun forkSession(sessionId: String) = withClient { api ->
        val forkedId = api.forkSession(sessionId)
        val history = api.history(forkedId)
        update {
            copy(
                sessions = api.sessions().preservingTitles(sessions),
                selectedSessionId = forkedId,
                sessionModels = api.sessionModels(forkedId),
                commands = api.sessionCommands(forkedId),
                approvalState = ApprovalUiState.None,
                liveChat = null,
            ).withSessionHistory(forkedId, history)
        }
    }

    fun archiveSession(sessionId: String) = withClient { api ->
        api.archiveSession(sessionId)
        val sessions = api.sessions()
        update {
            val kept = sessions.preservingTitles(this.sessions)
            if (selectedSessionId == sessionId) {
                copy(
                    sessions = kept,
                    selectedSessionId = null,
                    history = null,
                    liveChat = null,
                    sessionModels = null,
                    commands = emptyList(),
                    approvalState = ApprovalUiState.None,
                )
            } else copy(sessions = kept)
        }
    }

    fun sendMessage(text: String, steer: Boolean) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        if (text.isBlank()) return@withClient
        val line = text.trimEnd()
        if (line.startsWith('/')) {
            val execution = api.executeCommand(id, line)
            if (execution.result.kind == "error") {
                throw DshApiException(409, "COMMAND_FAILED", execution.result.text ?: "Command failed.")
            }
            update { withSessionHistory(id, api.history(id)).copy(commands = api.sessionCommands(id)) }
            return@withClient
        }
        api.sendMessage(id, text, steer)
        chatSessionRefreshPending = true
        scheduleChatRefresh()
    }

    fun refreshCommands() = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        update { copy(commands = api.sessionCommands(id)) }
    }

    fun selectPermissionPreset(preset: String) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        val execution = api.executeCommand(id, "/permission $preset")
        if (execution.result.kind == "error") {
            throw DshApiException(409, "COMMAND_FAILED", execution.result.text ?: "Permission change failed.")
        }
        update { withSessionHistory(id, api.history(id)).copy(commands = api.sessionCommands(id)) }
    }

    fun refreshSessionModels() = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        update { copy(sessionModels = api.sessionModels(id)) }
    }

    fun selectModel(selection: ModelSelection) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        api.selectModel(id, selection)
        update { copy(sessionModels = api.sessionModels(id)) }
    }

    fun selectAgentPreset(agentPreset: String) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        api.selectAgentPreset(id, agentPreset)
        val sessions = api.sessions()
        val models = api.sessionModels(id)
        val commands = api.sessionCommands(id)
        update { copy(sessions = sessions.preservingTitles(this.sessions), sessionModels = models, commands = commands) }
    }

    fun refreshProviderSettings() = withClient { api ->
        if ("settings.read" !in mutableState.value.device?.scopes.orEmpty()) return@withClient
        update { copy(providerSettings = api.providerSettings()) }
    }

    fun updateProvider(providerId: String, patch: ProviderPatch) = withClient { api ->
        update { copy(providerSettings = api.updateProvider(providerId, patch), discoveredModels = emptyList()) }
    }

    fun createCustomProvider(provider: CustomProviderCreate) = withClient { api ->
        update { copy(providerSettings = api.createCustomProvider(provider), discoveredModels = emptyList()) }
    }

    fun discoverModels(providerId: String, baseURL: String?, protocol: String?, apiKey: String?) = withClient { api ->
        update { copy(discoveredModels = api.discoverModels(providerId, baseURL, protocol, apiKey)) }
    }

    fun clearDiscoveredModels() = update { copy(discoveredModels = emptyList()) }

    fun cancelRun() = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        api.cancel(id)
        chatSessionRefreshPending = true
        scheduleChatRefresh()
    }

    fun refreshPendingApprovals() {
        val api = client ?: return
        val id = mutableState.value.selectedSessionId ?: return
        viewModelScope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { api.pendingApprovals(id) } }
            if (mutableState.value.selectedSessionId != id) return@launch
            result
                .onSuccess { approvals -> update { copy(approvalState = approvals.toApprovalState()) } }
                .onFailure { error ->
                    update {
                        copy(
                            approvalState = ApprovalUiState.None,
                            error = if (error is DshApiException && error.code == "RATE_LIMITED") null else messageOf(error),
                        )
                    }
                }
        }
    }

    fun decideApproval(approval: PendingApproval, allowOnce: Boolean) {
        val api = client ?: return
        val id = mutableState.value.selectedSessionId ?: return
        if (approval.sessionId != id) return
        val items = when (val current = mutableState.value.approvalState) {
            is ApprovalUiState.Pending -> current.items
            is ApprovalUiState.Deciding -> current.items
            else -> listOf(approval)
        }
        viewModelScope.launch {
            update { copy(approvalState = ApprovalUiState.Deciding(approval, items), error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    api.decideApproval(id, approval.id, allowOnce)
                    api.pendingApprovals(id) to api.sessions()
                }
            }.onSuccess { (approvals, sessions) ->
                if (mutableState.value.selectedSessionId == id) {
                    update { copy(approvalState = approvals.toApprovalState(), sessions = sessions.preservingTitles(this.sessions)) }
                    chatSessionRefreshPending = true
                    scheduleChatRefresh()
                }
            }.onFailure { error ->
                if (mutableState.value.selectedSessionId == id) {
                    update { copy(approvalState = ApprovalUiState.Pending(items), error = messageOf(error)) }
                }
            }
        }
    }

    fun selectRoot(id: String) {
        update { copy(selectedRootId = id, currentPath = "", document = null) }
        loadDirectory("")
    }

    fun loadDirectory(path: String = mutableState.value.currentPath) = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        val normalized = path.trim('/')
        val page = api.entries(root, normalized)
        update { copy(currentPath = normalized, directory = page, document = null) }
    }

    fun loadNextDirectoryPage() = withClient { api ->
        val current = mutableState.value.directory ?: return@withClient
        val cursor = current.nextCursor ?: return@withClient
        val root = mutableState.value.selectedRootId ?: return@withClient
        val next = api.entries(root, current.path, cursor)
        update {
            copy(directory = current.copy(
                entries = (current.entries + next.entries).distinctBy { it.path },
                nextCursor = next.nextCursor,
            ))
        }
    }

    fun openEntry(entry: FileEntry) {
        if (entry.kind == "directory") {
            loadDirectory(entry.path)
            return
        }
        withClient { api ->
            val root = mutableState.value.selectedRootId ?: return@withClient
            if (entry.size > MAX_EDIT_BYTES) {
                update { copy(document = OpenDocument(entry), error = "FILE_TOO_LARGE") }
                return@withClient
            }
            val content = api.readFile(root, entry.path)
            val decoded = decodeUtf8(content.bytes)
            update {
                copy(
                    document = OpenDocument(
                        entry = entry,
                        text = decoded?.text,
                        etag = content.etag,
                        hasBom = decoded?.hasBom == true,
                        lineEnding = decoded?.lineEnding ?: "\n",
                        contentType = content.contentType,
                    ),
                    error = if (decoded == null) "BINARY_FILE" else null,
                )
            }
        }
    }

    fun selectEntryForAction(entry: FileEntry) = update {
        copy(document = OpenDocument(entry))
    }

    fun updateDocument(text: String) = update {
        copy(document = document?.copy(text = text))
    }

    fun saveDocument() = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        val document = mutableState.value.document ?: return@withClient
        val text = document.text ?: return@withClient
        val normalized = text.replace("\r\n", "\n").replace("\r", "\n")
        val encoded = when (document.lineEnding) {
            "\r\n" -> normalized.replace("\n", "\r\n")
            "\r" -> normalized.replace("\n", "\r")
            else -> normalized
        }
            .toByteArray(Charsets.UTF_8)
        val bytes = if (document.hasBom) UTF8_BOM + encoded else encoded
        val etag = api.writeFile(root, document.entry.path, bytes, document.etag)
        update { copy(document = document.copy(etag = etag)) }
        val page = api.entries(root, mutableState.value.currentPath)
        update { copy(directory = page) }
    }

    fun createEntry(name: String, directory: Boolean) = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        val path = childPath(mutableState.value.currentPath, name)
        api.createEntry(root, path, directory)
        update { copy(directory = api.entries(root, mutableState.value.currentPath)) }
    }

    fun moveSelected(destination: String) = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        val document = mutableState.value.document ?: return@withClient
        api.moveEntry(root, document.entry.path, destination.trim('/'))
        update { copy(document = null, directory = api.entries(root, mutableState.value.currentPath)) }
    }

    fun trashSelected() = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        val document = mutableState.value.document ?: return@withClient
        api.trashEntry(root, document.entry.path)
        update {
            copy(document = null, directory = api.entries(root, currentPath), trash = api.trash())
        }
    }

    fun upload(name: String, length: Long, openInput: () -> InputStream) = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        api.writeFile(root, childPath(mutableState.value.currentPath, name), length, openInput)
        update { copy(directory = api.entries(root, mutableState.value.currentPath)) }
    }

    fun replaceSelected(length: Long, openInput: () -> InputStream) = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        val document = mutableState.value.document ?: return@withClient
        val etag = document.etag.ifBlank { api.readFile(root, document.entry.path).etag }
        api.writeFile(root, document.entry.path, length, openInput, etag)
        openEntry(document.entry.copy(size = length))
    }

    fun downloadSelected(openOutput: () -> OutputStream) = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        val entry = mutableState.value.document?.entry ?: return@withClient
        api.downloadFile(root, entry.path, openOutput())
    }

    fun loadTrash() = withClient { api -> update { copy(trash = api.trash()) } }

    fun restoreTrash(id: String) = withClient { api ->
        api.restoreTrash(id)
        update { copy(trash = api.trash()) }
        loadDirectory()
    }

    private fun restoreConnection() {
        viewModelScope.launch {
            val stored = withContext(Dispatchers.IO) { store.load() }
            if (stored == null) {
                update { copy(restoring = false) }
                return@launch
            }
            runCatching {
                val restored = DshClient(stored.endpoint, stored.token)
                val device = withContext(Dispatchers.IO) { restored.currentDevice() }
                val snapshot = withContext(Dispatchers.IO) { loadSnapshot(restored, device) }
                client = restored
                update { snapshot.copy(restoring = false, connected = true, endpointDraft = restored.endpoint.removeSuffix("/api/v1")) }
                connectEvents(restored)
            }.onFailure {
                store.clear()
                update { copy(restoring = false, error = messageOf(it)) }
            }
        }
    }

    private fun connectEvents(api: DshClient) {
        eventSubscription?.close()
        eventSubscription = api.events(
            listener = { event ->
                event.assistantStreamStart()?.let { start ->
                    assistantTurns[streamTurnKey(start.sessionId, start.attemptId)] = start.turn to start.step
                }
                val delta = event.chatDelta(assistantTurns)
                if (delta != null) {
                    if (mutableState.value.selectedSessionId == delta.sessionId) {
                        update { copy(liveChat = (liveChat ?: LiveChatState(delta.sessionId, delta.turn, delta.step)).append(delta)) }
                    }
                } else if (event.type.startsWith("chat.")) {
                    val sessionId = event.dataString("sessionId")
                    when (event.type) {
                        "chat.message.committed" -> {
                            if (sessionId != null && sessionId == mutableState.value.selectedSessionId) {
                                if (event.dataString("role") == "assistant") {
                                    val turn = event.dataInt("turn")
                                    val step = event.dataInt("step")
                                    if (turn != null && step != null) liveFinalizeTarget = LiveKey(sessionId, turn, step)
                                }
                                scheduleChatRefresh()
                            }
                        }
                        "chat.turn.start", "chat.turn.end", "chat.session.status" -> {
                            chatSessionRefreshPending = true
                            scheduleChatRefresh()
                        }
                        "chat.agent-preset.selected" -> {
                            chatSessionRefreshPending = true
                            scheduleChatRefresh()
                            if (sessionId == mutableState.value.selectedSessionId) refreshSessionModels()
                        }
                        "chat.approval.requested", "chat.approval.resolved" -> {
                            chatSessionRefreshPending = true
                            scheduleChatRefresh()
                            if (sessionId == mutableState.value.selectedSessionId) refreshPendingApprovals()
                        }
                        else -> if (sessionId == mutableState.value.selectedSessionId) scheduleChatRefresh()
                    }
                }
                if (event.type.startsWith("file.")) loadDirectory()
            },
            connection = { connected ->
                update { copy(eventsConnected = connected) }
                if (connected && mutableState.value.selectedSessionId != null) {
                    scheduleChatRefresh()
                    refreshPendingApprovals()
                    refreshCommands()
                }
            },
        )
    }

    private fun loadSnapshot(api: DshClient, device: DeviceView): HarnessState {
        val roots = if ("files.read" in device.scopes) api.roots() else emptyList()
        val canReadChat = "chat.read" in device.scopes
        val sessions = if (canReadChat) api.sessions() else emptyList()
        val chatWorkspaces = if (canReadChat) api.chatWorkspaces() else emptyList()
        val agentPresets = if (canReadChat) api.agentPresets() else emptyList()
        val canReadSettings = "settings.read" in device.scopes
        val selectedRoot = roots.firstOrNull()?.id
        val directory = selectedRoot?.let { api.entries(it) }
        return HarnessState(
            restoring = false,
            connected = true,
            endpoint = api.endpoint,
            endpointDraft = api.endpoint.removeSuffix("/api/v1"),
            healthOk = true,
            device = device,
            roots = roots,
            sessions = sessions,
            chatWorkspaces = chatWorkspaces,
            agentPresets = agentPresets,
            providerSettings = if (canReadSettings) api.providerSettings() else null,
            selectedRootId = selectedRoot,
            directory = directory,
            trash = if ("files.read" in device.scopes) api.trash() else emptyList(),
        )
    }

    private fun scheduleChatRefresh() {
        chatRefreshPending = true
        if (chatRefreshJob?.isActive == true) return
        chatRefreshJob = viewModelScope.launch {
            delay(250)
            while (chatRefreshPending || chatSessionRefreshPending) {
                chatRefreshPending = false
                val refreshSessions = chatSessionRefreshPending
                chatSessionRefreshPending = false
                val finalizeTarget = liveFinalizeTarget
                liveFinalizeTarget = null
                val api = client ?: break
                val id = mutableState.value.selectedSessionId
                if (id != null) {
                    runCatching { withContext(Dispatchers.IO) { api.history(id) } }
                        .onSuccess { history ->
                            if (mutableState.value.selectedSessionId == id) update {
                                val clearLive = finalizeTarget != null && liveChat?.let {
                                    it.sessionId == finalizeTarget.sessionId && it.turn == finalizeTarget.turn && it.step == finalizeTarget.step
                                } == true
                                withSessionHistory(id, history).copy(liveChat = if (clearLive) null else liveChat)
                            }
                        }
                        .onFailure { error ->
                            if (error !is DshApiException || error.code != "RATE_LIMITED") {
                                update { copy(error = messageOf(error)) }
                            } else {
                                if (finalizeTarget != null) liveFinalizeTarget = finalizeTarget
                                chatRefreshPending = true
                            }
                        }
                }
                if (refreshSessions) {
                    runCatching { withContext(Dispatchers.IO) { api.sessions() } }
                        .onSuccess { sessions -> update { copy(sessions = sessions.preservingTitles(this.sessions)) } }
                        .onFailure { error ->
                            if (error is DshApiException && error.code == "RATE_LIMITED") {
                                chatSessionRefreshPending = true
                            } else {
                                update { copy(error = messageOf(error)) }
                            }
                        }
                }
                delay(1_000)
            }
        }
    }

    private fun withClient(block: suspend (DshClient) -> Unit) {
        val api = client ?: return
        launchOperation { block(api) }
    }

    private fun launchOperation(block: suspend () -> Unit) {
        viewModelScope.launch {
            update { copy(busy = true, error = null) }
            runCatching { withContext(Dispatchers.IO) { block() } }
                .onFailure { update { copy(error = messageOf(it)) } }
            update { copy(busy = false) }
        }
    }

    private fun update(transform: HarnessState.() -> HarnessState) {
        mutableState.update { current -> current.transform() }
    }

    override fun onCleared() {
        chatRefreshJob?.cancel()
        eventSubscription?.close()
        super.onCleared()
    }

    private data class DecodedText(val text: String, val hasBom: Boolean, val lineEnding: String)
    private data class LiveKey(val sessionId: String, val turn: Int, val step: Int)

    private fun decodeUtf8(bytes: ByteArray): DecodedText? {
        val hasBom = bytes.size >= 3 && bytes.copyOfRange(0, 3).contentEquals(UTF8_BOM)
        val content = if (hasBom) bytes.copyOfRange(3, bytes.size) else bytes
        val decoder = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        val text = runCatching { decoder.decode(ByteBuffer.wrap(content)).toString() }.getOrNull() ?: return null
        if ('\u0000' in text) return null
        val lineEnding = when {
            "\r\n" in text -> "\r\n"
            '\r' in text -> "\r"
            else -> "\n"
        }
        return DecodedText(text, hasBom, lineEnding)
    }

    private fun childPath(parent: String, name: String): String =
        if (parent.isBlank()) name.trim('/') else "$parent/${name.trim('/')}"

    private fun HarnessState.withSessionHistory(sessionId: String, history: ChatHistory): HarnessState {
        val title = history.sessionTitle()
        val nextSessions = if (title.isNullOrBlank()) sessions else sessions.map { session ->
            if (session.id == sessionId && session.title.isNullOrBlank()) session.copy(title = title) else session
        }
        return copy(history = history, sessions = nextSessions)
    }

    private fun messageOf(error: Throwable): String = when (error) {
        is DshApiException -> "${error.code}: ${error.message}"
        else -> error.message ?: error::class.java.simpleName
    }

    private companion object {
        const val MAX_EDIT_BYTES = 2L * 1024 * 1024
        val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    }
}

private fun List<ChatSession>.preservingTitles(previous: List<ChatSession>): List<ChatSession> {
    if (previous.isEmpty()) return this
    val titles = previous.associate { it.id to it.title }
    return map { session ->
        if (!session.title.isNullOrBlank()) session
        else titles[session.id]?.takeIf { !it.isNullOrBlank() }?.let { session.copy(title = it) } ?: session
    }
}

private fun WorkspaceEvent.dataString(key: String): String? =
    ((data as? JsonObject)?.get(key) as? JsonPrimitive)?.contentOrNull

private fun WorkspaceEvent.dataInt(key: String): Int? =
    ((data as? JsonObject)?.get(key) as? JsonPrimitive)?.intOrNull

private fun List<PendingApproval>.toApprovalState(): ApprovalUiState =
    if (isEmpty()) ApprovalUiState.None else ApprovalUiState.Pending(this)
