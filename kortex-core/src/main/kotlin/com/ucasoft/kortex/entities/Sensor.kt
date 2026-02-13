package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Sensor(stateFlow: StateFlow<State>, context: KortexContext): Entity<SensorAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<SensorAttributes>()
}

@Serializable
open class SensorAttributes: StateAttributes() {
    @SerialName("device_class")
    val deviceClass: String? = null
    @SerialName("state_class")
    val stateClass: String? = null
    @SerialName("unit_of_measurement")
    val unit: String? = null
}