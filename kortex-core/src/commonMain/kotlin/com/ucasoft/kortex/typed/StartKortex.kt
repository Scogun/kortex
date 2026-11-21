package com.ucasoft.kortex.typed

import com.ucasoft.kortex.client.websocket.KortexContext
import com.ucasoft.kortex.startKortex

suspend fun startKortex(
    onAuthenticated: suspend (context: KortexContext) -> Unit,
    onResult: suspend (requestId: Int, result: Any) -> Unit = { _, _ -> },
    eventListener: EventListener,
) {
    startKortex(onAuthenticated, onResult) { event ->
        eventListener.dispatch(event)
    }
}