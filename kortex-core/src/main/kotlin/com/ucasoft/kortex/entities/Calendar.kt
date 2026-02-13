package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class Calendar(stateFlow: StateFlow<State>, context: KortexContext) : Entity<CalendarAttributes>(stateFlow, context) {

    override val attributes: CalendarAttributes
        get() = stateFlow.value.getAttributeAs<CalendarAttributes>()

    val isCreateEventSupported = ((attributes.supportedFeatures ?: 0) and CREATE_EVENT) != 0
    val isDeleteEventSupported = ((attributes.supportedFeatures ?: 0) and DELETE_EVENT) != 0
    val isUpdateEventSupported = ((attributes.supportedFeatures ?: 0) and UPDATE_EVENT) != 0

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
        val isoInput = input.replace(" ", "T") + "Z"
        return Instant.parse(isoInput)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}