package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.ucasoft.kortex.compose.extensions.collectAsState
import com.ucasoft.kortex.entities.InputDateTime
import kotlinx.coroutines.launch
import kotlin.time.Instant

@Composable
internal fun DateTimeList(dateTimes: List<InputDateTime>) {
    EntityList(
        dateTimes,
        trailingContent = {
            DateTimePicker(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(dateTime: InputDateTime) {

    val state by dateTime.collectAsState()
    val hasDate = dateTime.attributes.hasDate == true
    val hasTime = dateTime.attributes.hasTime == true
    val currentDate = state.state.substringBefore(' ').takeIf { hasDate }
    val currentHour = dateTime.attributes.hour?.toInt() ?: 0
    val currentMinute = dateTime.attributes.minute?.toInt() ?: 0
    val currentSecond = dateTime.attributes.second?.toInt() ?: 0
    val currentTime = if (hasTime) {
        "${currentHour.toString().padStart(2, '0')}:" +
            "${currentMinute.toString().padStart(2, '0')}:" +
            currentSecond.toString().padStart(2, '0')
    } else {
        null
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.toDatePickerMillis()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = currentHour.coerceIn(0, 23),
        initialMinute = currentMinute.coerceIn(0, 59),
        is24Hour = true
    )
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row {
        if (hasDate) {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Rounded.DateRange, contentDescription = "Set date")
            }
        }
        if (hasTime) {
            IconButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Rounded.AccessTime, contentDescription = "Set time")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis?.toDateString()
                        showDatePicker = false
                        if (selectedDate != null) {
                            scope.launch {
                                dateTime.setDateTime(
                                    date = selectedDate,
                                    time = currentTime
                                )
                            }
                        }
                    }
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedTime = "${timePickerState.hour.toString().padStart(2, '0')}:" +
                            "${timePickerState.minute.toString().padStart(2, '0')}:00"
                        showTimePicker = false
                        scope.launch {
                            dateTime.setDateTime(
                                date = currentDate,
                                time = selectedTime
                            )
                        }
                    }
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun String?.toDatePickerMillis(): Long? = this?.let { date ->
    runCatching { Instant.parse("${date}T00:00:00Z").toEpochMilliseconds() }.getOrNull()
}

private fun Long.toDateString(): String =
    Instant.fromEpochMilliseconds(this).toString().substringBefore('T')
