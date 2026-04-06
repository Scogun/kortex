package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class BinarySensor(stateFlow: StateFlow<State>, context: KortexContext): EntityState<BinarySensorAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<BinarySensorAttributes>()

    val isOn
        get() = if (state == "unavailable") null else state == "on"
}

@Serializable
open class BinarySensorAttributes: DeviceClassAttributes<BinarySensorDeviceClass>() {
}

enum class BinarySensorDeviceClass {
    BATTERY,
    BATTERY_CHARGING,
    CO,
    COLD,
    CONNECTIVITY,
    DOOR,
    GARAGE_DOOR,
    GAS,
    HEAT,
    LIGHT,
    LOCK,
    MOISTURE,
    MOTION,
    MOVING,
    OCCUPANCY,
    OPENING,
    PLUG,
    POWER,
    PRESENCE,
    PROBLEM,
    RUNNING,
    SAFETY,
    SMOKE,
    SOUND,
    TAMPER,
    UPDATE,
    VIBRATION,
    WINDOW
}