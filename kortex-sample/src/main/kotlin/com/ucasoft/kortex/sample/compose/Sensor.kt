package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ucasoft.kortex.compose.extensions.collectAsState
import com.ucasoft.kortex.entities.Sensor
import com.ucasoft.kortex.entities.SensorDeviceClass
import kotlin.time.Instant

@Composable
internal fun Sensors(sensors: List<Sensor>) {
    EntityList(sensors) {
        Sensor(it)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun RowScope.Sensor(sensor: Sensor) {
    val state by sensor.collectAsState()
    Column(
        modifier = Modifier.weight(1f)
    ) {
        when (state.attributes.deviceClass) {
            SensorDeviceClass.BATTERY -> "${state.getStateAs<Int>()} ${state.attributes.unit}"
            SensorDeviceClass.HUMIDITY, SensorDeviceClass.TEMPERATURE, SensorDeviceClass.VOLTAGE  -> "${state.getStateAs<Double>()} ${state.attributes.unit}"
            SensorDeviceClass.TIMESTAMP -> state.getStateAs<Instant>().toString()
            else -> state.getStateAs<String>()
        }?.let {
            EntityState(it)
        }
        EntityAttributes(state.attributes)
    }
}