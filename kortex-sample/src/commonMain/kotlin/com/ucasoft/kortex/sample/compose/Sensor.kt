package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ucasoft.kortex.compose.extensions.collectAsState
import com.ucasoft.kortex.entities.Sensor
import com.ucasoft.kortex.entities.SensorDeviceClass
import com.ucasoft.kortex.entities.SensorStateClass
import kotlin.time.Instant

@Composable
internal fun Sensors(sensors: List<Sensor>) {
    EntityList(sensors) {
        Sensor(it)
    }
}

@Composable
private fun RowScope.Sensor(sensor: Sensor) {
    val state by sensor.collectAsState()
    Column(
        modifier = Modifier.weight(1f)
    ) {
        when (state.attributes.deviceClass) {
            SensorDeviceClass.BATTERY -> "${state.getStateAs<Int>()} ${state.attributes.unit}"
            SensorDeviceClass.TIMESTAMP -> state.getStateAs<Instant>().toString()
            else -> when (state.attributes.stateClass) {
                SensorStateClass.MEASUREMENT -> "${state.getStateAs<Double>()} ${state.attributes.unit}"
                else -> state.getStateAs<String>()
            }
        }?.let {
            EntityState(it)
        }
        EntityAttributes(state.attributes)
    }
}