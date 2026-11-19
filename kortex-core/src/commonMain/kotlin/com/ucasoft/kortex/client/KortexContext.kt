package com.ucasoft.kortex.client

import com.ucasoft.kortex.client.requests.AuthRequest
import com.ucasoft.kortex.client.requests.CallServiceRequest
import com.ucasoft.kortex.client.requests.EventType
import com.ucasoft.kortex.client.requests.RegistryRequest
import com.ucasoft.kortex.client.requests.RegistryType
import com.ucasoft.kortex.client.requests.Request
import com.ucasoft.kortex.client.requests.RequestType
import com.ucasoft.kortex.client.requests.SubscribeEventRequest
import com.ucasoft.kortex.json
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAtomicApi::class)
class KortexContext(private val session: DefaultClientWebSocketSession, internal val scope: CoroutineScope) {

    private val id = AtomicInt(0)
    
    private val nextId
        get() = id.incrementAndFetch()

    private val requestIds = mutableMapOf<Int, RequestType>()
    private val pendingRequests = mutableMapOf<Int, CompletableDeferred<ContextResponse>>()

    suspend fun callService(domain: String, service: String, data: Map<String, JsonElement> = emptyMap()): Int {
        val id = request(
            CallServiceRequest(
                callId = nextId,
                domain = domain,
                service = service,
                data = data
            )
        )

        requestIds[id] = RequestType.CALL_SERVICE
        return id
    }

    suspend fun callServiceWithResponse(domain: String, service: String, data: Map<String, JsonElement> = emptyMap(), timeout: Duration = 1.seconds) : ContextResponse {
        val id = nextId
        requestIds[id] = RequestType.PENDING_REQUEST
        val deferred = CompletableDeferred<ContextResponse>()
        pendingRequests[id] = deferred

        request(
            CallServiceRequest(
                callId = id,
                domain = domain,
                service = service,
                data = data,
                returnResponse = true
            )
        )

        try {
            return withTimeout(timeout) {
                deferred.await()
            }
        } finally {
            pendingRequests.remove(id)
            requestIds.remove(id)
            if (deferred.isActive) {
                deferred.cancel()
            }
        }
    }

    suspend fun deleteAutomationConfig(id: String) {
        val response = session.call.client.delete("api/config/automation/config/$id")

        if (response.status != HttpStatusCode.OK) {
            error("Automation config delete for $id failed: ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun getAutomationConfig(id: String): JsonObject? {
        val response = session.call.client.get("api/config/automation/config/$id")

        return when (response.status) {
            HttpStatusCode.OK -> json.decodeFromString<JsonObject>(response.bodyAsText())
            HttpStatusCode.NotFound -> null
            else -> error("Automation config request for $id failed: ${response.status}")
        }
    }

    suspend fun getDevices(): Int {
        val id = request(
            RegistryRequest(
                registryId = nextId,
                registryType = RegistryType.DEVICE
            )
        )
        requestIds[id] = RequestType.DEVICE_LIST
        return id
    }

    suspend fun getEntities(): Int {
        val id = request(
            RegistryRequest(
                registryId = nextId,
                registryType = RegistryType.ENTITY
            )
        )
        requestIds[id] = RequestType.ENTITY_LIST
        return id
    }

    suspend fun getStates(): Int {
        val id = request(
            Request(
                id = nextId,
                type = "get_states"
            )
        )

        requestIds[id] = RequestType.GET_STATES
        return id
    }

    suspend fun saveAutomationConfig(id: String, config: JsonObject) {
        val response = session.call.client.post("api/config/automation/config/$id") {
            contentType(ContentType.Application.Json)
            setBody(config.toString())
        }

        if (response.status != HttpStatusCode.OK) {
            error("Automation config save for $id failed: ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun subscribeEvents(eventType: EventType): Int {
        val id = request(
            SubscribeEventRequest(
                subscriptionEventId = nextId,
                eventType = eventType
            )
        )

        requestIds[id] = RequestType.SUBSCRIBE
        return id
    }

    internal suspend fun auth(token: String) {
        session.sendSerialized(AuthRequest(token = token))
    }

    internal fun getRequestType(id: Int) = requestIds.remove(id)

    internal fun onPendingResult(id: Int, result: ContextResponse) =
        pendingRequests.remove(id)?.complete(result) ?: false


    private suspend inline fun <reified T : Request> request(request: T): Int {
        session.sendSerialized(request)
        return request.id
    }
}