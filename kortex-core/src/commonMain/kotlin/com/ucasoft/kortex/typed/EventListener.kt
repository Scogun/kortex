package com.ucasoft.kortex.typed

import com.ucasoft.kortex.entities.Event

/** Receives all HA events and, when recognized, their typed callbacks as well. */
interface EventListener {
    suspend fun onEvent(event: Event) {}

    suspend fun onStateChanged(event: StateChangedEvent) {}

    suspend fun onAutomationTriggered(event: AutomationTriggeredEvent) {}
}

internal suspend fun EventListener.dispatch(event: Event) {
    onEvent(event)

    val data = event.data ?: return
    when (event.type) {
        "state_changed" -> onStateChanged(
            StateChangedEvent(
                event = event,
                entityId = data.entityId,
                oldState = data.oldState,
                newState = data.newState,
            )
        )

        "automation_triggered" -> onAutomationTriggered(
            AutomationTriggeredEvent(
                event = event,
                entityId = data.entityId,
                itemId = data.itemId!!,
                runId = data.runId!!,
            )
        )
    }
}
