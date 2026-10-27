package io.github.hakunm.deepseekharness.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

enum class ChatItemKind { USER, ASSISTANT, REASONING, CONTEXT, TOOL }

data class ChatDisplayItem(
    val id: String,
    val seq: Int,
    val kind: ChatItemKind,
    val title: String? = null,
    val body: String = "",
    val streaming: Boolean = false,
)

data class ChatDelta(
    val sessionId: String,
    val turn: Int,
    val step: Int,
    val index: Int,
    val kind: ChatItemKind,
    val text: String,
)

data class LiveChatBlock(val index: Int, val kind: ChatItemKind, val text: String)

data class LiveChatState(
    val sessionId: String,
    val turn: Int,
    val step: Int,
    val blocks: List<LiveChatBlock> = emptyList(),
    val revision: Long = 0,
) {
    fun append(delta: ChatDelta): LiveChatState {
        val base = if (sessionId == delta.sessionId && turn == delta.turn && step == delta.step) this else {
            LiveChatState(delta.sessionId, delta.turn, delta.step)
        }
        val existing = base.blocks.indexOfFirst { it.index == delta.index }
        val next = if (existing < 0) {
            base.blocks + LiveChatBlock(delta.index, delta.kind, delta.text)
        } else {
            base.blocks.toMutableList().also { blocks ->
                val previous = blocks[existing]
                blocks[existing] = LiveChatBlock(
                    delta.index,
                    delta.kind,
                    if (previous.kind == delta.kind) previous.text + delta.text else delta.text,
                )
            }
        }
        return base.copy(blocks = next.sortedBy(LiveChatBlock::index), revision = base.revision + 1)
    }

    fun displayItems(): List<ChatDisplayItem> = blocks.filter { it.text.isNotEmpty() }.map { block ->
        ChatDisplayItem(
            id = "live-$turn-$step-${block.index}-${block.kind}",
            seq = Int.MAX_VALUE,
            kind = block.kind,
            body = block.text,
            streaming = true,
        )
    }
}

data class AssistantStreamStart(val sessionId: String, val attemptId: String, val turn: Int, val step: Int)

fun WorkspaceEvent.chatDelta(streamTurns: Map<String, Pair<Int, Int>> = emptyMap()): ChatDelta? {
    val value = data as? JsonObject ?: return null
    if (type == "chat.message.delta") return legacyDelta(value)
    return assistantStreamDelta(value, streamTurns)
}

fun WorkspaceEvent.assistantStreamStart(): AssistantStreamStart? {
    val value = data as? JsonObject ?: return null
    val frame = value.objectValue("frame") ?: value
    if (frame.stringValue("type") != "start") return null
    val sessionId = value.stringValue("sessionId") ?: frame.stringValue("sessionId") ?: return null
    val attemptId = frame.stringValue("attemptId") ?: return null
    val turn = frame.intValue("turn") ?: value.intValue("turn") ?: return null
    val step = frame.intValue("step") ?: value.intValue("step") ?: return null
    return AssistantStreamStart(sessionId, attemptId, turn, step)
}

private fun legacyDelta(value: JsonObject): ChatDelta? {
    val kind = when (value.stringValue("kind")) {
        "text" -> ChatItemKind.ASSISTANT
        "reasoning" -> ChatItemKind.REASONING
        else -> return null
    }
    return ChatDelta(
        sessionId = value.stringValue("sessionId") ?: return null,
        turn = value.intValue("turn") ?: return null,
        step = value.intValue("step") ?: return null,
        index = value.intValue("index") ?: return null,
        kind = kind,
        text = value.stringValue("text") ?: return null,
    )
}

/** DSH 0.2 publishes token frames on `agent/assistant-stream` instead of `assistant/chunk`. */
private fun WorkspaceEvent.assistantStreamDelta(value: JsonObject, streamTurns: Map<String, Pair<Int, Int>>): ChatDelta? {
    val frame = value.objectValue("frame") ?: value
    if (frame.stringValue("type") != "chunk") return null
    val chunk = frame.objectValue("chunk") ?: return null
    val kind = when (chunk.stringValue("type")) {
        "text-delta" -> ChatItemKind.ASSISTANT
        "reasoning-delta" -> ChatItemKind.REASONING
        else -> return null
    }
    val sessionId = value.stringValue("sessionId") ?: frame.stringValue("sessionId") ?: return null
    val attemptId = frame.stringValue("attemptId")
    val located = attemptId?.let { streamTurns[streamTurnKey(sessionId, it)] }
    return ChatDelta(
        sessionId = sessionId,
        turn = value.intValue("turn") ?: frame.intValue("turn") ?: located?.first ?: return null,
        step = value.intValue("step") ?: frame.intValue("step") ?: located?.second ?: return null,
        index = frame.intValue("index") ?: chunk.intValue("index") ?: return null,
        kind = kind,
        text = chunk.stringValue("text") ?: return null,
    )
}

internal fun streamTurnKey(sessionId: String, attemptId: String): String = "$sessionId\u0000$attemptId"

fun ChatHistory.displayItems(): List<ChatDisplayItem> = events.flatMap(::displayItems)

fun ChatHistory.todoItems(): List<TodoItem> {
    val values = (projections as? JsonObject)?.objectValue("values") ?: return emptyList()
    return (values["todos"] as? JsonArray).orEmpty().mapNotNull { raw ->
        val item = raw as? JsonObject ?: return@mapNotNull null
        val content = item.stringValue("content")?.takeIf(String::isNotBlank) ?: return@mapNotNull null
        val status = item.stringValue("status") ?: return@mapNotNull null
        TodoItem(content, status)
    }
}

fun ChatHistory.permissionSelect(): PermissionSelect? {
    val values = (projections as? JsonObject)?.objectValue("values") ?: return null
    val raw = values.objectValue("permissions") ?: return null
    val currentValue = raw.stringValue("currentValue") ?: return null
    val provided = (raw["options"] as? JsonArray).orEmpty().mapNotNull { entry ->
        val option = entry as? JsonObject ?: return@mapNotNull null
        val value = option.stringValue("value") ?: return@mapNotNull null
        val name = option.stringValue("name") ?: return@mapNotNull null
        PermissionOption(value, name, option.stringValue("description"))
    }
    if (provided.isNotEmpty()) return PermissionSelect(provided, currentValue)
    // dsh-workspace 1.1.0 forwards only currentValue. The selectable catalog is the DSH stock preset table.
    val defaults = listOf(
        PermissionOption("workspace-write", "workspace-write"),
        PermissionOption("danger-full-access", "danger-full-access"),
    )
    val options = if (currentValue == "custom" || defaults.any { it.value == currentValue }) {
        defaults
    } else {
        listOf(PermissionOption(currentValue, currentValue)) + defaults
    }
    return PermissionSelect(options, currentValue)
}

fun ChatHistory.sessionTitle(): String? {
    val values = (projections as? JsonObject)?.objectValue("values") ?: return null
    val title = values["title"] ?: return null
    val text = when (title) {
        is JsonPrimitive -> title.contentOrNull
        is JsonObject -> title.stringValue("title")
        else -> null
    }
    return text?.takeIf(String::isNotBlank)
}

private fun displayItems(entry: HistoryEntry): List<ChatDisplayItem> = when (entry.event.type) {
    "user/message" -> userItems(entry.event)
    "assistant/message" -> assistantItems(entry.event)
    "tool/call" -> listOf(toolItem(entry))
    else -> emptyList()
}

private fun userItems(event: SessionEvent): List<ChatDisplayItem> {
    val data = event.data as? JsonObject ?: return emptyList()
    val source = data.objectValue("source")
    val kind = source?.stringValue("kind")
    val body = contentText(data["content"])
    if (kind == "user") {
        return body.takeIf(String::isNotBlank)?.let {
            listOf(ChatDisplayItem("${event.seq}-user", event.seq, ChatItemKind.USER, body = it))
        }.orEmpty()
    }
    return listOf(ChatDisplayItem(
        id = "${event.seq}-context",
        seq = event.seq,
        kind = ChatItemKind.CONTEXT,
        title = contextLabel(source),
        body = body,
    ))
}

private fun assistantItems(event: SessionEvent): List<ChatDisplayItem> {
    val outer = event.data as? JsonObject ?: return emptyList()
    val message = outer.objectValue("message") ?: outer
    val fromContent = contentItems(event, message["content"])
    if (fromContent.isNotEmpty()) return fromContent
    return streamItems(event, outer["stream"] as? JsonArray)
}

private fun contentItems(event: SessionEvent, content: JsonElement?): List<ChatDisplayItem> {
    val blocks = content as? JsonArray ?: return contentText(content).takeIf(String::isNotBlank)?.let {
        listOf(ChatDisplayItem("${event.seq}-assistant", event.seq, ChatItemKind.ASSISTANT, body = it))
    }.orEmpty()
    val result = mutableListOf<ChatDisplayItem>()
    val prose = mutableListOf<String>()
    fun flushProse() {
        if (prose.isEmpty()) return
        result += ChatDisplayItem(
            id = "${event.seq}-assistant-${result.size}",
            seq = event.seq,
            kind = ChatItemKind.ASSISTANT,
            body = prose.joinToString("\n\n"),
        )
        prose.clear()
    }
    for (block in blocks) {
        val value = block as? JsonObject ?: continue
        when (value.stringValue("type")) {
            "text" -> value.stringValue("text")?.takeIf(String::isNotBlank)?.let(prose::add)
            "reasoning" -> {
                flushProse()
                value.stringValue("text")?.takeIf(String::isNotBlank)?.let { reasoning ->
                    result += ChatDisplayItem(
                        id = "${event.seq}-reasoning-${result.size}",
                        seq = event.seq,
                        kind = ChatItemKind.REASONING,
                        body = reasoning,
                    )
                }
            }
        }
    }
    flushProse()
    return result
}

private fun streamItems(event: SessionEvent, stream: JsonArray?): List<ChatDisplayItem> {
    if (stream == null) return emptyList()
    val pieces = mutableListOf<Pair<ChatItemKind, String>>()
    for (record in stream) {
        val value = record as? JsonObject ?: continue
        when (value.stringValue("type")) {
            "text-chunks" -> joinedTexts(value)?.let { pieces += ChatItemKind.ASSISTANT to it }
            "reasoning-chunks" -> joinedTexts(value)?.let { pieces += ChatItemKind.REASONING to it }
            "chunk" -> {
                val chunk = value.objectValue("chunk") ?: continue
                val kind = when (chunk.stringValue("type")) {
                    "text-delta" -> ChatItemKind.ASSISTANT
                    "reasoning-delta" -> ChatItemKind.REASONING
                    else -> null
                } ?: continue
                chunk.stringValue("text")?.takeIf(String::isNotBlank)?.let { pieces += kind to it }
            }
        }
    }
    val merged = mutableListOf<Pair<ChatItemKind, String>>()
    for ((kind, text) in pieces) {
        val last = merged.lastOrNull()
        if (last != null && last.first == kind) merged[merged.lastIndex] = kind to "${last.second}$text"
        else merged += kind to text
    }
    return merged.mapIndexed { index, (kind, text) ->
        ChatDisplayItem("${event.seq}-stream-$index", event.seq, kind, body = text)
    }
}

private fun joinedTexts(value: JsonObject): String? =
    (value["texts"] as? JsonArray).orEmpty()
        .mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        .joinToString("")
        .takeIf(String::isNotBlank)

private fun toolItem(entry: HistoryEntry): ChatDisplayItem {
    val event = entry.event
    val data = event.data as? JsonObject
    val name = data?.stringValue("name").orEmpty()
    val view = (entry.view as? JsonObject)?.objectValue("view")
    val viewTitle = view?.stringValue("title")
    val description = view?.stringValue("description")
    val arguments = when (val raw = data?.get("arguments")) {
        is JsonPrimitive -> raw.contentOrNull
        is JsonObject -> raw.toString()
        else -> null
    }
    return ChatDisplayItem(
        id = "${event.seq}-tool",
        seq = event.seq,
        kind = ChatItemKind.TOOL,
        title = toolTitle(name),
        body = description ?: argumentsSummary(arguments) ?: viewTitle ?: name,
    )
}

private fun contentText(content: JsonElement?): String = when (content) {
    is JsonPrimitive -> content.contentOrNull.orEmpty()
    is JsonArray -> content.mapNotNull { block ->
        val value = block as? JsonObject ?: return@mapNotNull null
        when (value.stringValue("type")) {
            "text", "reasoning" -> value.stringValue("text")
            else -> null
        }
    }.filter(String::isNotBlank).joinToString("\n\n")
    else -> ""
}

private fun contextLabel(source: JsonObject?): String? {
    source ?: return null
    return when (val kind = source.stringValue("kind")) {
        "agent-instructions" -> source.arrayLabels("changes", "path").ifEmpty { null }
        "session-reference" -> source.arrayLabels("references", "label").ifEmpty { null }
        "plugin" -> source.stringValue("plugin") ?: kind
        "skill-invocation" -> source.stringValue("name") ?: kind
        else -> kind
    }
}

private fun toolTitle(name: String): String = when {
    name == "bash" -> "Bash"
    name == "pwsh" -> "Pwsh"
    "skill" in name.lowercase() -> "Skill"
    name == "read" || name == "web_fetch" -> "Read"
    name == "grep" || name == "glob" || name == "web_search" -> "Search"
    name == "write" -> "Write"
    name == "edit" -> "Edit"
    name == "run_code" -> "Code"
    else -> "Tool call"
}

private fun argumentsSummary(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val parsed = runCatching { json.parseToJsonElement(raw) as? JsonObject }.getOrNull() ?: return null
    for (key in listOf("description", "path", "file_path", "query", "pattern", "url", "command", "name", "package")) {
        parsed.stringValue(key)?.takeIf(String::isNotBlank)?.let { return it.lineSequence().first() }
    }
    return null
}

private fun JsonObject.objectValue(key: String): JsonObject? = this[key] as? JsonObject

private fun JsonObject.stringValue(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.intValue(key: String): Int? = (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.arrayLabels(member: String, field: String): String {
    val values = (this[member] as? JsonArray).orEmpty().mapNotNull { entry ->
        (entry as? JsonObject)?.stringValue(field)?.takeIf(String::isNotBlank)
    }.distinct()
    return values.joinToString(", ")
}

private val json = Json { ignoreUnknownKeys = true }
