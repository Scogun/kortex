package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import com.ucasoft.kortex.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Timer(stateFlow: StateFlow<State>, context: KortexContext) : Entity<TimerAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<TimerAttributes>()

    val isActive
        get() = stateFlow.value.state == "active"

    val isPaused
        get() = stateFlow.value.state == "paused"

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun start(duration: Duration? = null) = context.callService(
        domain, "start",
        mutableMapOf("entity_id" to JsonPrimitive(entityId)).apply {
            duration?.let { put("duration", JsonUnquotedLiteral(json.encodeToString(DurationSerializer, it))) }
        }
    )

    suspend fun pause() = context.callService(domain, "pause", mapOf("entity_id" to JsonPrimitive(entityId)))

    fun onStateChange(
        block: suspend Timer.() -> Unit
    ) = onStateChange(context.scope, block)

    fun onStateChange(
        scope: CoroutineScope,
        block: suspend Timer.() -> Unit
    ) = observe {
        it.state
    }.drop(1).onEach { block() }.launchIn(scope)
}

@Serializable
open class TimerAttributes(
    @Serializable(with = DurationSerializer::class)
    val duration: Duration,
    val editable: Boolean
): StateAttributes()

object DurationSerializer : KSerializer<Duration> {
    override val descriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Duration) {
        val whole = value.inWholeSeconds
        val hours = whole / 3600
        val minutes = (whole % 3600) / 60
        val seconds = whole % 60
        encoder.encodeString("%02d:%02d:%02d".format(hours, minutes, seconds))
    }

    override fun deserialize(decoder: Decoder): Duration {
        val (h, m, s) = decoder.decodeString().split(":").map { it.toInt() }
        return (h * 3600 + m * 60 + s).seconds
    }

}