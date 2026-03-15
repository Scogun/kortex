package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class Select(stateFlow: StateFlow<State>, context: KortexContext) : Entity<SelectAttributes>(stateFlow, context) {
    override val attributesFlow = mapAttributes<SelectAttributes>()
}

@Serializable
open class SelectAttributes(
    val options: List<String>
): StateAttributes()

