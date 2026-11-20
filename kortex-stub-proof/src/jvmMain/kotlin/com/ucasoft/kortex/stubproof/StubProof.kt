package com.ucasoft.kortex.stubproof

import com.ucasoft.kortex.client.rest.RestKortexClient
import com.ucasoft.kortex.client.websocket.KortexContext
import com.ucasoft.kortex.client.websocket.requests.EventType
import com.ucasoft.kortex.entities.Trace
import com.ucasoft.kortex.startKortex
import kotlinx.serialization.json.*
import kotlin.system.exitProcess

/**
 * The Phase 0 scenario. A run deletes the stub and exits if the stub exists. Otherwise it creates
 * the stub and runs the Kotlin body each time HA reports a run of the stub, until stopped.
 */
class StubProof {

    private val client = RestKortexClient()

    private val stubId = "kortex_proof_stub"

    private lateinit var context: KortexContext

    suspend fun run() {
        startKortex(
            onAuthenticated = { context ->
                this.context = context
                if (!stubExists()) {
                    createStub()
                } else {
                    //deleteStub()
                }
                context.subscribeEvents(EventType.AUTOMATION_TRIGGERED)
                println("Listening for runs of $stubId. Run the stub in HA, and stop this run when done.")
            },
            onResult = { _, result ->
                if (result is Trace) {
                    automationBody(result)
                }
            },
            onEvent = { event ->
                val runId = event.data.runId
                if (event.data.itemId != null && runId != null) {
                    context.getTrace(event.data.itemId!!, runId)
                }
            }
        )
    }

    private suspend fun stubExists() = client.automations.getConfig(stubId) != null

    private suspend fun createStub() {
        client.automations.saveConfig(stubId, buildJsonObject {
            put("alias", "Kortex proof stub")
            put("description", "Stub of the Kortex Phase 0 proof. Safe to delete.")
            putJsonArray("triggers") {
                addJsonObject {
                    put("trigger", "event")
                    put("event_type", "kortex_proof_trigger")
                    put("id", "first")
                }
                addJsonObject {
                    put("trigger", "event")
                    put("event_type", "kortex_proof_other_trigger")
                    put("id", "second")
                }
            }
            putJsonArray("conditions") { }
            putJsonArray("actions") { }
            put("mode", "single")
        })

        println("Created stub $stubId: ${client.automations.getConfig(stubId)}")
    }

    private suspend fun deleteStub() {
        client.automations.deleteConfig(stubId)
        check(!stubExists()) { "Stub $stubId is still there after the delete" }

        println("Deleted stub $stubId")
        exitProcess(0)
    }

    // Stands in for the Kotlin code of the automation the stub belongs to.
    private fun automationBody(trace: Trace) {
        val trigger = trace.trigger
        println("Kotlin body runs for ${trace.itemId}, trigger ${trigger?.id}, HA run ${trace.runId}")
        println("  ${trigger?.platform} on ${trigger?.entityId}: ${trigger?.fromState?.state} -> ${trigger?.toState?.state}")
    }
}
