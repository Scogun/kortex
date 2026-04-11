package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable

class Button(stateFlow: StateFlow<State>, context: KortexContext): PressableEntity<ButtonAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<ButtonAttributes>()
}

@Serializable
open class ButtonAttributes: DeviceClassAttributes<ButtonDeviceClass>()

enum class ButtonDeviceClass {
    IDENTIFY,
    RESTART,
    UPDATE
}