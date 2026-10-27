package io.github.hakunm.deepseekharness.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
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
        server.enqueue(MockResponse().setBody("""{"ok":true,"version":"v1"}""").setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(PAIRING_RESPONSE).setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString())

        assertEquals("v1", client.health().version)
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
    fun requestsEarlierChatHistoryBySequence() {
        server.enqueue(MockResponse().setBody("""{"events":[],"hasMore":false}""").setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString(), "device-token")

        assertEquals(emptyList<HistoryEntry>(), client.history("session/1", beforeSeq = 42, maxMessages = 25).events)

        val request = server.takeRequest()
        assertEquals("/api/v1/chat/sessions/session%2F1/messages?beforeSeq=42&maxMessages=25", request.path)
    }

    @Test
    fun decodesBareSessionWireHistoryFromWorkspace11() {
        server.enqueue(MockResponse().setBody(
            """{"events":[{"type":"user/message","seq":1,"time":1761500000000,"data":{"content":[{"type":"text","text":"ping"}],"source":{"kind":"user"}}}],"hasMore":true}""",
        ).setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString(), "device-token")

        val history = client.history("session-1")

        assertEquals("ping", history.displayItems().single().body)
        assertTrue(history.hasMore)
        assertEquals("/api/v1/chat/sessions/session-1/messages", server.takeRequest().path)
    }

    @Test
    fun websocketHandshakeKeepsAnOkHttpCompatibleUrlScheme() {
        val httpClient = DshClient("http://example.test:3090")
        val httpsClient = DshClient("https://example.test")

        assertEquals("http://example.test:3090/api/v1/events", httpClient.eventsUrl().toString())
        assertEquals("https://example.test/api/v1/events", httpsClient.eventsUrl().toString())
    }

    @Test
    fun readsDhsWorkspaceAndAgentChoicesAndCreatesByWorkspaceIdentity() {
        server.enqueue(MockResponse().setBody(WORKSPACES_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(CREATED_WORKSPACE_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(PRESETS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(CREATED_SESSION_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(SELECTED_PRESET_RESPONSE).setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString(), "device-token")

        assertEquals("WebUI Workspace", client.chatWorkspaces().single().title)
        assertEquals("New Workspace", client.createChatWorkspace("root-1", "projects/new").title)
        assertEquals("Coding", client.agentPresets().single().name)
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
    fun readsAndUpdatesProviderSettingsWithoutExpectingSecretValues() {
        server.enqueue(MockResponse().setBody(PROVIDER_SETTINGS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(PROVIDER_SETTINGS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(PROVIDER_SETTINGS_RESPONSE).setHeader("Content-Type", "application/json"))
        server.enqueue(MockResponse().setBody(DISCOVERED_MODELS_RESPONSE).setHeader("Content-Type", "application/json"))
        val client = DshClient(server.url("/").toString(), "device-token")

        val settings = client.providerSettings()
        assertTrue(settings.providers.single().credential.configured)
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
    }

    private companion object {
        const val PAIRING_RESPONSE = """{"token":"secret-token","device":{"id":"device-1","name":"Pixel","scopes":["files.read"],"rootIds":["root-1"]}}"""
        const val ENTRIES_RESPONSE = """{"path":"目录","entries":[{"name":"file name.txt","path":"目录/file name.txt","kind":"file","size":5,"modifiedAt":1,"writable":true}]}"""
        const val WORKSPACES_RESPONSE = """{"items":[{"id":"workspace-1","title":"WebUI Workspace","rootId":"root-1","path":"project","createdAt":"2026-08-14T00:00:00.000Z","updatedAt":"2026-08-14T00:00:00.000Z"}]}"""
        const val CREATED_WORKSPACE_RESPONSE = """{"workspace":{"id":"workspace-2","title":"New Workspace","rootId":"root-1","path":"projects/new","createdAt":"2026-08-14T00:00:00.000Z","updatedAt":"2026-08-14T00:00:00.000Z"}}"""
        const val RENAMED_WORKSPACE_RESPONSE = """{"workspace":{"id":"workspace-1","title":"Renamed Workspace","rootId":"root-1","path":"project","createdAt":"2026-08-14T00:00:00.000Z","updatedAt":"2026-08-15T00:00:00.000Z"}}"""
        const val PRESETS_RESPONSE = """{"items":[{"id":"standard","name":"Coding","description":"Full coding agent","trust":"system","isDefault":true,"available":true}]}"""
        const val CREATED_SESSION_RESPONSE = """{"session":{"id":"session-created","agentPreset":"standard"}}"""
        const val SELECTED_PRESET_RESPONSE = """{"agentPreset":"minimal"}"""
        const val SESSION_MODELS_RESPONSE = """{"current":{"provider":"opencode-go","model":"deepseek-v4-pro","reasoningEffort":"max"},"routable":true,"groups":[{"id":"opencode-go","name":"OpenCode Go","models":[{"id":"deepseek-v4-pro","name":"DeepSeek V4 Pro","reasoning":{"efforts":[{"id":"off","name":"Off"},{"id":"high","name":"High"},{"id":"max","name":"Max"}],"defaultEffort":"max"}}]}],"failures":[]}"""
        const val SELECTED_MODEL_RESPONSE = """{"selected":{"provider":"opencode-go","model":"deepseek-v4-pro","reasoningEffort":"high"}}"""
        const val APPROVALS_RESPONSE = """{"items":[{"id":"approval-1","sessionId":"session/1","toolName":"bash","reason":"danger-full-access","detail":"echo [REDACTED]","risk":"full-access","requestedAt":1}]}"""
        const val COMMANDS_RESPONSE = """{"items":[{"name":"permission","description":"Change the permission preset","input":{"hint":"preset"}}]}"""
        const val COMMAND_EXECUTION_RESPONSE = """{"execution":{"commandId":"permission","result":{"kind":"text","text":"workspace-write"}}}"""
        const val PROVIDER_SETTINGS_RESPONSE = """{"writable":true,"revisionByNamespace":{"llm-pi-ai":3},"customProvider":{"available":true,"protocols":["openai-completions","openai-responses","anthropic-messages"],"revision":3},"providers":[{"id":"custom-openai","displayName":"Custom OpenAI","active":true,"configurable":true,"configured":true,"removable":true,"credential":{"ref":"CUSTOM_OPENAI_API_KEY","configured":true,"writable":true},"config":{"baseURL":"https://api.example/v1","api":"openai-completions","models":[{"id":"model-a"}]}}]}"""
        const val DISCOVERED_MODELS_RESPONSE = """{"models":[{"id":"model-b","name":"Model B"}]}"""
    }
}
