package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class DeviceTracker(stateFlow: StateFlow<State>, context: KortexContext): Entity<DeviceTrackerAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<DeviceTrackerAttributes>()
}

@Serializable
data class DeviceTrackerAttributes(
    @SerialName("source_type")
    val sourceType: String
): StateAttributes()