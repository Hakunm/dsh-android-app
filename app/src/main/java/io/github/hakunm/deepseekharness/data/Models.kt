package io.github.hakunm.deepseekharness.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
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
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

@Serializable
data class HealthView(val ok: Boolean, val version: String, val pluginVersion: String? = null)

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
    val title: String? = null,
    val workspaceId: String? = null,
    val workspaceTitle: String? = null,
    val agentPreset: String? = null,
    val parentSessionId: String? = null,
    val origin: String? = null,
    val pendingInteraction: String? = null,
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

data class PermissionOption(val value: String, val name: String, val description: String? = null)

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
    val trust: String,
    val isDefault: Boolean,
    val available: Boolean,
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
    val data: JsonElement,
    val sourceEventSeqs: List<Int> = emptyList(),
    val surfaceOp: JsonElement? = null,
)

/** v1 history rows are `{ event, view? }`. dsh-workspace 1.1.0 forwards bare SessionWireEvents. */
@Serializable(with = HistoryEntrySerializer::class)
data class HistoryEntry(val event: SessionEvent, val view: JsonElement? = null)

@Serializable
data class ChatHistory(
    val events: List<HistoryEntry>,
    val hasMore: Boolean,
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

/**
 * Decodes a DSH history row in either published shape:
 * `{ event, view? }`, a page record `{ type: "event", event }`, or a bare SessionWireEvent.
 */
internal object HistoryEntrySerializer : KSerializer<HistoryEntry> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("HistoryEntry")

    override fun serialize(encoder: Encoder, value: HistoryEntry) {
        val jsonEncoder = encoder as? JsonEncoder ?: error("HistoryEntry requires JSON")
        jsonEncoder.encodeJsonElement(buildJsonObject {
            put("event", value.event.toJson())
            value.view?.let { put("view", it) }
        })
    }

    override fun deserialize(decoder: Decoder): HistoryEntry {
        val jsonDecoder = decoder as? JsonDecoder ?: error("HistoryEntry requires JSON")
        val obj = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: error("History entry must be a JSON object")
        val nested = obj["event"] as? JsonObject
        val eventElement = if (nested != null && nested.looksLikeSessionEvent()) nested else obj
        return HistoryEntry(eventElement.toSessionEvent(), obj["view"])
    }
}

private fun SessionEvent.toJson(): JsonObject = buildJsonObject {
    put("type", type)
    put("seq", seq)
    put("time", time)
    put("data", data)
    if (sourceEventSeqs.isNotEmpty()) {
        put("sourceEventSeqs", JsonArray(sourceEventSeqs.map { JsonPrimitive(it) }))
    }
    surfaceOp?.let { put("surfaceOp", it) }
}

private fun JsonObject.toSessionEvent(): SessionEvent = SessionEvent(
    type = (this["type"] as? JsonPrimitive)?.takeIf { it.isString }?.content
        ?: error("Session event is missing type"),
    seq = (this["seq"] as? JsonPrimitive)?.intOrNull ?: error("Session event is missing seq"),
    time = (this["time"] as? JsonPrimitive)?.longOrNull ?: error("Session event is missing time"),
    data = this["data"] ?: JsonNull,
    sourceEventSeqs = (this["sourceEventSeqs"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonPrimitive)?.intOrNull },
    surfaceOp = this["surfaceOp"],
)

private fun JsonObject.looksLikeSessionEvent(): Boolean {
    val type = this["type"] as? JsonPrimitive ?: return false
    if (!type.isString || type.content.isEmpty()) return false
    if ((this["seq"] as? JsonPrimitive)?.intOrNull == null) return false
    if ((this["time"] as? JsonPrimitive)?.longOrNull == null) return false
    return this["data"] != null
}
