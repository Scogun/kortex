package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

class Sensor(stateFlow: StateFlow<State>, context: KortexContext): EntityState<SensorAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<SensorAttributes>()

    inline fun <reified T> getStateAs(): T? {
        val rawValue = state.takeIf { it.isNotBlank() }.takeIf { it != "unknown" && it != "unavailable" } ?: return null

        return when (T::class) {
            String::class -> rawValue
            Boolean::class -> rawValue.toBooleanStrictOrNull()
            Int::class -> rawValue.toIntOrNull()
            Long::class -> rawValue.toLongOrNull()
            Double::class -> rawValue.toDoubleOrNull()
            Instant::class -> runCatching { Instant.parse(rawValue) }.getOrNull()
            else -> null
        } as T?
    }
}

@Serializable
open class SensorAttributes: DeviceClassAttributes<SensorDeviceClass>() {
    val options: List<String>? = null
    @SerialName("state_class")
    val stateClass: SensorStateClass? = null
    @SerialName("unit_of_measurement")
    val unit: String? = null
}

enum class SensorDeviceClass {
    ABSOLUTE_HUMIDITY,
    APPARENT_POWER,
    AQI,
    AREA,
    ATMOSPHERIC_PRESSURE,
    BATTERY,
    BLOOD_GLUCOSE_CONCENTRATION,
    CARBON_DIOXIDE,
    CARBON_MONOXIDE,
    CONDUCTIVITY,
    CURRENT,
    DATA_RATE,
    DATA_SIZE,
    DATE,
    DISTANCE,
    DURATION,
    ENERGY,
    ENERGY_DISTANCE,
    ENERGY_STORAGE,
    ENUM,
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
    TIMESTAMP,
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

enum class SensorStateClass {
    MEASUREMENT,
    MEASUREMENT_ANGLE,
    TOTAL,
    TOTAL_INCREASING
}