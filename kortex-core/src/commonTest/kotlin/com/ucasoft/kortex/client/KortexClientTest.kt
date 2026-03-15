package com.ucasoft.kortex.client

import com.ucasoft.kortex.client.requests.EventType
import com.ucasoft.kortex.di.webSocketClient
import com.ucasoft.kortex.entities.Event
import com.ucasoft.kortex.entities.State
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class KortexClientTest : KoinTest {

    private val testPort = ServerSocket(0).use { it.localPort }
    private val testToken = "test_token"
    private lateinit var server: EmbeddedServer<*, *>

    private var serverHandler: suspend DefaultWebSocketServerSession.() -> Unit = {}

    private suspend fun DefaultWebSocketServerSession.awaitTextFrameMatching(regex: Regex): String {
        while (true) {
            val frame = incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                if (regex.containsMatchIn(text)) {
                    return text
                }
            }
        }
    }

    @BeforeEach
    fun setup() {
        server = embeddedServer(CIO, port = testPort) {
            install(WebSockets)
            routing {
                webSocket("/api/websocket") {
                    serverHandler()
                }
            }
        }.start(wait = false)

        startKoin {
            modules(module {
                single {
                    ClientConfig(
                        hostname = "localhost",
                        port = testPort,
                        token = testToken
                    )
                }
            }, webSocketClient)
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        server.stop(100, 100)
    }

    @Test
    fun `connect should authenticate successfully`() = runTest {
        val authDeferred = CompletableDeferred<KortexContext>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        launch {
            client.connect(
                onAuthenticated = {
                    authDeferred.complete(it)
                },
                onEvent = {}
            )
        }

        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(2.seconds) {
                val context = authDeferred.await()
                context shouldNotBeNull {}
            }
        }
    }

    @Test
    fun `connect should throw exception on invalid auth`() = runTest {
        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_invalid"}"""))
            awaitCancellation()
        }

        val client = KortexClient()

        shouldThrowWithMessage<Exception>("Authentication failed: Invalid token") {
            client.connect(onAuthenticated = {}, onEvent = {})
        }
    }

    @Test
    fun `connect should receive events`() = runTest {
        val eventDeferred = CompletableDeferred<Event>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            send(Frame.Text("""
                {
                    "type": "event", 
                    "event": {
                        "event_type": "test_event",
                        "data": { "entity_id": "1", "old_state": {
                            "entity_id" : "1",
                            "state": "off",
                            "attributes": {
                                "friendly_name": "Test Entity"
                            },
                            "last_changed": "2024-01-01T00:00:00Z",
                            "last_reported": "2024-01-01T00:00:00Z",
                            "last_updated": "2024-01-01T00:00:00Z"
                        }, "new_state": {
                            "entity_id" : "1",
                            "state": "on",
                            "attributes": {
                                "friendly_name": "Test Entity"
                            },
                            "last_changed": "2024-01-01T00:00:00Z",
                            "last_reported": "2024-01-01T00:00:00Z",
                            "last_updated": "2024-01-01T00:00:00Z"
                        } },
                        "time_fired": "2024-01-01T00:00:00Z"
                    }
                }
            """.trimIndent()))
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        launch {
            client.connect(
                onAuthenticated = {},
                onEvent = { eventDeferred.complete(it) }
            )
        }

        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(2.seconds) {
                val event = eventDeferred.await()
                event.type shouldBe "test_event"
                event.data.newState.state shouldBe "on"
            }
        }
    }

    @Test
    fun `connect should ignore event messages without event payload`() = runTest {
        val eventCalled = CompletableDeferred<Boolean>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            send(Frame.Text("""{"type": "event"}"""))
            delay(150.milliseconds)
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        client.connect(
            onAuthenticated = {},
            onEvent = { eventCalled.complete(true) }
        )

        eventCalled.isCompleted shouldBe false
    }

    @Test
    fun `connect should ignore non text frames`() = runTest {
        val eventCalled = CompletableDeferred<Boolean>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Binary(true, byteArrayOf(1, 2, 3)))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            delay(150.milliseconds)
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        client.connect(
            onAuthenticated = {},
            onEvent = { eventCalled.complete(true) }
        )

        eventCalled.isCompleted shouldBe false
    }

    @Test
    fun `connect should dispatch get states result to onResult`() = runTest {
        val resultDeferred = CompletableDeferred<Pair<Int, Any>>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"get_states\""))
            send(Frame.Text("""
                {
                    "id": 1,
                    "type": "result",
                    "success": true,
                    "result": [
                        {
                            "entity_id": "light.test",
                            "state": "on",
                            "attributes": {"friendly_name": "Test Light"},
                            "last_changed": "2024-01-01T00:00:00Z",
                            "last_reported": "2024-01-01T00:00:00Z",
                            "last_updated": "2024-01-01T00:00:00Z"
                        }
                    ]
                }
            """.trimIndent()))
            delay(150.milliseconds)
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        launch {
            client.connect(
                onAuthenticated = { context -> context.getStates() },
                onResult = { requestId, result -> resultDeferred.complete(requestId to result) },
                onEvent = {}
            )
        }

        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(2.seconds) {
                val (requestId, result) = resultDeferred.await()
                requestId shouldBe 1
                val states = result.shouldBeInstanceOf<List<State>>()
                states shouldHaveSize 1
                states.first().entityId shouldBe "light.test"
                states.first().state shouldBe "on"
            }
        }
    }

    @Test
    fun `connect should route pending request result to context response`(): Unit = runBlocking {
        val responseDeferred = CompletableDeferred<ContextResponse>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"call_service\""))
            send(Frame.Text("""
                {
                    "id": 1,
                    "type": "result",
                    "success": true,
                    "result": {
                        "context": {
                            "id": "light.test",
                            "parent_id": null,
                            "user_id": null
                        },
                        "response": {
                            "light.test": {
                                "entity_id": "light.test",
                                "state": "on"
                            }
                        }
                    }
                }
            """.trimIndent()))
            delay(150.milliseconds)
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        launch {
            client.connect(
                onAuthenticated = { context ->
                    launch {
                        responseDeferred.complete(
                            context.callServiceWithResponse(
                                domain = "light",
                                service = "turn_on",
                                timeout = 5.seconds
                            )
                        )
                    }
                },
                onEvent = {}
            )
        }

        withTimeout(5.seconds) {
            val response = responseDeferred.await()
            response.response["light.test"].shouldNotBeNull()
        }
    }

    @Test
    fun `connect should ignore successful result without known request type`() = runTest {
        val resultCalled = CompletableDeferred<Boolean>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"subscribe_events\""))
            send(Frame.Text("""
                {
                    "id": 1,
                    "type": "result",
                    "success": true,
                    "result": {"ok": true}
                }
            """.trimIndent()))
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        client.connect(
            onAuthenticated = { context -> context.subscribeEvents(EventType.STATE_CHANGED) },
            onResult = { _, _ -> resultCalled.complete(true) },
            onEvent = {}
        )

        resultCalled.isCompleted shouldBe false
    }

    @Test
    fun `connect should ignore failed result and result without id`() = runTest {
        val resultCalled = CompletableDeferred<Boolean>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"get_states\""))
            send(Frame.Text("""{"id": 1, "type": "result", "success": false, "message": "bad"}"""))
            send(Frame.Text("""{"type": "result", "success": true, "result": []}"""))
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        client.connect(
            onAuthenticated = { context -> context.getStates() },
            onResult = { _, _ -> resultCalled.complete(true) },
            onEvent = {}
        )

        resultCalled.isCompleted shouldBe false
    }

    @Test
    fun `connect should ignore unknown message types`() = runTest {
        val authDeferred = CompletableDeferred<Boolean>()
        val eventCalled = CompletableDeferred<Boolean>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            send(Frame.Text("""{"type": "mystery", "value": 123}"""))
            delay(150.milliseconds)
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = KortexClient()
        client.connect(
            onAuthenticated = { authDeferred.complete(true) },
            onEvent = { eventCalled.complete(true) }
        )

        authDeferred.await() shouldBe true
        eventCalled.isCompleted shouldBe false
    }
}
