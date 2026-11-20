package io.github.hakunm.deepseekharness.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DshClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun normalizesServerAddressToVersionedApi() {
        assertEquals("http://example.test:3090/api/v1", DshClient.normalizeEndpoint(" http://example.test:3090/ "))
        assertEquals("https://example.test/api/v1", DshClient.normalizeEndpoint("https://example.test/api/v1/"))
    }

    @Test
    fun healthAndPairingUsePublicUnauthenticatedRoutes() {
        server.enqueue(
            MockResponse()
                .setBody("""{"ok":true,"version":"v1","pluginVersion":"1.2.0","capabilities":["streaming","questions","pin"]}""")
                .setHeader("Content-Type", "application/json"),
        )
        server.enqueue(MockResponse().setResponseCode(201).setBody(PAIRING_RESPONSE).setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString())

        val health = client.health()
        assertEquals("v1", health.version)
        assertEquals("1.2.0", health.pluginVersion)
        assertEquals(listOf("streaming", "questions", "pin"), health.capabilities)
        assertEquals("secret-token", client.pair("ABCDEFGH", "Pixel").token)

        assertEquals("/api/v1/healthz", server.takeRequest().path)
        val pairing = server.takeRequest()
        assertEquals("/api/v1/pairings/exchange", pairing.path)
        assertTrue(pairing.body.readUtf8().contains("\"deviceName\":\"Pixel\""))
    }

    @Test
    fun authenticatedFileCallsEncodePathsAndSendOptimisticLockHeaders() {
        server.enqueue(MockResponse().setBody(ENTRIES_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setHeader("ETag", "\"etag-2\""))
        val client = DshClient(server.url("/").toString(), "device-token")

        val page = client.entries("root id", "目录/file name.txt")
        assertEquals("file name.txt", page.entries.single().name)
        assertEquals("\"etag-2\"", client.writeFile("root id", "目录/file name.txt", "hello".toByteArray(), "\"etag-1\""))

        val listing = server.takeRequest()
        assertEquals("Bearer device-token", listing.getHeader("Authorization"))
        assertTrue(listing.path.orEmpty().contains("/api/v1/roots/root%20id/entries"))
        assertTrue(listing.requestUrl?.queryParameter("path").orEmpty().contains("目录/file name.txt"))
        val write = server.takeRequest()
        assertEquals("PUT", write.method)
        assertEquals("\"etag-1\"", write.getHeader("If-Match"))
        assertEquals("hello", write.body.readUtf8())
    }

    @Test
    fun searchesFilesRecursivelyThroughAuthorizedRoot() {
        server.enqueue(
            MockResponse()
                .setBody("""{"query":"readme","path":"docs","entries":[{"name":"README.md","path":"docs/README.md","kind":"file","size":128,"modifiedAt":1,"writable":true}],"truncated":false}""")
                .setHeader("Content-Type", "application/json"),
        )
        val client = DshClient(server.url("/").toString(), "device-token")

        val result = client.searchFiles("root-1", "readme", path = "docs", limit = 20)
        assertEquals("readme", result.query)
        assertEquals("docs/README.md", result.entries.single().path)
        assertFalse(result.truncated)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/roots/root-1/search?q=readme&path=docs&limit=20", request.path)
    }

    @Test
    fun streamsUploadsAndDownloadsWithoutBufferingWholeFiles() {
        server.enqueue(MockResponse().setHeader("ETag", "\"stream-etag\""))
        server.enqueue(MockResponse().setBody("streamed response"))
        val client = DshClient(server.url("/").toString(), "device-token")
        val upload = "streamed request".toByteArray()

        assertEquals(
            "\"stream-etag\"",
            client.writeFile("root-1", "large.bin", upload.size.toLong(), { ByteArrayInputStream(upload) }),
        )
        val output = ByteArrayOutputStream()
        client.downloadFile("root-1", "large.bin", output)

        val write = server.takeRequest()
        assertEquals(upload.size.toLong(), write.getHeader("Content-Length")?.toLong())
        assertEquals("streamed request", write.body.readUtf8())
        assertEquals("streamed response", output.toString(Charsets.UTF_8.name()))
    }

    @Test
    fun requestsEarlierChatHistoryAndParsesBareWireEvents() {
        server.enqueue(
            MockResponse()
                .setBody("""{"events":[{"type":"user/message","seq":41,"time":1000,"data":{"content":[{"type":"text","text":"Hi"}],"source":{"kind":"user"}}}],"hasMore":false,"cursor":41}""")
                .setHeader("Content-Type", "application/json"),
        )
        val client = DshClient(server.url("/").toString(), "device-token")

        val history = client.history("session/1", beforeSeq = 42, maxMessages = 25)
        assertEquals(41, history.events.single().event.seq)
        assertEquals("user/message", history.events.single().event.type)
        assertEquals("Hi", history.displayItems().single().body)

        val request = server.takeRequest()
        assertEquals("/api/v1/chat/sessions/session%2F1/messages?beforeSeq=42&maxMessages=25", request.path)
    }

    @Test
    fun websocketHandshakeKeepsAnOkHttpCompatibleUrlScheme() {
        val httpClient = DshClient("http://example.test:3090")
        val httpsClient = DshClient("https://example.test")

        assertEquals("http://example.test:3090/api/v1/events", httpClient.eventsUrl().toString())
        assertEquals("https://example.test/api/v1/events", httpsClient.eventsUrl().toString())
    }

    @Test
    fun readsDhsWorkspaceAndAgentChoicesIncludingOmittedTrustField() {
        server.enqueue(MockResponse().setBody(WORKSPACES_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(CREATED_WORKSPACE_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(PRESETS_WITHOUT_TRUST_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(CREATED_SESSION_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(SELECTED_PRESET_RESPONSE).setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString(), "device-token")

        assertEquals("WebUI Workspace", client.chatWorkspaces().single().title)
        assertEquals("New Workspace", client.createChatWorkspace("root-1", "projects/new").title)
        val preset = client.agentPresets().single()
        assertEquals("Coding", preset.name)
        assertEquals("system", preset.trust)
        assertEquals("session-created", client.createSession("workspace-1", "standard").id)
        assertEquals("minimal", client.selectAgentPreset("session-created", "minimal"))

        assertEquals("/api/v1/chat/workspaces", server.takeRequest().path)
        val workspaceCreate = server.takeRequest()
        assertEquals("POST", workspaceCreate.method)
        assertTrue(workspaceCreate.body.readUtf8().contains("\"path\":\"projects/new\""))
        assertEquals("/api/v1/chat/agent-presets", server.takeRequest().path)
        val create = server.takeRequest()
        assertTrue(create.body.readUtf8().let { body ->
            body.contains("\"workspaceId\":\"workspace-1\"") &&
                body.contains("\"agentPreset\":\"standard\"") &&
                !body.contains("rootId") && !body.contains("\"path\"")
        })
        val selection = server.takeRequest()
        assertEquals("PUT", selection.method)
        assertEquals("/api/v1/chat/sessions/session-created/agent-preset", selection.path)
        assertTrue(selection.body.readUtf8().contains("\"agentPreset\":\"minimal\""))
    }

    @Test
    fun managesPinArchiveSearchQueueQuestionsAndImageAttachmentsInV120() {
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"s1","rootId":"r1","cwd":"","updatedAt":1,"running":false,"blank":false,"pinned":true,"archived":true,"title":"Pinned"}]}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("""{"items":[{"sessionId":"s1","snippet":"Liquid glass","session":{"id":"s1","rootId":"r1","cwd":"","updatedAt":1,"running":false,"blank":false,"title":"Pinned"}}],"hasMore":false,"mode":"fulltext"}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("""{"pinned":true}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("""{"pinned":false}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("""{"archived":false}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("""{"options":[{"value":"workspace-write","name":"Workspace Write"}],"defaultPreset":"workspace-write"}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("""{"cursor":5,"active":true,"running":true,"queue":[{"id":"q-1","placement":"queue","content":[{"type":"text","text":"Follow up"}]}],"pendingInput":[]}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("""{"accepted":true}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"ask-1","sessionId":"s1","state":"open","timed":false,"questions":[{"id":"q1","question":"Pick theme","options":[{"label":"Light"},{"label":"Dark"}],"multiSelect":false}],"requestedAt":10}]}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(202).setBody("""{"accepted":true}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(202).setBody("""{"message":{"accepted":true}}""").setHeader("Content-Type", "application/json"))

        val client = DshClient(server.url("/").toString(), "device-token")

        val sessions = client.sessions(includeArchived = true)
        assertTrue(sessions.single().pinned)
        assertTrue(sessions.single().archived)
        val search = client.searchSessions("glass", includeArchived = true)
        assertEquals("fulltext", search.mode)
        assertEquals("Liquid glass", search.items.single().snippet)
        client.pinSession("s1")
        client.unpinSession("s1")
        client.unarchiveSession("s1")
        assertEquals("workspace-write", client.permissionCatalog().defaultPreset)
        val state = client.sessionState("s1")
        assertEquals("Follow up", state.queue!!.single().displayText())
        client.updateQueue("s1", "q-1", "edit", "Updated follow up")
        val question = client.pendingQuestions("s1").single()
        assertEquals("Pick theme", question.questions.single().question)
        client.answerQuestion("s1", "ask-1", listOf(QuestionAnswerItem("q1", listOf("Dark"), null)))
        client.sendMessage(
            "s1",
            "Check this screenshot",
            steer = false,
            attachments = listOf(ImageAttachment(mediaType = "image/png", data = "AAAA", name = "screen.png")),
        )

        assertEquals("/api/v1/chat/sessions?includeArchived=true", server.takeRequest().path)
        assertEquals("/api/v1/chat/sessions/search?q=glass&includeArchived=true", server.takeRequest().path)
        assertEquals("POST", server.takeRequest().method)
        assertEquals("DELETE", server.takeRequest().method)
        assertEquals("/api/v1/chat/sessions/s1/unarchive", server.takeRequest().path)
        assertEquals("/api/v1/chat/permissions", server.takeRequest().path)
        assertEquals("/api/v1/chat/sessions/s1/state", server.takeRequest().path)
        val queueEdit = server.takeRequest()
        assertEquals("/api/v1/chat/sessions/s1/queue/q-1", queueEdit.path)
        assertTrue(queueEdit.body.readUtf8().contains("\"kind\":\"edit\""))
        assertEquals("/api/v1/chat/sessions/s1/questions", server.takeRequest().path)
        val answerReq = server.takeRequest()
        assertEquals("/api/v1/chat/sessions/s1/questions/ask-1/answer", answerReq.path)
        assertTrue(answerReq.body.readUtf8().contains("\"selected\":[\"Dark\"]"))
        val msgReq = server.takeRequest()
        val msgBody = msgReq.body.readUtf8()
        assertTrue(msgBody.contains("\"type\":\"image\""))
        assertTrue(msgBody.contains("\"mediaType\":\"image/png\""))
        assertTrue(msgBody.contains("\"name\":\"screen.png\""))
    }

    @Test
    fun managesWorkspacesAndSessionChildrenThroughVersionedRoutes() {
        server.enqueue(MockResponse().setBody(RENAMED_WORKSPACE_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("{\"deleted\":true}").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("{\"title\":\"Renamed session\",\"seq\":8}").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(201).setBody("{\"sessionId\":\"session-forked\"}").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody("{\"archived\":true}").setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString(), "device-token")

        assertEquals("Renamed Workspace", client.renameChatWorkspace("workspace/1", "Renamed Workspace").title)
        client.deleteChatWorkspace("workspace/1")
        assertEquals("Renamed session", client.renameSession("session/1", "Renamed session").title)
        assertEquals("session-forked", client.forkSession("session/1"))
        client.archiveSession("session/1")

        val workspaceRename = server.takeRequest()
        assertEquals("PATCH", workspaceRename.method)
        assertEquals("/api/v1/chat/workspaces/workspace%2F1", workspaceRename.path)
        assertEquals("{\"title\":\"Renamed Workspace\"}", workspaceRename.body.readUtf8())
        assertEquals("DELETE", server.takeRequest().method)
        val sessionRename = server.takeRequest()
        assertEquals("PATCH", sessionRename.method)
        assertEquals("/api/v1/chat/sessions/session%2F1", sessionRename.path)
        assertEquals("/api/v1/chat/sessions/session%2F1/fork", server.takeRequest().path)
        assertEquals("/api/v1/chat/sessions/session%2F1/archive", server.takeRequest().path)
    }

    @Test
    fun readsAndChangesSessionModelWithReasoningEffort() {
        server.enqueue(MockResponse().setBody(SESSION_MODELS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(SELECTED_MODEL_RESPONSE).setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString(), "device-token")

        val models = client.sessionModels("session-1")
        assertEquals("deepseek-v4-pro", models.current.model)
        assertEquals("max", models.current.reasoningEffort)
        assertEquals("High", models.groups.single().models.single().reasoning?.efforts?.get(1)?.name)
        assertEquals(
            "high",
            client.selectModel("session-1", ModelSelection("opencode-go", "deepseek-v4-pro", "high")).reasoningEffort,
        )

        assertEquals("/api/v1/chat/sessions/session-1/models", server.takeRequest().path)
        val selection = server.takeRequest()
        assertEquals("PUT", selection.method)
        assertTrue(selection.body.readUtf8().contains("\"reasoningEffort\":\"high\""))
    }

    @Test
    fun readsAndDecidesPendingApprovalWithoutPrivateRpcFields() {
        server.enqueue(MockResponse().setBody(APPROVALS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(202).setBody(""))
        val client = DshClient(server.url("/").toString(), "device-token")

        val approval = client.pendingApprovals("session/1").single()
        assertEquals("approval-1", approval.id)
        assertEquals("full-access", approval.risk)
        assertEquals("echo [REDACTED]", approval.detail)
        client.decideApproval("session/1", approval.id, allowOnce = true)

        assertEquals("/api/v1/chat/sessions/session%2F1/approvals", server.takeRequest().path)
        val decision = server.takeRequest()
        assertEquals("POST", decision.method)
        assertEquals("/api/v1/chat/sessions/session%2F1/approvals/approval-1/decision", decision.path)
        assertEquals("{\"outcome\":\"allowed-once\"}", decision.body.readUtf8())
    }

    @Test
    fun listsAndExecutesHostSlashCommandsThroughTheVersionedApi() {
        server.enqueue(MockResponse().setBody(COMMANDS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(COMMAND_EXECUTION_RESPONSE).setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString(), "device-token")

        val command = client.sessionCommands("session/1").single()
        assertEquals("permission", command.name)
        assertEquals("preset", command.input?.hint)
        assertEquals("workspace-write", client.executeCommand("session/1", "/permission workspace-write").result.text)

        assertEquals("/api/v1/chat/sessions/session%2F1/commands", server.takeRequest().path)
        val execution = server.takeRequest()
        assertEquals("POST", execution.method)
        assertEquals("/api/v1/chat/sessions/session%2F1/commands", execution.path)
        assertEquals("{\"line\":\"/permission workspace-write\"}", execution.body.readUtf8())
    }

    @Test
    fun readsUpdatesAndRemovesProviderSettingsWithoutExpectingSecretValues() {
        server.enqueue(MockResponse().setBody(PROVIDER_SETTINGS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(PROVIDER_SETTINGS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(PROVIDER_SETTINGS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(DISCOVERED_MODELS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(204))
        val client = DshClient(server.url("/").toString(), "device-token")

        val settings = client.providerSettings()
        assertTrue(settings.providers.single().credential.configured)
        assertEquals("https://default.example/v1", settings.providers.single().defaultBaseURL)
        assertEquals("openai-completions", settings.customProvider.protocols.first())
        client.updateProvider(
            "custom-openai",
            ProviderPatch(baseURL = "https://api.example/v1", apiKey = "write-only", models = listOf(ProviderModel("model-b"))),
        )
        client.createCustomProvider(
            CustomProviderCreate(
                id = "acme-gateway",
                baseURL = "https://acme.example/v1",
                api = "openai-responses",
                models = listOf(ProviderModel("acme-code")),
                expectedRevision = 3,
            ),
        )
        assertEquals("model-b", client.discoverModels("custom-openai", null, null, null).single().id)
        client.removeProvider("acme-gateway", expectedRevision = 3)

        assertEquals("/api/v1/settings/providers", server.takeRequest().path)
        val update = server.takeRequest()
        assertEquals("PATCH", update.method)
        val updateBody = update.body.readUtf8()
        assertTrue(updateBody.contains("\"apiKey\":\"write-only\""))
        assertTrue(updateBody.contains("\"baseURL\":\"https://api.example/v1\""))
        val create = server.takeRequest()
        assertEquals("POST", create.method)
        assertEquals("/api/v1/settings/providers", create.path)
        assertTrue(create.body.readUtf8().contains("\"id\":\"acme-gateway\""))
        assertEquals("/api/v1/settings/providers/custom-openai/discover", server.takeRequest().path)
        val remove = server.takeRequest()
        assertEquals("DELETE", remove.method)
        assertEquals("/api/v1/settings/providers/acme-gateway?expectedRevision=3", remove.path)
    }

    private companion object {
        const val PAIRING_RESPONSE = """{"token":"secret-token","device":{"id":"device-1","name":"Pixel","scopes":["files.read"],"rootIds":["root-1"]}}"""
        const val ENTRIES_RESPONSE = """{"path":"目录","entries":[{"name":"file name.txt","path":"目录/file name.txt","kind":"file","size":5,"modifiedAt":1,"writable":true}]}"""
        const val WORKSPACES_RESPONSE = """{"items":[{"id":"workspace-1","title":"WebUI Workspace","rootId":"root-1","path":"project","createdAt":"2026-08-14T00:00:00.000Z","updatedAt":"2026-08-14T00:00:00.000Z"}]}"""
        const val CREATED_WORKSPACE_RESPONSE = """{"workspace":{"id":"workspace-2","title":"New Workspace","rootId":"root-1","path":"projects/new","createdAt":"2026-08-14T00:00:00.000Z","updatedAt":"2026-08-14T00:00:00.000Z"}}"""
        const val RENAMED_WORKSPACE_RESPONSE = """{"workspace":{"id":"workspace-1","title":"Renamed Workspace","rootId":"root-1","path":"project","createdAt":"2026-08-14T00:00:00.000Z","updatedAt":"2026-08-15T00:00:00.000Z"}}"""
        const val PRESETS_WITHOUT_TRUST_RESPONSE = """{"items":[{"id":"standard","name":"Coding","description":"Full coding agent","isDefault":true,"available":true}]}"""
        const val CREATED_SESSION_RESPONSE = """{"session":{"id":"session-created","agentPreset":"standard"}}"""
        const val SELECTED_PRESET_RESPONSE = """{"agentPreset":"minimal"}"""
        const val SESSION_MODELS_RESPONSE = """{"current":{"provider":"opencode-go","model":"deepseek-v4-pro","reasoningEffort":"max"},"routable":true,"groups":[{"id":"opencode-go","name":"OpenCode Go","models":[{"id":"deepseek-v4-pro","name":"DeepSeek V4 Pro","reasoning":{"efforts":[{"id":"off","name":"Off"},{"id":"high","name":"High"},{"id":"max","name":"Max"}],"defaultEffort":"max"}}]}],"failures":[]}"""
        const val SELECTED_MODEL_RESPONSE = """{"selected":{"provider":"opencode-go","model":"deepseek-v4-pro","reasoningEffort":"high"}}"""
        const val APPROVALS_RESPONSE = """{"items":[{"id":"approval-1","sessionId":"session/1","toolName":"bash","reason":"danger-full-access","detail":"echo [REDACTED]","risk":"full-access","requestedAt":1}]}"""
        const val COMMANDS_RESPONSE = """{"items":[{"name":"permission","description":"Change the permission preset","input":{"hint":"preset"}}]}"""
        const val COMMAND_EXECUTION_RESPONSE = """{"execution":{"commandId":"permission","result":{"kind":"text","text":"workspace-write"}}}"""
        const val PROVIDER_SETTINGS_RESPONSE = """{"writable":true,"revisionByNamespace":{"llm-pi-ai":3},"customProvider":{"available":true,"protocols":["openai-completions","openai-responses","anthropic-messages"],"revision":3},"providers":[{"id":"custom-openai","displayName":"Custom OpenAI","active":true,"defaultBaseURL":"https://default.example/v1","configurable":true,"configured":true,"removable":true,"credential":{"ref":"CUSTOM_OPENAI_API_KEY","configured":true,"writable":true},"config":{"baseURL":"https://api.example/v1","api":"openai-completions","models":[{"id":"model-a"}]}}]}"""
        const val DISCOVERED_MODELS_RESPONSE = """{"models":[{"id":"model-b","name":"Model B"}]}"""
    }
}
