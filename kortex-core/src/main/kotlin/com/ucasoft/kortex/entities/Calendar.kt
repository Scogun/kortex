package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import com.ucasoft.kortex.json
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class Calendar(stateFlow: StateFlow<State>, context: KortexContext) : Entity<CalendarAttributes>(stateFlow, context) {

    val isEventHappening
        get() = stateFlow.value.state == "on"

    override val attributes: CalendarAttributes
        get() = stateFlow.value.getAttributeAs<CalendarAttributes>()

    val isCreateEventSupported = ((attributes.supportedFeatures ?: 0) and CREATE_EVENT) != 0
    val isDeleteEventSupported = ((attributes.supportedFeatures ?: 0) and DELETE_EVENT) != 0
    val isUpdateEventSupported = ((attributes.supportedFeatures ?: 0) and UPDATE_EVENT) != 0

    suspend fun getEvents(start: Instant = Clock.System.now(), end: Instant = start.plus(1.days)): List<CalendarEvent> {
        val events = context.callServiceWithResponse(
            domain, "get_events", mapOf(
                "entity_id" to JsonPrimitive(entityId),
                "start_date_time" to JsonPrimitive(start.toString()),
                "end_date_time" to JsonPrimitive(end.toString())
            )
        )

        return json.decodeFromJsonElement<List<CalendarEvent>>(events.response[entityId]!!.jsonObject["events"]!!)
    }

    private companion object {
        const val CREATE_EVENT = 1
        const val DELETE_EVENT = 2
        const val UPDATE_EVENT = 4
    }
}

@Serializable
class CalendarAttributes(
    @SerialName("all_day")
    val allDay: Boolean? = null,
    val description: String? = null,
    @SerialName("end_time")
    @Serializable(with = InstantSerializer::class)
    val endTime: Instant? = null,
    val location: String? = null,
    @SerialName("offset_reached")
    val offsetReached: Boolean? = null,
    val message: String? = null,
    @SerialName("start_time")
    @Serializable(with = InstantSerializer::class)
    val startTime: Instant? = null,
    @SerialName("supported_features")
    val supportedFeatures: Int? = null
) : StateAttributes()


object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(Instant::class.simpleName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val input = decoder.decodeString()
        val isoInput = when {
            input.length == 10 -> "${input}T00:00:00Z"
            input.contains(" ") -> input.replace(" ", "T") + "Z"
            input.contains("T") -> if (input.endsWith("Z")) input else "${input}Z"
            else -> input
        }
        return Instant.parse(isoInput)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

@Serializable
data class CalendarEvent(
    val description: String? = null,
    @Serializable(with = InstantSerializer::class)
    val end: Instant,
    val location: String? = null,
    @Serializable(with = InstantSerializer::class)
    val start: Instant,
    val summary: String
)