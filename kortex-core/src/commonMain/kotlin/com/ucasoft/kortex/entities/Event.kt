package com.ucasoft.kortex.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Event(
    @SerialName("event_type")
    val type: String,
    val data: EventData? = null,
    @SerialName("time_fired")
    val timeFired: Instant,
)

@Serializable
data class EventData(
    @SerialName("entity_id")
    val entityId: String,
    @SerialName("old_state")
    val oldState: State? = null,
    @SerialName("new_state")
    val newState: State? = null,
    @SerialName("item_id")
    val itemId: String? = null,
    @SerialName("run_id")
    val runId: String? = null,
)