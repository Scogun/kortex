package com.ucasoft.kortex.client

import com.ucasoft.kortex.client.requests.RequestType
import com.ucasoft.kortex.entities.Event
import com.ucasoft.kortex.entities.State
import com.ucasoft.kortex.json
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.decodeFromJsonElement
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KortexClient : KoinComponent {

    private val config by inject<ClientConfig>()

    private val cio = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
    }

    suspend fun connect(
        onAuthenticated: suspend (context: KortexContext) -> Unit,
        onResult: suspend (requestId: Int, result: Any) -> Unit = { _, _ -> },
        onEvent: suspend (event: Event) -> Unit
    ) {
        cio.webSocket("ws://${config.hostname}:${config.port}/api/websocket") {

            val scope = CoroutineScope(coroutineContext + SupervisorJob())

            val context = KortexContext(this, scope)

            try {
                context.auth(config.token)

                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val frameText = frame.readText()
                    val response = json.decodeFromString<CommonResponse>(frameText)

                    when (response.type) {
                        "auth_ok" -> {
                            onAuthenticated(context)
                        }

                        "auth_invalid" -> {
                            throw Exception("Authentication failed: Invalid token")
                        }

                        "event" -> {
                            response.event?.let { onEvent(json.decodeFromJsonElement<Event>(it)) }
                        }

                        "result" -> {
                            if (response.success == true && response.id != null) {
                                val requestType = context.getRequestType(response.id)
                                when (requestType) {
                                    RequestType.GET_STATES -> {
                                        onResult(
                                            response.id,
                                            json.decodeFromJsonElement<List<State>>(response.result!!)
                                        )
                                    }
                                    null -> response.result?.let { context.onPendingResult(response.id, json.decodeFromJsonElement<ContextResponse>(it)) }
                                    else -> {
                                        println("Request ${response.id} result: ${response.result}")
                                    }
                                }
                            } else {
                                println("Request ${response.id} failed: ${response.message}")
                            }
                        }

                        else -> {
                            println(frameText)
                        }
                    }
                }
            } finally {
                scope.cancel()
            }
        }
    }
}