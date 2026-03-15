package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Climate(stateFlow: StateFlow<State>, context: KortexContext) : ToggleableEntity<ClimateAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<ClimateAttributes>()

    val isTargetTemperatureSupported = (attributes.supportedFeatures and TARGET_TEMPERATURE) != 0
    val isTargetTemperatureRangeSupported = (attributes.supportedFeatures and TARGET_TEMPERATURE_RANGE) != 0
    val isTargetHumiditySupported = (attributes.supportedFeatures and TARGET_HUMIDITY) != 0
    val isFanModeSupported = (attributes.supportedFeatures and FAN_MODE) != 0
    val isPresetModeSupported = (attributes.supportedFeatures and PRESET_MODE) != 0
    val isSwingModeSupported = (attributes.supportedFeatures and SWING_MODE) != 0
    val isTurnOffSupported = (attributes.supportedFeatures and TURN_OFF) != 0
    val isTurnOnSupported = (attributes.supportedFeatures and TURN_ON) != 0
    val isSwingHorizontalModeSupported = (attributes.supportedFeatures and SWING_HORIZONTAL_MODE) != 0

    private companion object {
        const val TARGET_TEMPERATURE = 1
        const val TARGET_TEMPERATURE_RANGE = 2
        const val TARGET_HUMIDITY = 4
        const val FAN_MODE = 8
        const val PRESET_MODE = 16
        const val SWING_MODE = 32
        const val TURN_OFF = 128
        const val TURN_ON = 256
        const val SWING_HORIZONTAL_MODE = 512
    }
}

@Serializable
open class ClimateAttributes(
    @SerialName("hvac_modes")
    val hvacModes: List<HVACMode>,
) : FeaturedAttributes()

enum class HVACMode {
    OFF,
    HEAT,
    COOL,
    HEAT_COOL,
    AUTO,
    DRY,
    FAN_ONLY
}