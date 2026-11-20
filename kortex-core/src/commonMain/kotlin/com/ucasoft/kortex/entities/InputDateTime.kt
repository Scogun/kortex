package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.websocket.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

class InputDateTime(stateFlow: StateFlow<State>, context: KortexContext): EntityState<InputDateTimeAttributes>(stateFlow, context) {
    override val attributesFlow = mapAttributes<InputDateTimeAttributes>()

    suspend fun setDateTime(
        date: String? = null,
        time: String? = null,
        datetime: String? = null,
        timestamp: Double? = null
    ) {
        require(date != null || time != null || datetime != null || timestamp != null) {
            "At least one of date, time, datetime, or timestamp must be provided"
        }
        require(datetime == null || (date == null && time == null && timestamp == null)) {
            "datetime cannot be combined with date, time, or timestamp"
        }
        require(timestamp == null || (date == null && time == null)) {
            "timestamp cannot be combined with date or time"
        }

        callService(
            "set_datetime",
            listOfNotNull(
                date?.let { "date" to JsonPrimitive(it) },
                time?.let { "time" to JsonPrimitive(it) },
                datetime?.let { "datetime" to JsonPrimitive(it) },
                timestamp?.let { "timestamp" to JsonPrimitive(it) }
            ).toMap()
        )
    }
}

@Serializable
class InputDateTimeAttributes: StateAttributes() {
    val day: Byte? = null
    val second: Byte? = null
    @SerialName("has_date")
    val hasDate: Boolean? = null
    @SerialName("has_time")
    val hasTime: Boolean? = null
    val hour: Byte? = null
    val minute: Byte? = null
    val month: Byte? = null
    val year: Short? = null
}
