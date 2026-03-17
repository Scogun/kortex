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
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Timer(stateFlow: StateFlow<State>, context: KortexContext) : Entity<TimerAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<TimerAttributes>()

    val isActive
        get() = state == "active"

    val isPaused
        get() = state == "paused"

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun start(duration: Duration? = null) = callService(
        "start",
        duration?.let { mapOf("duration" to JsonUnquotedLiteral(json.encodeToString(DurationSerializer, it))) }
            ?: emptyMap())

    suspend fun pause() = callService("pause")

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
        val hours = (whole / 3600).toString().padStart(2, '0')
        val minutes = ((whole % 3600) / 60).toString().padStart(2, '0')
        val seconds = (whole % 60).toString().padStart(2, '0')
        encoder.encodeString("$hours:$minutes:$seconds")
    }

    override fun deserialize(decoder: Decoder): Duration {
        val (h, m, s) = decoder.decodeString().split(":").map { it.toInt() }
        return (h * 3600 + m * 60 + s).seconds
    }

}