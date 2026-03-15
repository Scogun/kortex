package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class Switch(stateFlow: StateFlow<State>, context: KortexContext): ToggleableEntity<SwitchAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<SwitchAttributes>()
}

@Serializable
open class SwitchAttributes: StateAttributes()