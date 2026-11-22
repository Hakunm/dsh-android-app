package io.github.hakunm.deepseekharness

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.hakunm.deepseekharness.data.AgentPreset
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
import io.github.hakunm.deepseekharness.data.FileSearchResult
import io.github.hakunm.deepseekharness.data.ImageAttachment
import io.github.hakunm.deepseekharness.data.LiveChatState
import io.github.hakunm.deepseekharness.data.ModelSelection
import io.github.hakunm.deepseekharness.data.PendingApproval
import io.github.hakunm.deepseekharness.data.PendingQuestion
import io.github.hakunm.deepseekharness.data.PermissionCatalog
import io.github.hakunm.deepseekharness.data.ProviderModel
import io.github.hakunm.deepseekharness.data.ProviderPatch
import io.github.hakunm.deepseekharness.data.ProviderSettings
import io.github.hakunm.deepseekharness.data.QuestionAnswerItem
import io.github.hakunm.deepseekharness.data.RootView
import io.github.hakunm.deepseekharness.data.SecureConnectionStore
import io.github.hakunm.deepseekharness.data.SessionModels
import io.github.hakunm.deepseekharness.data.SessionSearchResult
import io.github.hakunm.deepseekharness.data.SessionState
import io.github.hakunm.deepseekharness.data.TrashEntry
import io.github.hakunm.deepseekharness.data.WorkspaceEvent
import io.github.hakunm.deepseekharness.data.assistantStreamStart
import io.github.hakunm.deepseekharness.data.chatDelta
import io.github.hakunm.deepseekharness.data.preservingTitles
import io.github.hakunm.deepseekharness.data.withSessionHistory
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
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

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class PendingImageAttachment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mediaType: String,
    val base64Data: String,
    val previewBytes: ByteArray,
) {
    fun toWire(): ImageAttachment = ImageAttachment(
        type = "image",
        mediaType = mediaType,
        data = base64Data,
        name = name,
    )
}

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

sealed interface QuestionUiState {
    data object None : QuestionUiState
    data object Loading : QuestionUiState
    data class Pending(val items: List<PendingQuestion>) : QuestionUiState
    data class Submitting(val item: PendingQuestion, val items: List<PendingQuestion>) : QuestionUiState
}

data class HarnessState(
    val restoring: Boolean = true,
    val hasSavedConnection: Boolean = false,
    val busy: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val endpointDraft: String = "http://192.168.1.20:3090",
    val healthOk: Boolean = false,
    val serverPluginVersion: String? = null,
    val capabilities: List<String> = emptyList(),
    val connected: Boolean = false,
    val endpoint: String = "",
    val device: DeviceView? = null,
    val roots: List<RootView> = emptyList(),
    val sessions: List<ChatSession> = emptyList(),
    val showArchived: Boolean = false,
    val sessionSearchQuery: String = "",
    val sessionSearchResults: SessionSearchResult? = null,
    val searchingSessions: Boolean = false,
    val chatWorkspaces: List<ChatWorkspace> = emptyList(),
    val agentPresets: List<AgentPreset> = emptyList(),
    val permissionCatalog: PermissionCatalog? = null,
    val workspacePickerRootId: String? = null,
    val workspacePickerPath: String = "",
    val workspacePickerDirectory: DirectoryPage? = null,
    val workspacePickerError: String? = null,
    val sessionModels: SessionModels? = null,
    val sessionState: SessionState? = null,
    val commands: List<CommandDescriptor> = emptyList(),
    val pendingImages: List<PendingImageAttachment> = emptyList(),
    val providerSettings: ProviderSettings? = null,
    val discoveredModels: List<ProviderModel> = emptyList(),
    val selectedSessionId: String? = null,
    val history: ChatHistory? = null,
    val liveChat: LiveChatState? = null,
    val approvalState: ApprovalUiState = ApprovalUiState.None,
    val questionState: QuestionUiState = QuestionUiState.None,
    val eventsConnected: Boolean = false,
    val selectedRootId: String? = null,
    val currentPath: String = "",
    val directory: DirectoryPage? = null,
    val directoryError: String? = null,
    val fileSearchQuery: String = "",
    val fileSearchResults: FileSearchResult? = null,
    val searchingFiles: Boolean = false,
    val document: OpenDocument? = null,
    val trash: List<TrashEntry> = emptyList(),
    val error: String? = null,
)

class HarnessViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SecureConnectionStore(application)
    private val uiPrefs = application.getSharedPreferences("dsh_ui_prefs", Context.MODE_PRIVATE)
    private val mutableState = MutableStateFlow(HarnessState(themeMode = loadThemeMode()))
    val state: StateFlow<HarnessState> = mutableState.asStateFlow()
    private var client: DshClient? = null
    private var eventSubscription: Closeable? = null
    private var chatRefreshJob: Job? = null
    private val streamTurns = ConcurrentHashMap<String, Int>()
    @Volatile private var chatRefreshPending = false
    @Volatile private var chatSessionRefreshPending = false
    @Volatile private var liveFinalizeTarget: LiveKey? = null

    init {
        restoreConnection()
    }

    fun setThemeMode(mode: ThemeMode) {
        uiPrefs.edit { putString("theme_mode", mode.name) }
        update { copy(themeMode = mode) }
    }

    private fun loadThemeMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(uiPrefs.getString("theme_mode", ThemeMode.SYSTEM.name).orEmpty()) }
            .getOrDefault(ThemeMode.SYSTEM)

    fun setEndpointDraft(value: String) = update { copy(endpointDraft = value, healthOk = false, error = null) }

    fun testConnection(endpoint: String) = launchOperation {
        val normalized = DshClient.normalizeEndpoint(endpoint)
        val health = DshClient(normalized).health()
        update {
            copy(
                endpointDraft = normalized.removeSuffix("/api/v1"),
                healthOk = health.ok,
                serverPluginVersion = health.pluginVersion,
                capabilities = health.capabilities,
            )
        }
    }

    fun pair(endpoint: String, code: String, deviceName: String) = launchOperation {
        require(code.isNotBlank()) { "Pairing code is required." }
        require(deviceName.isNotBlank()) { "Device name is required." }
        val unauthenticated = DshClient(endpoint)
        val health = unauthenticated.health()
        val pairing = unauthenticated.pair(code, deviceName)
        val paired = DshClient(unauthenticated.endpoint, pairing.token)
        store.save(unauthenticated.endpoint, pairing.token)
        client = paired
        val snapshot = loadSnapshot(paired, pairing.device, health.pluginVersion, health.capabilities)
        update {
            snapshot.copy(
                restoring = false,
                hasSavedConnection = true,
                busy = true,
                connected = true,
                healthOk = true,
                themeMode = themeMode,
            )
        }
        connectEvents(paired)
    }

    fun retryRestore() {
        update { copy(restoring = true, error = null) }
        restoreConnection()
    }

    fun disconnect() {
        eventSubscription?.close()
        eventSubscription = null
        chatRefreshJob?.cancel()
        streamTurns.clear()
        client = null
        store.clear()
        val currentTheme = mutableState.value.themeMode
        val lastDraft = mutableState.value.endpointDraft
        mutableState.value = HarnessState(
            restoring = false,
            hasSavedConnection = false,
            themeMode = currentTheme,
            endpointDraft = lastDraft,
        )
    }

    fun clearError() = update { copy(error = null) }

    fun refreshDashboard() = withClient { api ->
        val health = runCatching { api.health() }.getOrNull()
        val device = api.currentDevice()
        val showArchived = mutableState.value.showArchived
        val snapshot = loadSnapshot(
            api = api,
            device = device,
            pluginVersion = health?.pluginVersion ?: mutableState.value.serverPluginVersion,
            capabilities = health?.capabilities ?: mutableState.value.capabilities,
            includeArchived = showArchived,
            preferredRootId = mutableState.value.selectedRootId,
        )
        update {
            snapshot.copy(
                restoring = false,
                hasSavedConnection = true,
                busy = true,
                themeMode = themeMode,
                showArchived = showArchived,
                sessions = snapshot.sessions.preservingTitles(sessions),
                selectedSessionId = selectedSessionId?.takeIf { id -> snapshot.sessions.any { it.id == id } },
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
                sessionState = null,
                commands = emptyList(),
                pendingImages = emptyList(),
                approvalState = if (id == null) ApprovalUiState.None else ApprovalUiState.Loading,
                questionState = if (id == null) QuestionUiState.None else QuestionUiState.Loading,
            )
        }
        if (id != null) {
            refreshHistory()
            refreshSessionModels()
            refreshPendingApprovals()
            refreshPendingQuestions()
            refreshSessionState()
            refreshCommands()
        }
    }

    fun toggleShowArchived() {
        val next = !mutableState.value.showArchived
        update { copy(showArchived = next) }
        refreshSessionsList(next)
        if (mutableState.value.sessionSearchQuery.isNotBlank()) {
            searchSessions(mutableState.value.sessionSearchQuery)
        }
    }

    private fun refreshSessionsList(includeArchived: Boolean = mutableState.value.showArchived) = withClient { api ->
        val fresh = api.sessions(includeArchived)
        update { copy(sessions = fresh.preservingTitles(sessions)) }
    }

    fun setSessionSearchQuery(query: String) {
        update { copy(sessionSearchQuery = query) }
        if (query.isBlank()) {
            update { copy(sessionSearchResults = null, searchingSessions = false) }
        }
    }

    fun searchSessions(query: String = mutableState.value.sessionSearchQuery) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            update { copy(sessionSearchQuery = "", sessionSearchResults = null, searchingSessions = false) }
            return
        }
        val api = client ?: return
        val includeArchived = mutableState.value.showArchived
        viewModelScope.launch {
            update { copy(sessionSearchQuery = query, searchingSessions = true, error = null) }
            runCatching { withContext(Dispatchers.IO) { api.searchSessions(trimmed, includeArchived) } }
                .onSuccess { results ->
                    update { copy(sessionSearchResults = results, searchingSessions = false) }
                }
                .onFailure { error ->
                    update { copy(searchingSessions = false, error = messageOf(error)) }
                }
        }
    }

    fun clearSessionSearch() = update {
        copy(sessionSearchQuery = "", sessionSearchResults = null, searchingSessions = false)
    }

    fun togglePinSession(session: ChatSession) = withClient { api ->
        if (session.pinned) api.unpinSession(session.id) else api.pinSession(session.id)
        val fresh = api.sessions(mutableState.value.showArchived)
        update { copy(sessions = fresh.preservingTitles(sessions)) }
    }

    fun unarchiveSession(sessionId: String) = withClient { api ->
        api.unarchiveSession(sessionId)
        val fresh = api.sessions(mutableState.value.showArchived)
        update { copy(sessions = fresh.preservingTitles(sessions)) }
    }

    fun refreshHistory() = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        val history = api.history(id)
        update { copy(history = history, sessions = sessions.withSessionHistory(id, history)) }
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
        val sessions = api.sessions(mutableState.value.showArchived)
        val history = api.history(created.id)
        val models = api.sessionModels(created.id)
        val commands = runCatching { api.sessionCommands(created.id) }.getOrDefault(emptyList())
        update {
            copy(
                sessions = sessions.preservingTitles(this.sessions).withSessionHistory(created.id, history),
                selectedSessionId = created.id,
                history = history,
                sessionModels = models,
                sessionState = null,
                commands = commands,
                pendingImages = emptyList(),
                approvalState = ApprovalUiState.None,
                questionState = QuestionUiState.None,
            )
        }
    }

    fun prepareWorkspacePicker() {
        val rootId = mutableState.value.workspacePickerRootId
            ?.takeIf { id -> mutableState.value.roots.any { it.id == id } }
            ?: mutableState.value.selectedRootId
            ?: mutableState.value.roots.firstOrNull()?.id
        update {
            copy(
                workspacePickerRootId = rootId,
                workspacePickerPath = "",
                workspacePickerDirectory = null,
                workspacePickerError = null,
            )
        }
        if (rootId != null) loadWorkspacePickerDirectory("")
    }

    fun selectWorkspacePickerRoot(rootId: String) {
        update {
            copy(
                workspacePickerRootId = rootId,
                workspacePickerPath = "",
                workspacePickerDirectory = null,
                workspacePickerError = null,
            )
        }
        loadWorkspacePickerDirectory("")
    }

    fun loadWorkspacePickerDirectory(path: String) = withClient { api ->
        val rootId = mutableState.value.workspacePickerRootId ?: return@withClient
        val rootLabel = mutableState.value.roots.find { it.id == rootId }?.label
        val normalized = path.trim('/')
        runCatching { api.entries(rootId, normalized) }
            .onSuccess { page ->
                update {
                    copy(
                        workspacePickerPath = normalized,
                        workspacePickerDirectory = page,
                        workspacePickerError = null,
                    )
                }
            }
            .onFailure { err ->
                val friendly = classifyDirectoryAccessError(rootLabel, normalized, err)
                val keepParent = normalized.isNotEmpty() &&
                    mutableState.value.workspacePickerPath != normalized &&
                    mutableState.value.workspacePickerDirectory != null
                if (keepParent) {
                    update { copy(error = friendly) }
                } else {
                    update {
                        copy(
                            workspacePickerPath = normalized,
                            workspacePickerDirectory = null,
                            workspacePickerError = friendly,
                            error = friendly,
                        )
                    }
                }
            }
    }

    fun createWorkspaceAndSession(rootId: String, path: String, preset: String?) = withClient { api ->
        val workspace = api.createChatWorkspace(rootId, path.trim('/'))
        val created = api.createSession(workspace.id, preset?.takeIf(String::isNotBlank))
        val sessions = api.sessions(mutableState.value.showArchived)
        val workspaces = api.chatWorkspaces()
        val history = api.history(created.id)
        update {
            copy(
                chatWorkspaces = workspaces,
                sessions = sessions.preservingTitles(this.sessions).withSessionHistory(created.id, history),
                selectedSessionId = created.id,
                history = history,
                sessionModels = api.sessionModels(created.id),
                sessionState = null,
                commands = runCatching { api.sessionCommands(created.id) }.getOrDefault(emptyList()),
                pendingImages = emptyList(),
                approvalState = ApprovalUiState.None,
                questionState = QuestionUiState.None,
            )
        }
    }

    fun renameWorkspace(workspaceId: String, title: String) = withClient { api ->
        api.renameChatWorkspace(workspaceId, title)
        val fresh = api.sessions(mutableState.value.showArchived)
        update { copy(chatWorkspaces = api.chatWorkspaces(), sessions = fresh.preservingTitles(sessions)) }
    }

    fun deleteWorkspace(workspaceId: String) = withClient { api ->
        api.deleteChatWorkspace(workspaceId)
        val fresh = api.sessions(mutableState.value.showArchived)
        update { copy(chatWorkspaces = api.chatWorkspaces(), sessions = fresh.preservingTitles(sessions)) }
    }

    fun renameSession(sessionId: String, title: String) = withClient { api ->
        api.renameSession(sessionId, title)
        val fresh = api.sessions(mutableState.value.showArchived)
        update { copy(sessions = fresh.preservingTitles(sessions)) }
    }

    fun forkSession(sessionId: String) = withClient { api ->
        val forkedId = api.forkSession(sessionId)
        val fresh = api.sessions(mutableState.value.showArchived)
        val history = api.history(forkedId)
        update {
            copy(
                sessions = fresh.preservingTitles(sessions).withSessionHistory(forkedId, history),
                selectedSessionId = forkedId,
                history = history,
                sessionModels = api.sessionModels(forkedId),
                sessionState = null,
                commands = runCatching { api.sessionCommands(forkedId) }.getOrDefault(emptyList()),
                pendingImages = emptyList(),
                approvalState = ApprovalUiState.None,
                questionState = QuestionUiState.None,
                liveChat = null,
            )
        }
    }

    fun archiveSession(sessionId: String) = withClient { api ->
        api.archiveSession(sessionId)
        val sessions = api.sessions(mutableState.value.showArchived)
        update {
            if (selectedSessionId == sessionId && !showArchived) {
                copy(
                    sessions = sessions.preservingTitles(this.sessions),
                    selectedSessionId = null,
                    history = null,
                    liveChat = null,
                    sessionModels = null,
                    sessionState = null,
                    commands = emptyList(),
                    pendingImages = emptyList(),
                    approvalState = ApprovalUiState.None,
                    questionState = QuestionUiState.None,
                )
            } else copy(sessions = sessions.preservingTitles(this.sessions))
        }
    }

    fun addPendingImage(name: String, mediaType: String, bytes: ByteArray) {
        val normalizedType = when (mediaType.lowercase()) {
            "image/jpg" -> "image/jpeg"
            else -> mediaType.lowercase()
        }
        if (normalizedType !in SUPPORTED_IMAGE_TYPES) {
            update { copy(error = "Unsupported image format: $mediaType") }
            return
        }
        if (bytes.size > MAX_IMAGE_BYTES) {
            update { copy(error = "IMAGE_TOO_LARGE") }
            return
        }
        val attachment = PendingImageAttachment(
            name = name.ifBlank { "image.png" },
            mediaType = normalizedType,
            base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP),
            previewBytes = bytes,
        )
        update { copy(pendingImages = (pendingImages + attachment).take(MAX_IMAGE_COUNT)) }
    }

    fun removePendingImage(id: String) = update {
        copy(pendingImages = pendingImages.filterNot { it.id == id })
    }

    fun sendFromHome(
        workspaceId: String?,
        preset: String?,
        text: String,
        initialPermission: String? = null,
    ) = withClient { api ->
        val images = mutableState.value.pendingImages
        if (text.isBlank() && images.isEmpty()) return@withClient
        val wsId = workspaceId?.takeIf(String::isNotBlank)
            ?: mutableState.value.chatWorkspaces.firstOrNull()?.id
            ?: return@withClient
        val created = api.createSession(wsId, preset?.takeIf(String::isNotBlank))
        if (!initialPermission.isNullOrBlank()) {
            runCatching { api.executeCommand(created.id, "/permission $initialPermission") }
        }
        val models = runCatching { api.sessionModels(created.id) }.getOrNull()
        val commands = runCatching { api.sessionCommands(created.id) }.getOrDefault(emptyList())
        val line = text.trimEnd()
        if (images.isEmpty() && line.startsWith('/')) {
            val execution = api.executeCommand(created.id, line)
            if (execution.result.kind == "error") {
                throw DshApiException(409, "COMMAND_FAILED", execution.result.text ?: "Command failed.")
            }
            val history = api.history(created.id)
            val sessions = api.sessions(mutableState.value.showArchived)
            update {
                copy(
                    sessions = sessions.preservingTitles(this.sessions).withSessionHistory(created.id, history),
                    selectedSessionId = created.id,
                    history = history,
                    sessionModels = models,
                    sessionState = null,
                    commands = commands,
                    pendingImages = emptyList(),
                    approvalState = ApprovalUiState.None,
                    questionState = QuestionUiState.None,
                    liveChat = null,
                )
            }
            return@withClient
        }
        api.sendMessage(created.id, text, false, images.map(PendingImageAttachment::toWire))
        val history = api.history(created.id)
        val sessions = api.sessions(mutableState.value.showArchived)
        update {
            copy(
                sessions = sessions.preservingTitles(this.sessions).withSessionHistory(created.id, history),
                selectedSessionId = created.id,
                history = history,
                sessionModels = models,
                sessionState = null,
                commands = commands,
                pendingImages = emptyList(),
                approvalState = ApprovalUiState.None,
                questionState = QuestionUiState.None,
                liveChat = null,
            )
        }
        chatSessionRefreshPending = true
        scheduleChatRefresh()
        refreshSessionState()
    }

    fun sendMessage(text: String, steer: Boolean) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        val images = mutableState.value.pendingImages
        if (text.isBlank() && images.isEmpty()) return@withClient
        val line = text.trimEnd()
        if (images.isEmpty() && line.startsWith('/')) {
            val execution = api.executeCommand(id, line)
            if (execution.result.kind == "error") {
                throw DshApiException(409, "COMMAND_FAILED", execution.result.text ?: "Command failed.")
            }
            val history = api.history(id)
            val cmds = runCatching { api.sessionCommands(id) }.getOrDefault(mutableState.value.commands)
            update { copy(history = history, commands = cmds, sessions = sessions.withSessionHistory(id, history)) }
            return@withClient
        }
        api.sendMessage(id, text, steer, images.map(PendingImageAttachment::toWire))
        update { copy(pendingImages = emptyList()) }
        chatSessionRefreshPending = true
        scheduleChatRefresh()
        refreshSessionState()
    }

    fun refreshSessionState() {
        val api = client ?: return
        val id = mutableState.value.selectedSessionId ?: return
        viewModelScope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { api.sessionState(id) } }
            if (mutableState.value.selectedSessionId != id) return@launch
            result.onSuccess { sessionState -> update { copy(sessionState = sessionState) } }
        }
    }

    fun editQueueItem(itemId: String, newText: String) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        api.updateQueue(id, itemId, "edit", newText)
        refreshSessionState()
    }

    fun removeQueueItem(itemId: String) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        api.updateQueue(id, itemId, "remove")
        refreshSessionState()
    }

    fun steerQueueItem(itemId: String) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        api.updateQueue(id, itemId, "steer")
        refreshSessionState()
    }

    fun refreshCommands() = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        val commands = runCatching { api.sessionCommands(id) }.getOrDefault(emptyList())
        update { copy(commands = commands) }
    }

    fun selectPermissionPreset(preset: String) = withClient { api ->
        val id = mutableState.value.selectedSessionId ?: return@withClient
        val execution = api.executeCommand(id, "/permission $preset")
        if (execution.result.kind == "error") {
            throw DshApiException(409, "COMMAND_FAILED", execution.result.text ?: "Permission change failed.")
        }
        val history = api.history(id)
        val cmds = runCatching { api.sessionCommands(id) }.getOrDefault(mutableState.value.commands)
        update { copy(history = history, commands = cmds) }
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
        val sessions = api.sessions(mutableState.value.showArchived)
        val models = api.sessionModels(id)
        val commands = runCatching { api.sessionCommands(id) }.getOrDefault(emptyList())
        update {
            copy(
                sessions = sessions.preservingTitles(this.sessions),
                sessionModels = models,
                commands = commands,
            )
        }
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

    fun removeProvider(providerId: String, expectedRevision: Int? = null) = withClient { api ->
        api.removeProvider(providerId, expectedRevision)
        update { copy(providerSettings = api.providerSettings(), discoveredModels = emptyList()) }
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
        refreshSessionState()
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
                    api.pendingApprovals(id) to api.sessions(mutableState.value.showArchived)
                }
            }.onSuccess { (approvals, sessions) ->
                if (mutableState.value.selectedSessionId == id) {
                    update {
                        copy(
                            approvalState = approvals.toApprovalState(),
                            sessions = sessions.preservingTitles(this.sessions),
                        )
                    }
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

    fun refreshPendingQuestions() {
        val api = client ?: return
        val id = mutableState.value.selectedSessionId ?: return
        viewModelScope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { api.pendingQuestions(id) } }
            if (mutableState.value.selectedSessionId != id) return@launch
            result
                .onSuccess { questions -> update { copy(questionState = questions.toQuestionState()) } }
                .onFailure { error ->
                    val silent = error is DshApiException && (error.status == 404 || error.status == 501 || error.code == "RATE_LIMITED")
                    update {
                        copy(
                            questionState = QuestionUiState.None,
                            error = if (silent) null else messageOf(error),
                        )
                    }
                }
        }
    }

    fun answerQuestion(question: PendingQuestion, answers: List<QuestionAnswerItem>) {
        val api = client ?: return
        val id = mutableState.value.selectedSessionId ?: return
        if (question.sessionId != id) return
        val items = when (val current = mutableState.value.questionState) {
            is QuestionUiState.Pending -> current.items
            is QuestionUiState.Submitting -> current.items
            else -> listOf(question)
        }
        viewModelScope.launch {
            update { copy(questionState = QuestionUiState.Submitting(question, items), error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    api.answerQuestion(id, question.id, answers)
                    api.pendingQuestions(id) to api.sessions(mutableState.value.showArchived)
                }
            }.onSuccess { (questions, sessions) ->
                if (mutableState.value.selectedSessionId == id) {
                    update {
                        copy(
                            questionState = questions.toQuestionState(),
                            sessions = sessions.preservingTitles(this.sessions),
                        )
                    }
                    chatSessionRefreshPending = true
                    scheduleChatRefresh()
                }
            }.onFailure { error ->
                if (mutableState.value.selectedSessionId == id) {
                    update { copy(questionState = QuestionUiState.Pending(items), error = messageOf(error)) }
                }
            }
        }
    }

    fun selectRoot(id: String) {
        update {
            copy(
                selectedRootId = id,
                currentPath = "",
                directory = null,
                directoryError = null,
                document = null,
                fileSearchQuery = "",
                fileSearchResults = null,
            )
        }
        loadDirectory("")
    }

    fun setFileSearchQuery(query: String) {
        update { copy(fileSearchQuery = query) }
        if (query.isBlank()) {
            update { copy(fileSearchResults = null, searchingFiles = false) }
        }
    }

    fun searchFiles(query: String = mutableState.value.fileSearchQuery) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            update { copy(fileSearchQuery = "", fileSearchResults = null, searchingFiles = false) }
            return
        }
        val api = client ?: return
        val root = mutableState.value.selectedRootId ?: return
        val rootLabel = mutableState.value.roots.find { it.id == root }?.label
        val basePath = mutableState.value.currentPath
        viewModelScope.launch {
            update { copy(fileSearchQuery = query, searchingFiles = true, error = null) }
            runCatching { withContext(Dispatchers.IO) { api.searchFiles(root, trimmed, basePath) } }
                .onSuccess { result -> update { copy(fileSearchResults = result, searchingFiles = false) } }
                .onFailure { error ->
                    update { copy(searchingFiles = false, error = classifyDirectoryAccessError(rootLabel, basePath, error)) }
                }
        }
    }

    fun clearFileSearch() = update {
        copy(fileSearchQuery = "", fileSearchResults = null, searchingFiles = false)
    }

    fun loadDirectory(path: String = mutableState.value.currentPath) = withClient { api ->
        val root = mutableState.value.selectedRootId ?: return@withClient
        val rootLabel = mutableState.value.roots.find { it.id == root }?.label
        val normalized = path.trim('/')
        runCatching { api.entries(root, normalized) }
            .onSuccess { page ->
                update { copy(currentPath = normalized, directory = page, directoryError = null, document = null) }
            }
            .onFailure { err ->
                val friendly = classifyDirectoryAccessError(rootLabel, normalized, err)
                val keepParent = normalized.isNotEmpty() &&
                    mutableState.value.currentPath != normalized &&
                    mutableState.value.directory != null
                if (keepParent) {
                    update { copy(document = null, error = friendly) }
                } else {
                    update {
                        copy(
                            currentPath = normalized,
                            directory = null,
                            directoryError = friendly,
                            document = null,
                            error = friendly,
                        )
                    }
                }
            }
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
            update { copy(fileSearchQuery = "", fileSearchResults = null) }
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
        }.toByteArray(Charsets.UTF_8)
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
                update { copy(restoring = false, hasSavedConnection = false) }
                return@launch
            }
            val draft = stored.endpoint.removeSuffix("/api/v1")
            update { copy(endpointDraft = draft, hasSavedConnection = true) }
            runCatching {
                val restored = DshClient(stored.endpoint, stored.token)
                val health = withContext(Dispatchers.IO) { runCatching { restored.health() }.getOrNull() }
                val device = withContext(Dispatchers.IO) { restored.currentDevice() }
                val snapshot = withContext(Dispatchers.IO) {
                    loadSnapshot(restored, device, health?.pluginVersion, health?.capabilities.orEmpty())
                }
                client = restored
                update {
                    snapshot.copy(
                        restoring = false,
                        hasSavedConnection = true,
                        connected = true,
                        themeMode = themeMode,
                        endpointDraft = draft,
                    )
                }
                connectEvents(restored)
            }.onFailure { error ->
                val unauthorized = error is DshApiException && (error.status == 401 || error.status == 403)
                if (unauthorized) {
                    store.clear()
                    update { copy(restoring = false, hasSavedConnection = false, error = messageOf(error)) }
                } else {
                    update { copy(restoring = false, hasSavedConnection = true, error = messageOf(error)) }
                }
            }
        }
    }

    private fun connectEvents(api: DshClient) {
        eventSubscription?.close()
        eventSubscription = api.events(
            listener = { event ->
                event.assistantStreamStart()?.let { (sid, turn) -> streamTurns[sid] = turn }
                val delta = event.chatDelta(streamTurns)
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
                                refreshSessionState()
                            }
                        }
                        "chat.turn.start", "chat.turn.end", "chat.session.status" -> {
                            chatSessionRefreshPending = true
                            scheduleChatRefresh()
                            if (sessionId == mutableState.value.selectedSessionId) refreshSessionState()
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
                        "chat.question.requested", "chat.question.resolved" -> {
                            chatSessionRefreshPending = true
                            scheduleChatRefresh()
                            if (sessionId == mutableState.value.selectedSessionId) refreshPendingQuestions()
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
                    refreshPendingQuestions()
                    refreshSessionState()
                    refreshCommands()
                }
            },
        )
    }

    private fun loadSnapshot(
        api: DshClient,
        device: DeviceView,
        pluginVersion: String? = null,
        capabilities: List<String> = emptyList(),
        includeArchived: Boolean = false,
        preferredRootId: String? = null,
    ): HarnessState {
        val canReadFiles = "files.read" in device.scopes
        val roots = if (canReadFiles) api.roots() else emptyList()
        val canReadChat = "chat.read" in device.scopes
        val sessions = if (canReadChat) api.sessions(includeArchived) else emptyList()
        val chatWorkspaces = if (canReadChat) api.chatWorkspaces() else emptyList()
        val agentPresets = if (canReadChat) api.agentPresets() else emptyList()
        val permissionCatalog = if (canReadChat) runCatching { api.permissionCatalog() }.getOrNull() else null
        val canReadSettings = "settings.read" in device.scopes

        val orderedRoots = if (preferredRootId != null) {
            val preferred = roots.find { it.id == preferredRootId }
            if (preferred != null) listOf(preferred) + roots.filterNot { it.id == preferredRootId } else roots
        } else {
            roots
        }
        var selectedRoot: String? = null
        var directory: DirectoryPage? = null
        var directoryError: String? = null
        var initialError: String? = null
        if (canReadFiles && orderedRoots.isNotEmpty()) {
            var firstFailedRootId: String? = null
            var firstFailure: String? = null
            for (candidate in orderedRoots) {
                val attempt = runCatching { api.entries(candidate.id) }
                if (attempt.isSuccess) {
                    selectedRoot = candidate.id
                    directory = attempt.getOrNull()
                    initialError = firstFailure
                    break
                } else {
                    val err = attempt.exceptionOrNull()
                    if (err != null && firstFailure == null) {
                        firstFailedRootId = candidate.id
                        firstFailure = classifyDirectoryAccessError(candidate.label, "", err)
                    }
                }
            }
            if (directory == null) {
                selectedRoot = firstFailedRootId ?: orderedRoots.first().id
                directoryError = firstFailure
                initialError = firstFailure
            }
        }
        return HarnessState(
            restoring = false,
            hasSavedConnection = true,
            connected = true,
            endpoint = api.endpoint,
            endpointDraft = api.endpoint.removeSuffix("/api/v1"),
            healthOk = true,
            serverPluginVersion = pluginVersion,
            capabilities = capabilities,
            device = device,
            roots = roots,
            sessions = sessions,
            showArchived = includeArchived,
            chatWorkspaces = chatWorkspaces,
            agentPresets = agentPresets,
            permissionCatalog = permissionCatalog,
            providerSettings = if (canReadSettings) api.providerSettings() else null,
            selectedRootId = selectedRoot,
            currentPath = "",
            directory = directory,
            directoryError = directoryError,
            trash = if (canReadFiles) runCatching { api.trash() }.getOrDefault(emptyList()) else emptyList(),
            error = initialError,
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
                                copy(
                                    history = history,
                                    sessions = sessions.withSessionHistory(id, history),
                                    liveChat = if (clearLive) null else liveChat,
                                )
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
                    runCatching { withContext(Dispatchers.IO) { api.sessions(mutableState.value.showArchived) } }
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

    private fun messageOf(error: Throwable): String = formatErrorMessage(error)

    private companion object {
        const val MAX_EDIT_BYTES = 2L * 1024 * 1024
        const val MAX_IMAGE_BYTES = 8 * 1024 * 1024
        const val MAX_IMAGE_COUNT = 4
        val SUPPORTED_IMAGE_TYPES = setOf("image/png", "image/jpeg", "image/webp", "image/gif")
        val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    }
}

private val WINDOWS_DRIVE_ROOT_REGEX = Regex("^[A-Za-z]:[\\\\/]?$")

internal fun isSystemDriveRoot(label: String?): Boolean {
    val trimmed = label?.trim().orEmpty()
    return trimmed == "/" || WINDOWS_DRIVE_ROOT_REGEX.matches(trimmed)
}

internal fun displayDirectoryTarget(rootLabel: String?, relativePath: String): String {
    val root = rootLabel?.trim().orEmpty()
    val rel = relativePath.trim('/')
    return when {
        root.isEmpty() && rel.isEmpty() -> "/"
        root.isEmpty() -> "/$rel"
        rel.isEmpty() -> root
        root.endsWith('\\') || root.endsWith('/') -> "$root$rel"
        else -> "$root/$rel"
    }
}

internal fun classifyDirectoryAccessError(
    rootLabel: String?,
    relativePath: String,
    error: Throwable,
): String {
    val normalizedPath = relativePath.trim('/')
    val target = displayDirectoryTarget(rootLabel, normalizedPath)
    if (error is DshApiException) {
        if (error.code == "LINK_TRAVERSAL_FORBIDDEN") {
            return "LINK_TRAVERSAL_FORBIDDEN|$target"
        }
        if (error.code == "ROOT_NOT_FOUND" || error.code == "PATH_NOT_FOUND") {
            return "DIRECTORY_NOT_FOUND|$target"
        }
        val isProtectedFailure = error.code == "INTERNAL_ERROR" ||
            error.code == "ACCESS_DENIED" ||
            error.code == "PERMISSION_DENIED" ||
            error.status == 403 ||
            error.status == 500
        if (isProtectedFailure) {
            return if (normalizedPath.isEmpty() && isSystemDriveRoot(rootLabel)) {
                "PROTECTED_DRIVE_ROOT|$target"
            } else {
                "PROTECTED_DIRECTORY|$target"
            }
        }
    }
    return formatErrorMessage(error)
}

internal fun formatErrorMessage(error: Throwable): String = when (error) {
    is DshApiException -> "${error.code}: ${error.message}"
    else -> error.message ?: error::class.java.simpleName
}

private fun WorkspaceEvent.dataString(key: String): String? =
    ((data as? JsonObject)?.get(key) as? JsonPrimitive)?.contentOrNull

private fun WorkspaceEvent.dataInt(key: String): Int? =
    ((data as? JsonObject)?.get(key) as? JsonPrimitive)?.intOrNull

private fun List<PendingApproval>.toApprovalState(): ApprovalUiState =
    if (isEmpty()) ApprovalUiState.None else ApprovalUiState.Pending(this)

private fun List<PendingQuestion>.toQuestionState(): QuestionUiState =
    if (isEmpty()) QuestionUiState.None else QuestionUiState.Pending(this)
