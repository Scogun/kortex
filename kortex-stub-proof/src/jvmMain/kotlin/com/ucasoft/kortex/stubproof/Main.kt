package com.ucasoft.kortex.stubproof

import com.ucasoft.kortex.client.KortexContext
import com.ucasoft.kortex.startKortex
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Phase 0 stub proof (AGENTS.md §7). A throwaway program, not the Runtime.
 */
suspend fun main() = coroutineScope {
    val authenticated = CompletableDeferred<KortexContext>()

    // startKortex returns only when the connection ends, so the scenario runs beside it.
    val connection = launch {
        startKortex(
            onAuthenticated = { authenticated.complete(it) },
            onEvent = {
                println("Event received: $it")
            }
        )
        error("Home Assistant closed the connection")
    }

    try {
        StubProof(authenticated.await()).run()
    } finally {
        connection.cancel()
    }
}
