package io.github.hakunm.deepseekharness.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

@Serializable
data class HealthView(
    val ok: Boolean,
    val version: String,
    val pluginVersion: String? = null,
    val capabilities: List<String> = emptyList(),
)

@Serializable
data class RootView(val id: String, val label: String, val createdAt: Long)

@Serializable
data class DeviceView(
    val id: String,
    val name: String,
    val scopes: List<String>,
    val rootIds: List<String>,
)

@Serializable
data class PairingResult(val token: String, val device: DeviceView)

@Serializable
data class FileEntry(
    val name: String,
    val path: String,
    val kind: String,
    val size: Long,
    val modifiedAt: Double,
    val writable: Boolean,
)

@Serializable
data class DirectoryPage(val path: String, val entries: List<FileEntry>, val nextCursor: String? = null)

@Serializable
data class FileSearchResult(
    val query: String,
    val path: String = "",
    val entries: List<FileEntry> = emptyList(),
    val truncated: Boolean = false,
)

@Serializable
data class TrashEntry(
    val id: String,
    val rootId: String,
    val path: String,
    val kind: String,
    val size: Long,
    val createdAt: Long,
    val status: String,
)

@Serializable
data class ChatSession(
    val id: String,
    val rootId: String,
    val cwd: String,
    val updatedAt: Long,
    val running: Boolean,
    val blank: Boolean,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val title: String? = null,
    val workspaceId: String? = null,
    val workspaceTitle: String? = null,
    val agentPreset: String? = null,
    val parentSessionId: String? = null,
    val origin: String? = null,
    val pendingInteraction: String? = null,
)

@Serializable
data class SessionSearchHit(
    val sessionId: String,
    val snippet: String = "",
    val session: ChatSession,
)

@Serializable
data class SessionSearchResult(
    val items: List<SessionSearchHit> = emptyList(),
    val hasMore: Boolean = false,
    val mode: String = "metadata",
)

@Serializable
data class PendingApproval(
    val id: String,
    val sessionId: String,
    val toolName: String,
    val reason: String? = null,
    val detail: String? = null,
    val risk: String,
    val requestedAt: Long,
)

@Serializable
data class QuestionOption(
    val label: String,
    val description: String? = null,
)

@Serializable
data class QuestionItem(
    val id: String,
    val question: String,
    val detail: String? = null,
    val header: String? = null,
    val options: List<QuestionOption> = emptyList(),
    val multiSelect: Boolean = false,
)

@Serializable
data class PendingQuestion(
    val id: String,
    val sessionId: String,
    val callId: String? = null,
    val state: String = "open",
    val timed: Boolean = false,
    val questions: List<QuestionItem> = emptyList(),
    val requestedAt: Long = 0L,
)

@Serializable
data class QuestionAnswerItem(
    val id: String,
    val selected: List<String> = emptyList(),
    val custom: String? = null,
)

@Serializable
data class ImageAttachment(
    val type: String = "image",
    val mediaType: String,
    val data: String,
    val name: String? = null,
)

@Serializable
data class SessionQueuedItem(
    val id: String,
    val placement: String = "queue",
    val rpcId: String? = null,
    val content: List<JsonElement> = emptyList(),
) {
    fun displayText(): String {
        val parts = content.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            when ((obj["type"] as? JsonPrimitive)?.contentOrNull) {
                "text" -> (obj["text"] as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotBlank)
                "image" -> (obj["name"] as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotBlank)?.let { "[$it]" }
                    ?: "[Image]"
                else -> null
            }
        }
        return parts.joinToString(" ").trim()
    }
}

@Serializable
data class SessionState(
    val cursor: Int = 0,
    val active: Boolean = false,
    val running: Boolean = false,
    val queue: List<SessionQueuedItem>? = null,
    val pendingInput: List<String> = emptyList(),
)

@Serializable
data class CommandInput(val hint: String)

@Serializable
data class CommandDescriptor(
    val name: String,
    val description: String,
    val input: CommandInput? = null,
)

@Serializable
data class CommandResult(
    val kind: String,
    val text: String? = null,
    val sourceEventSeq: Int? = null,
)

@Serializable
data class CommandExecution(val commandId: String, val result: CommandResult)

data class TodoItem(val content: String, val status: String)

@Serializable
data class PermissionOption(val value: String, val name: String, val description: String? = null)

@Serializable
data class PermissionCatalog(
    val options: List<PermissionOption> = emptyList(),
    val defaultPreset: String = "workspace-write",
)

data class PermissionSelect(val options: List<PermissionOption>, val currentValue: String)

@Serializable
data class CreatedSession(val id: String, val agentPreset: String? = null)

@Serializable
data class RenamedSession(val title: String, val seq: Int)

@Serializable
data class ChatWorkspace(
    val id: String,
    val title: String,
    val rootId: String,
    val path: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class AgentPreset(
    val id: String,
    val name: String,
    val description: String? = null,
    val trust: String = "system",
    val isDefault: Boolean = false,
    val available: Boolean = true,
)

@Serializable
data class ModelSelection(
    val provider: String,
    val model: String,
    val reasoningEffort: String? = null,
)

@Serializable
data class ModelReasoningEffort(val id: String, val name: String, val description: String? = null)

@Serializable
data class ModelReasoning(
    val efforts: List<ModelReasoningEffort>,
    val defaultEffort: String? = null,
)

@Serializable
data class ModelView(
    val id: String,
    val name: String,
    val description: String? = null,
    val contextWindow: Long? = null,
    val maxTokens: Long? = null,
    val reasoning: ModelReasoning? = null,
)

@Serializable
data class ModelProviderGroup(val id: String, val name: String, val models: List<ModelView>)

@Serializable
data class ModelFailure(val provider: String, val message: String)

@Serializable
data class SessionModels(
    val current: ModelSelection,
    val routable: Boolean,
    val groups: List<ModelProviderGroup>,
    val failures: List<ModelFailure> = emptyList(),
)

@Serializable
data class ProviderModel(
    val id: String,
    val name: String? = null,
    val contextWindow: Long? = null,
    val maxTokens: Long? = null,
)

@Serializable
data class ProviderConfig(
    val baseURL: String? = null,
    val api: String? = null,
    val displayName: String? = null,
    val thinking: String? = null,
    val reasoningEffort: String? = null,
    val models: List<ProviderModel> = emptyList(),
    val modelsInherited: Boolean = false,
)

@Serializable
data class CredentialState(
    val ref: String,
    val configured: Boolean,
    val source: String? = null,
    val writable: Boolean,
)

@Serializable
data class ProviderView(
    val id: String,
    val displayName: String,
    val active: Boolean,
    val declared: Boolean? = null,
    val defaultBaseURL: String? = null,
    val error: String? = null,
    val configurable: Boolean,
    val configured: Boolean,
    val removable: Boolean,
    val credential: CredentialState,
    val config: ProviderConfig,
)

@Serializable
data class ProviderSettings(
    val writable: Boolean,
    val revisionByNamespace: Map<String, Int>,
    val customProvider: CustomProviderCapability = CustomProviderCapability(),
    val providers: List<ProviderView>,
)

@Serializable
data class CustomProviderCapability(
    val available: Boolean = false,
    val protocols: List<String> = emptyList(),
    val revision: Int? = null,
)

@Serializable
data class ProviderPatch(
    val displayName: String? = null,
    val baseURL: String? = null,
    val api: String? = null,
    val apiKey: String? = null,
    val thinking: String? = null,
    val reasoningEffort: String? = null,
    val models: List<ProviderModel>? = null,
    val expectedRevision: Int? = null,
)

@Serializable
data class CustomProviderCreate(
    val id: String,
    val displayName: String? = null,
    val baseURL: String,
    val api: String,
    val apiKey: String? = null,
    val models: List<ProviderModel>,
    val expectedRevision: Int? = null,
)

@Serializable
data class SessionEvent(
    val type: String,
    val seq: Int,
    val time: Long,
    val data: JsonElement = JsonNull,
    val sourceEventSeqs: List<Int> = emptyList(),
    val surfaceOp: JsonElement? = null,
)

@Serializable(with = HistoryEntrySerializer::class)
data class HistoryEntry(val event: SessionEvent, val view: JsonElement? = null)

object HistoryEntrySerializer : KSerializer<HistoryEntry> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("HistoryEntry")

    override fun deserialize(decoder: Decoder): HistoryEntry {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("HistoryEntrySerializer requires JSON decoding")
        val element = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("HistoryEntry must be a JSON object")
        val nested = element["event"] as? JsonObject
        val eventObj = nested ?: element
        val event = SessionEvent(
            type = (eventObj["type"] as? JsonPrimitive)?.contentOrNull.orEmpty(),
            seq = (eventObj["seq"] as? JsonPrimitive)?.intOrNull ?: 0,
            time = (eventObj["time"] as? JsonPrimitive)?.longOrNull ?: 0L,
            data = eventObj["data"] ?: JsonNull,
            sourceEventSeqs = (eventObj["sourceEventSeqs"] as? JsonArray)
                .orEmpty()
                .mapNotNull { (it as? JsonPrimitive)?.intOrNull },
            surfaceOp = eventObj["surfaceOp"],
        )
        val view = element["view"]?.takeUnless { it is JsonNull }
        return HistoryEntry(event = event, view = view)
    }

    override fun serialize(encoder: Encoder, value: HistoryEntry) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("HistoryEntrySerializer requires JSON encoding")
        val eventElement = jsonEncoder.json.encodeToJsonElement(SessionEvent.serializer(), value.event)
        val obj = buildJsonObject {
            put("event", eventElement)
            value.view?.let { put("view", it) }
        }
        jsonEncoder.encodeJsonElement(obj)
    }
}

@Serializable
data class ChatHistory(
    val events: List<HistoryEntry>,
    val hasMore: Boolean = false,
    val cursor: Int? = null,
    val projections: JsonElement? = null,
)

@Serializable
data class WorkspaceEvent(val id: String, val type: String, val time: Long, val data: JsonElement)

data class FileContent(val bytes: ByteArray, val etag: String, val contentType: String?)

data class StoredConnection(val endpoint: String, val token: String)

class DshApiException(
    val status: Int,
    val code: String,
    override val message: String,
) : RuntimeException(message)
