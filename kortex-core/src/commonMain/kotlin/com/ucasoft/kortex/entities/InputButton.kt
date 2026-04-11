package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow

class InputButton(stateFlow: StateFlow<State>, context: KortexContext): PressableEntity<StateAttributes>(stateFlow, context) {
    override val attributesFlow = mapAttributes<StateAttributes>()
}