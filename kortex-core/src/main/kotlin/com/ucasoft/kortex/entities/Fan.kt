package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Fan(stateFlow: StateFlow<State>, context: KortexContext): ToggleableEntity<FanAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<FanAttributes>()

    val isSpeedSupported = (attributes.supportedFeatures and SET_SPEED) != 0
    val isOscillateSupported = (attributes.supportedFeatures and OSCILLATE) != 0
    val isDirectionSupported = (attributes.supportedFeatures and DIRECTION) != 0
    val isPresetModeSupported = (attributes.supportedFeatures and PRESET_MODE) != 0
    val isTurnOffSupported = (attributes.supportedFeatures and TURN_OFF) != 0
    val isTurnOnSupported = (attributes.supportedFeatures and TURN_ON) != 0


    private companion object {
        const val SET_SPEED = 1
        const val OSCILLATE = 2
        const val DIRECTION = 4
        const val PRESET_MODE = 8
        const val TURN_OFF = 16
        const val TURN_ON = 32
    }
}

@Serializable
open class FanAttributes(
    @SerialName("current_direction")
    val currentDirection: String? = null,
    val oscillating: Boolean? = null,
    val percentage: Int? = 0,
    @SerialName("preset_mode")
    val presetMode: String? = null,
    @SerialName("preset_modes")
    val presetModes: List<String>? = null,
    @SerialName("speed_count")
    val speedCount: Int = 100
): FeaturedAttributes()