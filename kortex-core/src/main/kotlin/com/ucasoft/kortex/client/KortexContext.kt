package com.ucasoft.kortex.client

import com.ucasoft.kortex.client.requests.AuthRequest
import com.ucasoft.kortex.client.requests.CallServiceRequest
import com.ucasoft.kortex.client.requests.EventType
import com.ucasoft.kortex.client.requests.Request
import com.ucasoft.kortex.client.requests.RequestType
import com.ucasoft.kortex.client.requests.SubscribeEventRequest
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonElement
import java.util.concurrent.atomic.AtomicInteger

class KortexContext(private val session: DefaultClientWebSocketSession, internal val scope: CoroutineScope) {

    private val id = AtomicInteger()

    private val nextId
        get() = id.incrementAndGet()

    private val requestIds = mutableMapOf<Int, RequestType>()

    suspend fun callService(domain: String, service: String, data: Map<String, JsonElement> = emptyMap(), returnResponse: Boolean? = null): Int {
        val id = request(
            CallServiceRequest(
                callId = nextId,
                domain = domain,
                service = service,
                data = data,
                returnResponse = returnResponse
            )
        )

        requestIds[id] = RequestType.CALL_SERVICE
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

    private suspend inline fun <reified T : Request> request(request: T): Int {
        session.sendSerialized(request)
        return request.id
    }
}