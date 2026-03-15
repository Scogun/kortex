package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Number(stateFlow: StateFlow<State>, context: KortexContext): Entity<NumberAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<NumberAttributes>()
}

@Serializable
open class NumberAttributes : DeviceClassAttributes<NumberDeviceClass>() {

    val max: Float = 100f
    val min: Float = 0f
    @SerialName("step")
    private val providedStep: Float? = null
    val step: Float
        get() = providedStep ?: calculateStep()
    val mode: String = "auto"
    @SerialName("unit_of_measurement")
    val unit: String? = null

    private fun calculateStep(): Float {
        val range = max - min

        if (range > 1f) return 1f

        var step = 1f
        while (step >= range) {
            step /= 10f
        }

        return step
    }
}

enum class NumberDeviceClass {
    ABSOLUTE_HUMIDITY,
    APPARENT_POWER,
    AQI,
    AREA,
    ATMOSPHERIC_PRESSURE,
    BATTERY,
    BLOOD_GLUCOSE_CONCENTRATION,
    CO2,
    CO,
    CONDUCTIVITY,
    CURRENT,
    DATA_RATE,
    DATA_SIZE,
    DISTANCE,
    DURATION,
    ENERGY,
    ENERGY_DISTANCE,
    ENERGY_STORAGE,
    FREQUENCY,
    GAS,
    HUMIDITY,
    ILLUMINANCE,
    IRRADIANCE,
    MOISTURE,
    MONETARY,
    NITROGEN_DIOXIDE,
    NITROGEN_MONOXIDE,
    NITROUS_OXIDE,
    OZONE,
    PH,
    PM1,
    PM25,
    PM4,
    PM10,
    POWER,
    POWER_FACTOR,
    PRECIPITATION,
    PRECIPITATION_INTENSITY,
    PRESSURE,
    REACTIVE_ENERGY,
    REACTIVE_POWER,
    SIGNAL_STRENGTH,
    SOUND_PRESSURE,
    SPEED,
    SULPHUR_DIOXIDE,
    TEMPERATURE,
    TEMPERATURE_DELTA,
    VOLATILE_ORGANIC_COMPOUNDS,
    VOLATILE_ORGANIC_COMPOUNDS_PARTS,
    VOLTAGE,
    VOLUME,
    VOLUME_FLOW_RATE,
    VOLUME_STORAGE,
    WATER,
    WEIGHT,
    WIND_DIRECTION,
    WIND_SPEED
}