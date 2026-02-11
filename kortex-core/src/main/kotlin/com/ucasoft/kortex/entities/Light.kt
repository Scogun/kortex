package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Light(stateFlow: StateFlow<State>, context: KortexContext): ToggleableEntity<LightAttributes>(stateFlow, context) {

    override val attributes
        get() = stateFlow.value.getAttributeAs<LightAttributes>()

    val isBrightnessSupported = (attributes.supportedFeatures and SUPPORT_BRIGHTNESS) != 0
    val isColorTempSupported = (attributes.supportedFeatures and SUPPORT_COLOR_TEMP) != 0
    val isEffectSupported = (attributes.supportedFeatures and SUPPORT_EFFECT) != 0
    val isFlashSupported = (attributes.supportedFeatures and SUPPORT_FLASH) != 0
    val isColorSupported = (attributes.supportedFeatures and SUPPORT_COLOR) != 0
    val isTransitionSupported = (attributes.supportedFeatures and SUPPORT_TRANSITION) != 0
    val isWhiteValueSupported = (attributes.supportedFeatures and SUPPORT_WHITE_VALUE) != 0

    private companion object {
        const val SUPPORT_BRIGHTNESS = 1
        const val SUPPORT_COLOR_TEMP = 2
        const val SUPPORT_EFFECT = 4
        const val SUPPORT_FLASH = 8
        const val SUPPORT_COLOR = 16
        const val SUPPORT_TRANSITION = 32
        const val SUPPORT_WHITE_VALUE = 64
    }
}

@Serializable
open class LightAttributes(
    @SerialName("color_mode")
    val colorMode: String? = null,
    @SerialName("supported_color_modes")
    val supportedColorModes: List<String>,
    @SerialName("supported_features")
    val supportedFeatures: Int
): StateAttributes()