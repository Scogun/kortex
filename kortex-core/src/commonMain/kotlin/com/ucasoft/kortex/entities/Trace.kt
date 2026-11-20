package com.ucasoft.kortex.entities

import com.ucasoft.kortex.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class Trace(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("run_id")
    val runId: String,
    val trace: Map<String, List<TraceStep>>
) {
    // HA records the trigger as the step whose path is "trigger/<index>", or "trigger" for a manual run.
    val rawTrigger: JsonObject?
        get() = trace.entries
            .firstOrNull { it.key.substringBefore('/') == "trigger" }
            ?.value?.firstOrNull()
            ?.changedVariables?.get("trigger") as? JsonObject

    val trigger: TraceTrigger? by lazy {
        rawTrigger?.let { json.decodeFromJsonElement<TraceTrigger>(it) }
    }

    val triggerId: String?
        get() = trigger?.id
}

@Serializable
data class TraceStep(
    val path: String,
    @SerialName("changed_variables")
    val changedVariables: JsonObject? = null
)

@Serializable
data class TraceTrigger(
    val id: String? = null,
    val idx: String? = null,
    val alias: String? = null,
    val platform: String? = null,
    val description: String? = null,
    @SerialName("entity_id")
    val entityId: String? = null,
    @SerialName("from_state")
    val fromState: State? = null,
    @SerialName("to_state")
    val toState: State? = null
)
