package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class Zone(stateFlow: StateFlow<State>, context: KortexContext): Entity<ZoneAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<ZoneAttributes>()
}

@Serializable
data class ZoneAttributes(
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val passive: Boolean,
    val persons: List<String>,
    val editable: Boolean
): StateAttributes()