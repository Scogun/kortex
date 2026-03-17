package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ucasoft.kortex.compose.extensions.collectAsState
import com.ucasoft.kortex.compose.extensions.getTrackFullName
import com.ucasoft.kortex.compose.extensions.mediaPositionAsState
import com.ucasoft.kortex.entities.MediaPlayer

@Composable
internal fun MediaPlayers(mediaPlayers: List<MediaPlayer>) {
    LazyColumn {
        items(mediaPlayers) {
            MediaPlayer(it)
        }
    }
}

@Composable
private fun MediaPlayer(mediaPlayer: MediaPlayer) {

    val state by mediaPlayer.collectAsState()
    val position by mediaPlayer.mediaPositionAsState()

    ListItem(
        overlineContent = {
            Text(mediaPlayer.attributes.friendlyName)
        },
        leadingContent = {
            MediaPlayerStateIcon(state.stateFlow.value.state)
        },
        headlineContent = {
            Text(state.attributes.getTrackFullName())
        },
        supportingContent = {
            LinearProgressIndicator(
                progress = { position },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun MediaPlayerStateIcon(state: String) {
    when (state) {
        "playing" -> Icon(Icons.Default.PlayArrow, contentDescription = "Play")
        "paused" -> Icon(Icons.Default.Pause, contentDescription = "Pause")
        "off" -> Icon(Icons.Default.PowerOff, contentDescription = "Off")
        else -> Icon(Icons.Default.QuestionMark, contentDescription = "Unknown")
    }
}