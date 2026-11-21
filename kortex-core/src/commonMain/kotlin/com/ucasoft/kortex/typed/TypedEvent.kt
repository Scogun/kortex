package com.ucasoft.kortex.typed

import com.ucasoft.kortex.entities.Event
import com.ucasoft.kortex.entities.State


interface TypedEvent {
    val event: Event
    val entityId: String
}

data class StateChangedEvent(
    override val event: Event,
    override val entityId: String,
    val oldState: State?,
    val newState: State?
): TypedEvent

data class AutomationTriggeredEvent(
    override val event: Event,
    override val entityId: String,
    val itemId: String,
    val runId: String
) : TypedEvent
