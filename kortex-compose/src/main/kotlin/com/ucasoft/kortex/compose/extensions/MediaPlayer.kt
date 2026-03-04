package com.ucasoft.kortex.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.ucasoft.kortex.entities.MediaPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

val MediaPlayer.mediaPosition
    get() = if (attributes.mediaPosition == null) {
        null
    } else {
        if (stateFlow.value.state == "playing") {
            val position = attributes.mediaPosition!!
            val updatedAt = attributes.mediaPositionUpdatedAt!!
            position + (Clock.System.now() - updatedAt).inWholeSeconds
        } else {
            attributes.mediaPosition!!
        }
    }

@Composable
fun MediaPlayer.mediaPositionAsState(): State<Float> {
    val mediaPositionState = remember(this) { mutableStateOf(calculateProgress(attributes.mediaDuration, mediaPosition)) }

    LaunchedEffect(this) {
        stateFlow.collect {
            mediaPositionState.value = calculateProgress(attributes.mediaDuration, mediaPosition)
        }
    }

    LaunchedEffect(this) {
        while (isActive) {
            if (stateFlow.value.state == "playing") {
                mediaPositionState.value = calculateProgress(attributes.mediaDuration, mediaPosition)
            }
            delay(500.milliseconds)
        }
    }

    return mediaPositionState
}

fun MediaPlayer.getTrackFullName(delimiter: String = " - ") : String {
    val builder = StringBuilder(attributes.mediaArtist ?: "")
    if (!attributes.mediaArtist.isNullOrBlank() && !attributes.mediaTitle.isNullOrBlank()) {
        builder.append(delimiter)
    }
    builder.append(attributes.mediaTitle ?: "")
    return builder.toString()
}

private fun calculateProgress(duration: Double?, position: Double?) =
    (position?.toFloat() ?: 0f) / (duration?.toFloat() ?: 1f)
