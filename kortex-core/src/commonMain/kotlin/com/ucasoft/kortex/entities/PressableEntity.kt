package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class PressableEntity<T: StateAttributes>(
    stateFlow: StateFlow<State>,
    context: KortexContext
): EntityState<T>(stateFlow, context) {

    suspend fun press() = callService("press")

    fun onPressed(
        block: suspend PressableEntity<T>.() -> Unit
    ) = onPressed(context.scope, block)

    fun onPressed(
        scope: CoroutineScope,
        block: suspend PressableEntity<T>.() -> Unit
    ) = observe {
        it.state
    }.drop(1).onEach { block() }.launchIn(scope)
}