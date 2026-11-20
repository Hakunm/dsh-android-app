package io.github.hakunm.deepseekharness.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ChatPresentationTest {
    @Test
    fun keepsReadableConversationRowsAndDropsProtocolLifecycleEvents() {
        val history = ChatHistory(
            events = listOf(
                entry("turn/start", 1, """{"turn":1}"""),
                entry("user/message", 2, """{"content":[{"type":"text","text":"Build the feature"}],"source":{"kind":"user"}}"""),
                entry(
                    "user/message",
                    3,
                    """{"content":[{"type":"text","text":"Injected instructions"}],"source":{"kind":"agent-instructions","changes":[{"path":"AGENTS.md"}]}}""",
                ),
                entry(
                    "assistant/message",
                    4,
                    """{"turn":1,"step":1,"message":{"content":[{"type":"reasoning","text":"Inspect the repository"},{"type":"tool-call","id":"call-1","name":"bash","arguments":"{}"},{"type":"text","text":"The implementation is ready."}]}}""",
                ),
                entry(
                    "tool/call",
                    5,
                    """{"turn":1,"step":1,"callId":"call-1","name":"bash","arguments":"{\"command\":\"pnpm test\",\"description\":\"Run tests\"}"}""",
                    """{"for":"call","view":{"card":"terminal","title":"pnpm test","description":"Run tests"}}""",
                ),
                entry(
                    "tool/call",
                    6,
                    """{"turn":1,"step":1,"callId":"call-2","name":"mystery","arguments":"{raw protocol payload"}""",
                    """{"for":"call","view":{"title":"Safe host title"}}""",
                ),
                entry("step/end", 7, """{"turn":1,"step":1}"""),
                entry("turn/end", 8, """{"turn":1,"reason":{"kind":"completed"}}"""),
            ),
            hasMore = false,
        )

        val items = history.displayItems()

        assertEquals(
            listOf(
                ChatItemKind.USER,
                ChatItemKind.CONTEXT,
                ChatItemKind.REASONING,
                ChatItemKind.ASSISTANT,
                ChatItemKind.TOOL,
                ChatItemKind.TOOL,
            ),
            items.map { it.kind },
        )
        assertEquals("AGENTS.md", items[1].title)
        assertEquals("The implementation is ready.", items[3].body)
        assertEquals("Bash", items[4].title)
        assertEquals("Run tests", items[4].body)
        assertEquals("Safe host title", items[5].body)
        assertFalse(items[5].body.contains("raw protocol payload"))
        assertFalse(items.any { it.body.contains("turn/end") || it.body.contains("sourceEventSeqs") })
    }

    @Test
    fun parsesDsh02xStreamAssistantChunksObjectToolArgumentsAndUserImages() {
        val raw = """
            {
              "events": [
                {"type":"user/message","seq":1,"time":1,"data":{"content":[{"type":"text","text":"Look at this"},{"type":"image","mediaType":"image/png","data":"AA==","name":"mock.png"}],"source":{"kind":"user"}}},
                {"type":"assistant/message","seq":2,"time":2,"data":{"turn":1,"step":1,"stream":[{"type":"reasoning-chunks","chunks":["Plan ","step 1"]},{"type":"text-chunks","chunks":["Hello ","world"]}]}},
                {"type":"tool/call","seq":3,"time":3,"data":{"name":"read","arguments":{"file_path":"src/App.kt"}}}
              ],
              "hasMore": false,
              "projections": {"values":{"title":{"title":"Liquid Glass Redesign"}}}
            }
        """.trimIndent()

        val history = json.decodeFromString(ChatHistory.serializer(), raw)
        val items = history.displayItems()

        assertEquals("Liquid Glass Redesign", history.sessionTitle())
        assertEquals(
            listOf(ChatItemKind.USER, ChatItemKind.REASONING, ChatItemKind.ASSISTANT, ChatItemKind.TOOL),
            items.map { it.kind },
        )
        assertEquals("Look at this\n\n[Image: mock.png]", items[0].body)
        assertEquals("Plan step 1", items[1].body)
        assertEquals("Hello world", items[2].body)
        assertEquals("Read", items[3].title)
        assertEquals("src/App.kt", items[3].body)
    }

    @Test
    fun foldsStableWebSocketDeltasAndAssistantStreamFrames() {
        val first = WorkspaceEvent(
            "1",
            "chat.message.delta",
            1,
            json.parseToJsonElement(
                """{"sessionId":"session-1","turn":2,"step":1,"index":0,"kind":"text","text":"实时"}""",
            ),
        ).chatDelta()!!
        val second = WorkspaceEvent(
            "2",
            "chat.message.delta",
            2,
            json.parseToJsonElement(
                """{"sessionId":"session-1","turn":2,"step":1,"index":0,"kind":"text","text":"输出"}""",
            ),
        ).chatDelta()!!

        val live = LiveChatState(first.sessionId, first.turn, first.step).append(first).append(second)

        assertEquals("实时输出", live.displayItems().single().body)
        assertEquals(ChatItemKind.ASSISTANT, live.displayItems().single().kind)
        assertEquals(2, live.revision)

        val streamStart = WorkspaceEvent(
            "3",
            "chat.agent.assistant-stream",
            3,
            json.parseToJsonElement("""{"sessionId":"session-2","phase":"start","turn":4}"""),
        ).assistantStreamStart()
        assertEquals("session-2" to 4, streamStart)

        val streamChunk = WorkspaceEvent(
            "4",
            "chat.agent.assistant-stream",
            4,
            json.parseToJsonElement("""{"sessionId":"session-2","phase":"chunk","chunk":{"type":"text-delta","delta":"Chunked"}}"""),
        ).chatDelta(mapOf("session-2" to 4))!!
        assertEquals(4, streamChunk.turn)
        assertEquals("Chunked", streamChunk.text)
    }

    @Test
    fun readsTodoPermissionFallbackAndPreservesSessionTitles() {
        val catalog = PermissionCatalog(
            options = listOf(
                PermissionOption("read-only", "Read Only"),
                PermissionOption("workspace-write", "Workspace Write", "Write inside the workspace"),
            ),
            defaultPreset = "workspace-write",
        )
        val history = ChatHistory(
            events = emptyList(),
            hasMore = false,
            projections = json.parseToJsonElement(
                """{"values":{"title":"Release v1.2.0","todos":[{"content":"Inspect API","status":"completed"},{"content":"Build UI","status":"in_progress"}],"permissions":"read-only"}}""",
            ),
        )

        assertEquals(listOf("Inspect API", "Build UI"), history.todoItems().map(TodoItem::content))
        assertEquals(listOf("completed", "in_progress"), history.todoItems().map(TodoItem::status))
        val permissions = history.permissionSelect(catalog)!!
        assertEquals("read-only", permissions.currentValue)
        assertEquals(2, permissions.options.size)

        val rawList = listOf(ChatSession("s1", "r1", "", 10L, running = false, blank = false))
        val titled = rawList.withSessionHistory("s1", history)
        assertEquals("Release v1.2.0", titled.single().title)
        val preserved = rawList.preservingTitles(titled)
        assertEquals("Release v1.2.0", preserved.single().title)
    }

    private fun entry(type: String, seq: Int, data: String, view: String? = null) = HistoryEntry(
        event = SessionEvent(type, seq, seq.toLong(), json.parseToJsonElement(data)),
        view = view?.let(json::parseToJsonElement),
    )

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
