package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.websocket.KortexContext
import kotlinx.coroutines.flow.StateFlow
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