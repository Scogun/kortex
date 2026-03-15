package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable

class Button(stateFlow: StateFlow<State>, context: KortexContext): Entity<ButtonAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<ButtonAttributes>()

    suspend fun press() = callService("press")

    fun onPressed(
        block: suspend Button.() -> Unit
    ) = onPressed(context.scope, block)

    fun onPressed(
        scope: CoroutineScope,
        block: suspend Button.() -> Unit
    ) = observe {
        it.state
    }.drop(1).onEach { block() }.launchIn(scope)
}

@Serializable
open class ButtonAttributes: DeviceClassAttributes<ButtonDeviceClass>()

enum class ButtonDeviceClass {
    IDENTIFY,
    RESTART,
    UPDATE
}