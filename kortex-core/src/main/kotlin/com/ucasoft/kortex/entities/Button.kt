package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class Button(stateFlow: StateFlow<State>, context: KortexContext): Entity<ButtonAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<ButtonAttributes>()
}

@Serializable
open class ButtonAttributes: StateAttributes()