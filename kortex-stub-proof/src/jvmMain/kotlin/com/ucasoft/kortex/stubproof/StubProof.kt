package com.ucasoft.kortex.stubproof

import com.ucasoft.kortex.client.KortexContext
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * The Phase 0 scenario. Each run creates the stub if it is missing and deletes it otherwise,
 * so the stub can be inspected in HA between two runs.
 */
class StubProof(private val context: KortexContext) {

    private val stubId = "kortex_proof_stub"

    suspend fun run() {
        if (stubExists()) deleteStub() else createStub()
    }

    private suspend fun stubExists() = context.getAutomationConfig(stubId) != null

    private suspend fun createStub() {
        context.saveAutomationConfig(stubId, buildJsonObject {
            put("alias", "Kortex proof stub")
            put("description", "Stub of the Kortex Phase 0 proof. Safe to delete.")
            putJsonArray("triggers") {
                addJsonObject {
                    put("trigger", "event")
                    put("event_type", "kortex_proof_trigger")
                }
            }
            putJsonArray("conditions") { }
            putJsonArray("actions") {
                addJsonObject {
                    put("event", "kortex_proof_hand_off")
                    putJsonObject("event_data") {
                        put("automation_id", stubId)
                    }
                }
            }
            put("mode", "single")
        })

        println("Created stub $stubId: ${context.getAutomationConfig(stubId)}")
    }

    private suspend fun deleteStub() {
        context.deleteAutomationConfig(stubId)
        check(!stubExists()) { "Stub $stubId is still there after the delete" }

        println("Deleted stub $stubId")
    }
}
