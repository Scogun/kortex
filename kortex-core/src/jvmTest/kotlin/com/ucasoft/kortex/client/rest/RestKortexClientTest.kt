package com.ucasoft.kortex.client.rest

import com.ucasoft.kortex.client.ClientConfig
import com.ucasoft.kortex.di.clientModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap

class RestKortexClientTest : KoinTest {

    private val testPort = ServerSocket(0).use { it.localPort }
    private val testToken = "test_token"
    private lateinit var server: EmbeddedServer<*, *>

    private val savedAutomationConfigs = ConcurrentHashMap<String, String>()

    private val deletedAutomationConfigs = ConcurrentHashMap.newKeySet<String>()

    @BeforeEach
    fun setup() {
        server = embeddedServer(CIO, port = testPort) {
            routing {
                get("/api/config/automation/config/{id}") {
                    when {
                        call.request.headers[HttpHeaders.Authorization] != "Bearer $testToken" ->
                            call.respond(HttpStatusCode.Unauthorized)

                        call.parameters["id"] == "existing" ->
                            call.respondText("""{"id": "existing", "alias": "Existing"}""", ContentType.Application.Json)

                        call.parameters["id"] == "broken" ->
                            call.respond(HttpStatusCode.InternalServerError)

                        else ->
                            call.respondText(
                                """{"message": "Resource not found"}""",
                                ContentType.Application.Json,
                                HttpStatusCode.NotFound
                            )
                    }
                }
                post("/api/config/automation/config/{id}") {
                    when {
                        call.request.headers[HttpHeaders.Authorization] != "Bearer $testToken" ->
                            call.respond(HttpStatusCode.Unauthorized)

                        !call.request.contentType().match(ContentType.Application.Json) ->
                            call.respond(HttpStatusCode.UnsupportedMediaType)

                        call.parameters["id"] == "broken" ->
                            call.respondText(
                                """{"message": "Message malformed: broken"}""",
                                ContentType.Application.Json,
                                HttpStatusCode.BadRequest
                            )

                        else -> {
                            savedAutomationConfigs[call.parameters.getOrFail("id")] = call.receiveText()
                            call.respondText("""{"result": "ok"}""", ContentType.Application.Json)
                        }
                    }
                }
                delete("/api/config/automation/config/{id}") {
                    when {
                        call.request.headers[HttpHeaders.Authorization] != "Bearer $testToken" ->
                            call.respond(HttpStatusCode.Unauthorized)

                        call.parameters["id"] == "existing" -> {
                            deletedAutomationConfigs += "existing"
                            call.respondText("""{"result": "ok"}""", ContentType.Application.Json)
                        }

                        else ->
                            call.respondText(
                                """{"message": "Resource not found"}""",
                                ContentType.Application.Json,
                                HttpStatusCode.BadRequest
                            )
                    }
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
            }, clientModule)
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        server.stop(100, 100)
    }

    @Test
    fun `get automation config should return config of existing automation`() = runTest {
        val config = RestKortexClient().getAutomationConfig("existing")

        config.shouldNotBeNull().getValue("alias").jsonPrimitive.content.shouldBe("Existing")
    }

    @Test
    fun `get automation config should return null for unknown automation`() = runTest {
        RestKortexClient().getAutomationConfig("unknown").shouldBeNull()
    }

    @Test
    fun `get automation config should fail on unexpected status`() = runTest {
        shouldThrow<IllegalStateException> {
            RestKortexClient().getAutomationConfig("broken")
        }
    }

    @Test
    fun `save automation config should post config as json`() = runTest {
        val config = buildJsonObject { put("alias", "New") }

        RestKortexClient().saveAutomationConfig("new", config)

        Json.parseToJsonElement(savedAutomationConfigs["new"].shouldNotBeNull()).shouldBe(config)
    }

    @Test
    fun `save automation config should fail with the reason on unexpected status`() = runTest {
        shouldThrow<IllegalStateException> {
            RestKortexClient().saveAutomationConfig("broken", buildJsonObject { })
        }.message.shouldContain("Message malformed: broken")
    }

    @Test
    fun `delete automation config should delete existing automation`() = runTest {
        RestKortexClient().deleteAutomationConfig("existing")

        deletedAutomationConfigs.shouldContainExactly("existing")
    }

    @Test
    fun `delete automation config should fail with the reason on unexpected status`() = runTest {
        shouldThrow<IllegalStateException> {
            RestKortexClient().deleteAutomationConfig("unknown")
        }.message.shouldContain("Resource not found")
    }

    @Test
    fun `build image url should resolve entity picture against configured host`() {
        RestKortexClient().buildImageUrl("/api/image_proxy/image.test?token=abc")
            .shouldBe("http://localhost:$testPort/api/image_proxy/image.test?token=abc")
    }

    @Test
    fun `build image url should use https for secure config`() {
        loadKoinModules(module {
            single { ClientConfig(hostname = "ha.test", port = 8123, token = testToken, secure = true) }
        })

        RestKortexClient().buildImageUrl("/api/image_proxy/image.test?token=abc")
            .shouldBe("https://ha.test:8123/api/image_proxy/image.test?token=abc")
    }

    @Test
    fun `build image url should keep absolute entity picture`() {
        RestKortexClient().buildImageUrl("https://images.test/picture.png?size=2")
            .shouldBe("https://images.test/picture.png?size=2")
    }
}
