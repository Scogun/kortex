package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ucasoft.kortex.compose.KortexApplication
import com.ucasoft.kortex.compose.LocalKortexApplication
import com.ucasoft.kortex.config.ConfigLoader

val config = ConfigLoader.load()

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        var host by remember { mutableStateOf(config.hostname)}
        var token by remember { mutableStateOf(config.token) }

        Scaffold(
            topBar = {
                Row {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host") }
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Token") }
                    )
                }
            }
        ) {
            KortexApplication(token, host) {

                val state = LocalKortexApplication.current

                when {
                    state.error != null -> {
                        Text(state.error!!)
                    }

                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {
                        HomeAssistant(state)
                    }
                }
            }
        }
    }
}

