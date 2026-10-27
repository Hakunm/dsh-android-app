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
    fun foldsStableWebSocketDeltasIntoAReadableLiveAssistantMessage() {
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
    }

    @Test
    fun readsTodoAndPermissionControlsFromSessionProjections() {
        val history = ChatHistory(
            events = emptyList(),
            hasMore = false,
            projections = json.parseToJsonElement(
                """{"values":{"todos":[{"content":"Inspect API","status":"completed"},{"content":"Build UI","status":"in_progress"}],"permissions":{"currentValue":"workspace-write","options":[{"value":"read-only","name":"Read Only"},{"value":"workspace-write","name":"Workspace Write","description":"Write inside the workspace"},{"value":"danger-full-access","name":"Full access"}]}}}""",
            ),
        )

        assertEquals(listOf("Inspect API", "Build UI"), history.todoItems().map(TodoItem::content))
        assertEquals(listOf("completed", "in_progress"), history.todoItems().map(TodoItem::status))
        val permissions = history.permissionSelect()!!
        assertEquals("workspace-write", permissions.currentValue)
        assertEquals(listOf("read-only", "workspace-write", "danger-full-access"), permissions.options.map(PermissionOption::value))
        assertEquals("Write inside the workspace", permissions.options[1].description)
    }

    @Test
    fun decodesWorkspace11HistoryAndFillsPermissionChoicesFromTheStockCatalog() {
        val history = json.decodeFromString(
            ChatHistory.serializer(),
            """
            {"events":[
              {"type":"user/message","seq":2,"time":1761500000000,"data":{"content":[{"type":"text","text":"Hello"}],"source":{"kind":"user"}},"surfaceOp":"append","sourceEventSeqs":{"opaque":true}},
              {"type":"event","event":{"type":"assistant/message","seq":3,"time":1761500001000,"data":{"turn":1,"step":1,"message":{"content":[]},"stream":[{"type":"reasoning-chunks","texts":["Think"]},{"type":"text-chunks","texts":["Hi"," there"]}]}}},
              {"event":{"type":"tool/call","seq":4,"time":4,"data":{"name":"bash","arguments":{"command":"ls"}}},"view":{"view":{"description":"list files"}}}
            ],"hasMore":false,"projections":{"asOfSeq":4,"values":{"title":{"title":"Hello title","source":{"kind":"fallback"}},"permissions":{"currentValue":"read-only"},"todos":null}}}
            """.trimIndent(),
        )

        assertEquals("user/message", history.events[0].event.type)
        assertEquals(emptyList<Int>(), history.events[0].event.sourceEventSeqs)
        assertEquals("append", (history.events[0].event.surfaceOp as? kotlinx.serialization.json.JsonPrimitive)?.content)
        val items = history.displayItems()
        assertEquals(
            listOf(ChatItemKind.USER, ChatItemKind.REASONING, ChatItemKind.ASSISTANT, ChatItemKind.TOOL),
            items.map { it.kind },
        )
        assertEquals("Hello", items[0].body)
        assertEquals("Think", items[1].body)
        assertEquals("Hi there", items[2].body)
        assertEquals("list files", items[3].body)
        assertEquals("Hello title", history.sessionTitle())
        val permissions = history.permissionSelect()!!
        assertEquals("read-only", permissions.currentValue)
        assertEquals(
            listOf("read-only", "workspace-write", "danger-full-access"),
            permissions.options.map(PermissionOption::value),
        )
    }

    @Test
    fun foldsAssistantStreamFramesUsingTheTurnFromTheStartFrame() {
        val start = WorkspaceEvent(
            "1",
            "agent/assistant-stream",
            1,
            json.parseToJsonElement(
                """{"sessionId":"session-1","frame":{"type":"start","attemptId":"attempt-1","turn":4,"step":2}}""",
            ),
        ).assistantStreamStart()!!
        val turns = mapOf(streamTurnKey(start.sessionId, start.attemptId) to (start.turn to start.step))
        val delta = WorkspaceEvent(
            "2",
            "agent/assistant-stream",
            2,
            json.parseToJsonElement(
                """{"sessionId":"session-1","frame":{"type":"chunk","attemptId":"attempt-1","index":0,"chunk":{"type":"reasoning-delta","text":"token"}}}""",
            ),
        ).chatDelta(turns)!!

        assertEquals("session-1", delta.sessionId)
        assertEquals(4, delta.turn)
        assertEquals(2, delta.step)
        assertEquals(0, delta.index)
        assertEquals(ChatItemKind.REASONING, delta.kind)
        assertEquals("token", delta.text)
    }

    private fun entry(type: String, seq: Int, data: String, view: String? = null) = HistoryEntry(
        event = SessionEvent(type, seq, seq.toLong(), json.parseToJsonElement(data)),
        view = view?.let(json::parseToJsonElement),
    )

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
