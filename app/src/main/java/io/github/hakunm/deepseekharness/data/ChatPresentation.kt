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

fun WorkspaceEvent.assistantStreamStart(): Pair<String, Int>? {
    if (type != "chat.agent.assistant-stream" && type != "agent/assistant-stream") return null
    val value = data as? JsonObject ?: return null
    if (value.stringValue("phase") != "start") return null
    val sessionId = value.stringValue("sessionId") ?: return null
    val turn = value.intValue("turn") ?: return null
    return sessionId to turn
}

fun WorkspaceEvent.chatDelta(streamTurns: Map<String, Int> = emptyMap()): ChatDelta? {
    val value = data as? JsonObject ?: return null
    if (type == "chat.message.delta") {
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
    if (type == "chat.agent.assistant-stream" || type == "agent/assistant-stream") {
        if (value.stringValue("phase") != "chunk") return null
        val sessionId = value.stringValue("sessionId") ?: return null
        val chunk = value.objectValue("chunk") ?: return null
        val kind = when (chunk.stringValue("type")) {
            "text-delta" -> ChatItemKind.ASSISTANT
            "reasoning-delta" -> ChatItemKind.REASONING
            else -> return null
        }
        val text = chunk.stringValue("delta") ?: chunk.stringValue("text") ?: return null
        val turn = value.intValue("turn") ?: streamTurns[sessionId] ?: 0
        val step = value.intValue("step") ?: 0
        val index = chunk.intValue("index") ?: value.intValue("frameIndex") ?: 0
        return ChatDelta(sessionId, turn, step, index, kind, text)
    }
    return null
}

fun ChatHistory.displayItems(): List<ChatDisplayItem> = events.flatMap(::displayItems)

fun ChatHistory.sessionTitle(): String? {
    val values = (projections as? JsonObject)?.objectValue("values") ?: return null
    return when (val raw = values["title"]) {
        is JsonPrimitive -> raw.contentOrNull?.trim()?.takeIf(String::isNotEmpty)
        is JsonObject -> raw.stringValue("title")?.trim()?.takeIf(String::isNotEmpty)
        else -> null
    }
}

fun ChatHistory.todoItems(): List<TodoItem> {
    val values = (projections as? JsonObject)?.objectValue("values") ?: return emptyList()
    return (values["todos"] as? JsonArray).orEmpty().mapNotNull { raw ->
        val item = raw as? JsonObject ?: return@mapNotNull null
        val content = item.stringValue("content")?.takeIf(String::isNotBlank) ?: return@mapNotNull null
        val status = item.stringValue("status") ?: return@mapNotNull null
        TodoItem(content, status)
    }
}

fun ChatHistory.permissionSelect(catalog: PermissionCatalog? = null): PermissionSelect? {
    val values = (projections as? JsonObject)?.objectValue("values")
    val rawElement = values?.get("permissions")
    if (rawElement is JsonObject) {
        val currentValue = rawElement.stringValue("currentValue")
            ?: rawElement.stringValue("preset")
            ?: rawElement.stringValue("value")
            ?: catalog?.defaultPreset
            ?: return null
        val options = (rawElement["options"] as? JsonArray).orEmpty().mapNotNull { entry ->
            val option = entry as? JsonObject ?: return@mapNotNull null
            val value = option.stringValue("value") ?: return@mapNotNull null
            val name = option.stringValue("name") ?: return@mapNotNull null
            PermissionOption(value, name, option.stringValue("description"))
        }.ifEmpty { catalog?.options.orEmpty() }
        return PermissionSelect(options, currentValue)
    }
    if (rawElement is JsonPrimitive) {
        val current = rawElement.contentOrNull?.takeIf(String::isNotBlank)
        if (current != null && catalog != null && catalog.options.isNotEmpty()) {
            return PermissionSelect(catalog.options, current)
        }
    }
    if (catalog != null && catalog.options.isNotEmpty()) {
        return PermissionSelect(catalog.options, catalog.defaultPreset)
    }
    return null
}

fun List<ChatSession>.preservingTitles(previous: List<ChatSession>): List<ChatSession> {
    val priorById = previous.associateBy(ChatSession::id)
    return map { session ->
        if (!session.title.isNullOrBlank()) session
        else {
            val savedTitle = priorById[session.id]?.title
            if (!savedTitle.isNullOrBlank()) session.copy(title = savedTitle) else session
        }
    }
}

fun List<ChatSession>.withSessionHistory(sessionId: String, history: ChatHistory): List<ChatSession> {
    val title = history.sessionTitle() ?: return this
    return map { session ->
        if (session.id == sessionId && session.title.isNullOrBlank()) session.copy(title = title) else session
    }
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
    if (kind == null || kind == "user") {
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
    val content = message["content"]
    val fromContent = when (content) {
        is JsonArray -> parseContentBlocks(event.seq, content)
        is JsonPrimitive -> content.contentOrNull?.takeIf(String::isNotBlank)?.let {
            listOf(ChatDisplayItem("${event.seq}-assistant", event.seq, ChatItemKind.ASSISTANT, body = it))
        }.orEmpty()
        else -> emptyList()
    }
    if (fromContent.isNotEmpty()) return fromContent

    val stream = outer["stream"] ?: message["stream"]
    return parseStreamBlocks(event.seq, stream)
}

private fun parseContentBlocks(seq: Int, blocks: JsonArray): List<ChatDisplayItem> {
    val result = mutableListOf<ChatDisplayItem>()
    val prose = mutableListOf<String>()
    fun flushProse() {
        if (prose.isEmpty()) return
        result += ChatDisplayItem(
            id = "$seq-assistant-${result.size}",
            seq = seq,
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
                        id = "$seq-reasoning-${result.size}",
                        seq = seq,
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

private fun parseStreamBlocks(seq: Int, stream: JsonElement?): List<ChatDisplayItem> {
    val items = stream as? JsonArray ?: return emptyList()
    val result = mutableListOf<ChatDisplayItem>()
    val currentText = StringBuilder()
    var currentKind: ChatItemKind? = null

    fun flush() {
        val kind = currentKind ?: return
        val text = currentText.toString()
        if (text.isNotBlank()) {
            val tag = if (kind == ChatItemKind.REASONING) "reasoning" else "assistant"
            result += ChatDisplayItem("$seq-$tag-${result.size}", seq, kind, body = text)
        }
        currentText.clear()
        currentKind = null
    }

    fun appendChunk(kind: ChatItemKind, piece: String) {
        if (piece.isEmpty()) return
        if (currentKind != null && currentKind != kind) flush()
        currentKind = kind
        currentText.append(piece)
    }

    for (entry in items) {
        val obj = entry as? JsonObject ?: continue
        when (obj.stringValue("type")) {
            "text-chunks" -> {
                val joined = (obj["chunks"] as? JsonArray).orEmpty()
                    .mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                    .joinToString("")
                appendChunk(ChatItemKind.ASSISTANT, joined)
            }
            "reasoning-chunks" -> {
                val joined = (obj["chunks"] as? JsonArray).orEmpty()
                    .mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                    .joinToString("")
                appendChunk(ChatItemKind.REASONING, joined)
            }
            "chunk" -> {
                val chunk = obj.objectValue("chunk") ?: continue
                when (chunk.stringValue("type")) {
                    "text-delta" -> appendChunk(ChatItemKind.ASSISTANT, chunk.stringValue("delta") ?: chunk.stringValue("text").orEmpty())
                    "reasoning-delta" -> appendChunk(ChatItemKind.REASONING, chunk.stringValue("delta") ?: chunk.stringValue("text").orEmpty())
                }
            }
            "text-delta" -> appendChunk(ChatItemKind.ASSISTANT, obj.stringValue("delta") ?: obj.stringValue("text").orEmpty())
            "reasoning-delta" -> appendChunk(ChatItemKind.REASONING, obj.stringValue("delta") ?: obj.stringValue("text").orEmpty())
        }
    }
    flush()
    return result
}

private fun toolItem(entry: HistoryEntry): ChatDisplayItem {
    val event = entry.event
    val data = event.data as? JsonObject
    val name = data?.stringValue("name").orEmpty()
    val view = (entry.view as? JsonObject)?.objectValue("view")
    val viewTitle = view?.stringValue("title")
    val description = view?.stringValue("description")
    val arguments = data?.get("arguments")
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
            "image" -> {
                val name = value.stringValue("name")?.takeIf(String::isNotBlank)
                if (name != null) "[Image: $name]" else "[Image]"
            }
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

private fun argumentsSummary(raw: JsonElement?): String? {
    val parsed: JsonObject = when (raw) {
        is JsonObject -> raw
        is JsonPrimitive -> {
            val text = raw.contentOrNull?.takeIf(String::isNotBlank) ?: return null
            runCatching { json.parseToJsonElement(text) as? JsonObject }.getOrNull() ?: return null
        }
        else -> return null
    }
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
