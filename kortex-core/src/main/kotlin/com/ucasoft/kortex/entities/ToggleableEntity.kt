package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.JsonPrimitive

abstract class ToggleableEntity<T : StateAttributes>(
    stateFlow: StateFlow<State>,
    context: KortexContext
) : Entity<T>(stateFlow, context) {

    val isOn
        get() = stateFlow.value.state == "on"

    suspend fun turnOn() = context.callService(domain, "turn_on", mapOf("entity_id" to JsonPrimitive(entityId)))

    suspend fun turnOff() = context.callService(domain, "turn_off", mapOf("entity_id" to JsonPrimitive(entityId)))

    suspend fun toggle() = context.callService(domain, "toggle", mapOf("entity_id" to JsonPrimitive(entityId)))

    fun onToggled(
        block: suspend ToggleableEntity<T>.() -> Unit
    ) = onToggled(context.scope, block)

    fun onToggled(
        scope: CoroutineScope,
        block: suspend ToggleableEntity<T>.() -> Unit
    ) = observe {
        it.state
    }.drop(1).onEach { block() }.launchIn(scope)
}