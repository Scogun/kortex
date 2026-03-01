package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class Light(stateFlow: StateFlow<State>, context: KortexContext): ToggleableEntity<LightAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<LightAttributes>()

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
    val brightness: Int? = null,
    @SerialName("color_mode")
    val colorMode: ColorMode? = null,
    @SerialName("color_temp_kelvin")
    val colorTempKelvin: Int? = null,
    val effect: String? = null,
    @SerialName("effect_list")
    val effectList: List<String>? = null,
    @SerialName("hs_color")
    val hsColor: HsColor? = null,
    @SerialName("max_color_temp_kelvin")
    val maxColorTempKelvin: Int? = null,
    @SerialName("min_color_temp_kelvin")
    val minColorTempKelvin: Int? = null,
    @SerialName("rgb_color")
    val rgbColor: RgbColor? = null,
    @SerialName("rgbw_color")
    val rgbwColor: RgbwColor? = null,
    @SerialName("rgbww_color")
    val rgbwwColor: RgbwwColor? = null,
    @SerialName("supported_color_modes")
    val supportedColorModes: List<String>,
    @SerialName("xy_color")
    val xyColor: XyColor? = null
): FeaturedAttributes()

enum class ColorMode {
    UNKNOWN,
    ONOFF,
    BRIGHTNESS,
    COLOR_TEMP,
    HS,
    RGB,
    RGBW,
    RGBWW,
    WHITE,
    XY
}

@Serializable(with = ColorSerializers.HS::class)
data class HsColor(val hue: Float, val saturation: Float)

@Serializable(with = ColorSerializers.RGB::class)
data class RgbColor(val red: Int, val green: Int, val blue: Int)

@Serializable(with = ColorSerializers.RGBW::class)
data class RgbwColor(val red: Int, val green: Int, val blue: Int, val white: Int)

@Serializable(with = ColorSerializers.RGBWW::class)
data class RgbwwColor(val red: Int, val green: Int, val blue: Int, val coldWhite: Int, val warmWhite: Int)

@Serializable(with = ColorSerializers.XY::class)
data class XyColor(val x: Float, val y: Float)

object ColorSerializers {
    // Шаблон для десериализации массива в объект
    abstract class ArraySerializer<T, V>(elementSerializer: KSerializer<V>) : KSerializer<T> {
        protected val delegate = ListSerializer(elementSerializer)
        override val descriptor = delegate.descriptor
    }

    object HS : ArraySerializer<HsColor, Float>(Float.serializer()) {
        override fun serialize(encoder: Encoder, value: HsColor) = encoder.encodeSerializableValue(delegate, listOf(value.hue, value.saturation))
        override fun deserialize(decoder: Decoder) = delegate.deserialize(decoder).let { HsColor(it[0], it[1]) }
    }

    object RGB : ArraySerializer<RgbColor, Int>(Int.serializer()) {
        override fun serialize(encoder: Encoder, value: RgbColor) = encoder.encodeSerializableValue(delegate, listOf(value.red, value.green, value.blue))
        override fun deserialize(decoder: Decoder) = delegate.deserialize(decoder).let { RgbColor(it[0], it[1], it[2]) }
    }

    object RGBW : ArraySerializer<RgbwColor, Int>(Int.serializer()) {
        override fun serialize(encoder: Encoder, value: RgbwColor) = encoder.encodeSerializableValue(delegate, listOf(value.red, value.green, value.blue, value.white))
        override fun deserialize(decoder: Decoder) = delegate.deserialize(decoder).let { RgbwColor(it[0], it[1], it[2], it[3]) }
    }

    object RGBWW : ArraySerializer<RgbwwColor, Int>(Int.serializer()) {
        override fun serialize(encoder: Encoder, value: RgbwwColor) = encoder.encodeSerializableValue(delegate, listOf(value.red, value.green, value.blue, value.coldWhite, value.warmWhite))
        override fun deserialize(decoder: Decoder) = delegate.deserialize(decoder).let { RgbwwColor(it[0], it[1], it[2], it[3], it[4]) }
    }

    object XY : ArraySerializer<XyColor, Float>(Float.serializer()) {
        override fun serialize(encoder: Encoder, value: XyColor) = encoder.encodeSerializableValue(delegate, listOf(value.x, value.y))
        override fun deserialize(decoder: Decoder) = delegate.deserialize(decoder).let { XyColor(it[0], it[1]) }
    }
}