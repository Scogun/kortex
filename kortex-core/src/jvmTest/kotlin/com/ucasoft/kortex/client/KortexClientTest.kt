package com.ucasoft.kortex.client

import com.ucasoft.kortex.client.websocket.requests.EventType
import com.ucasoft.kortex.client.websocket.ContextResponse
import com.ucasoft.kortex.client.websocket.KortexContext
import com.ucasoft.kortex.client.websocket.WSKortexClient
import com.ucasoft.kortex.di.clientModule
import com.ucasoft.kortex.entities.Event
import com.ucasoft.kortex.entities.State
import com.ucasoft.kortex.entities.Trace
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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

    private var testPort = 0
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
        server = embeddedServer(CIO, port = 0) {
            install(WebSockets)
            routing {
                webSocket("/api/websocket") {
                    serverHandler()
                }
            }
        }.start(wait = false)
        testPort = runBlocking { server.engine.resolvedConnectors().first().port }

        startKoin {
            modules(module {
                single {
                    ClientConfig(
                        hostname = "localhost",
                        port = testPort,
                        token = testToken
                    )
                }
            }, clientModule)
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

        val client = WSKortexClient()
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
                context.shouldNotBeNull()
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

        val client = WSKortexClient()

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

        val client = WSKortexClient()
        launch {
            client.connect(
                onAuthenticated = {},
                onEvent = { eventDeferred.complete(it) }
            )
        }

        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(2.seconds) {
                val event = eventDeferred.await()
                event.type.shouldBe("test_event")
                event.data.newState.shouldNotBeNull().state.shouldBe("on")
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

        val client = WSKortexClient()
        client.connect(
            onAuthenticated = {},
            onEvent = { eventCalled.complete(true) }
        )

        eventCalled.isCompleted.shouldBeFalse()
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

        val client = WSKortexClient()
        client.connect(
            onAuthenticated = {},
            onEvent = { eventCalled.complete(true) }
        )

        eventCalled.isCompleted.shouldBeFalse()
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

        val client = WSKortexClient()
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
                requestId.shouldBe(1)
                result.shouldBeInstanceOf<List<State>>().shouldBeSingleton {
                    it.entityId.shouldBe("light.test")
                    it.state.shouldBe("on")
                }
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

        val client = WSKortexClient()
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

        val client = WSKortexClient()
        client.connect(
            onAuthenticated = { context -> context.subscribeEvents(EventType.STATE_CHANGED) },
            onResult = { _, _ -> resultCalled.complete(true) },
            onEvent = {}
        )

        resultCalled.isCompleted.shouldBeFalse()
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

        val client = WSKortexClient()
        client.connect(
            onAuthenticated = { context -> context.getStates() },
            onResult = { _, _ -> resultCalled.complete(true) },
            onEvent = {}
        )

        resultCalled.isCompleted.shouldBeFalse()
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

        val client = WSKortexClient()
        client.connect(
            onAuthenticated = { authDeferred.complete(true) },
            onEvent = { eventCalled.complete(true) }
        )

        authDeferred.await().shouldBeTrue()
        eventCalled.isCompleted.shouldBeFalse()
    }

    @Test
    fun `connect should deliver automation triggered events to onEvent`() = runTest {
        val eventDeferred = CompletableDeferred<Event>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            awaitTextFrameMatching(Regex("\"event_type\"\\s*:\\s*\"automation_triggered\""))
            send(Frame.Text("""
                {
                    "id": 1,
                    "type": "event",
                    "event": {
                        "event_type": "automation_triggered",
                        "data": {
                            "name": "Kortex test",
                            "entity_id": "automation.kortex_test",
                            "item_id": "kortex_test",
                            "run_id": "run-1"
                        },
                        "origin": "LOCAL",
                        "time_fired": "2024-01-01T00:00:00Z"
                    }
                }
            """.trimIndent()))
            delay(150.milliseconds)
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = WSKortexClient()
        client.connect(
            onAuthenticated = { context -> context.subscribeEvents(EventType.AUTOMATION_TRIGGERED) },
            onEvent = { eventDeferred.complete(it) }
        )

        val event = eventDeferred.await()
        event.type.shouldBe("automation_triggered")
        event.data.entityId.shouldBe("automation.kortex_test")
        event.data.itemId.shouldBe("kortex_test")
        event.data.runId.shouldBe("run-1")
        event.data.newState.shouldBeNull()
    }

    @Test
    fun `get trace should dispatch trace with trigger id to onResult`() = runTest {
        val requestFrame = CompletableDeferred<String>()
        val resultDeferred = CompletableDeferred<Any>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            requestFrame.complete(awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"trace/get\"")))
            send(Frame.Text("""
                {
                    "id": 1,
                    "type": "result",
                    "success": true,
                    "result": {
                        "last_step": "trigger/1",
                        "run_id": "run-1",
                        "state": "running",
                        "domain": "automation",
                        "item_id": "kortex_test",
                        "trace": {
                            "trigger/1": [
                                {
                                    "path": "trigger/1",
                                    "timestamp": "2024-01-01T00:00:00Z",
                                    "changed_variables": {
                                        "this": { "entity_id": "automation.kortex_test", "state": "on" },
                                        "trigger": {
                                            "id": "turned_off",
                                            "idx": "1",
                                            "alias": null,
                                            "platform": "state",
                                            "description": "state of switch.test",
                                            "entity_id": "switch.test",
                                            "from_state": {
                                                "entity_id": "switch.test",
                                                "state": "on",
                                                "attributes": { "friendly_name": "Test Switch" },
                                                "last_changed": "2024-01-01T00:00:00Z",
                                                "last_reported": "2024-01-01T00:00:00Z",
                                                "last_updated": "2024-01-01T00:00:00Z"
                                            },
                                            "to_state": {
                                                "entity_id": "switch.test",
                                                "state": "off",
                                                "attributes": { "friendly_name": "Test Switch" },
                                                "last_changed": "2024-01-01T00:01:00Z",
                                                "last_reported": "2024-01-01T00:01:00Z",
                                                "last_updated": "2024-01-01T00:01:00Z"
                                            },
                                            "for": null
                                        }
                                    }
                                }
                            ]
                        },
                        "config": { "id": "kortex_test" },
                        "context": { "id": "01", "parent_id": null, "user_id": null }
                    }
                }
            """.trimIndent()))
            delay(150.milliseconds)
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = WSKortexClient()
        client.connect(
            onAuthenticated = { context -> context.getTrace("kortex_test", "run-1") },
            onResult = { _, result -> resultDeferred.complete(result) },
            onEvent = {}
        )

        requestFrame.await().let {
            it.shouldContain("\"domain\":\"automation\"")
            it.shouldContain("\"item_id\":\"kortex_test\"")
            it.shouldContain("\"run_id\":\"run-1\"")
        }
        resultDeferred.await().shouldBeInstanceOf<Trace>().let {
            it.runId.shouldBe("run-1")
            it.triggerId.shouldBe("turned_off")
            it.trigger.shouldNotBeNull().let { trigger ->
                trigger.idx.shouldBe("1")
                trigger.platform.shouldBe("state")
                trigger.entityId.shouldBe("switch.test")
                trigger.fromState.shouldNotBeNull().state.shouldBe("on")
                trigger.toState.shouldNotBeNull().state.shouldBe("off")
            }
        }
    }

    @Test
    fun `trace of a manual run should have no trigger id`() = runTest {
        val resultDeferred = CompletableDeferred<Any>()

        serverHandler = {
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"auth\""))
            send(Frame.Text("""{"type": "auth_ok"}"""))
            awaitTextFrameMatching(Regex("\"type\"\\s*:\\s*\"trace/get\""))
            send(Frame.Text("""
                {
                    "id": 1,
                    "type": "result",
                    "success": true,
                    "result": {
                        "run_id": "run-2",
                        "item_id": "kortex_test",
                        "trace": {
                            "trigger": [
                                {
                                    "path": "trigger",
                                    "timestamp": "2024-01-01T00:00:00Z",
                                    "changed_variables": { "trigger": { "platform": null } }
                                }
                            ]
                        }
                    }
                }
            """.trimIndent()))
            delay(150.milliseconds)
            close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        }

        val client = WSKortexClient()
        client.connect(
            onAuthenticated = { context -> context.getTrace("kortex_test", "run-2") },
            onResult = { _, result -> resultDeferred.complete(result) },
            onEvent = {}
        )

        resultDeferred.await().shouldBeInstanceOf<Trace>().let {
            it.triggerId.shouldBeNull()
            it.trigger.shouldNotBeNull().platform.shouldBeNull()
        }
    }
}
