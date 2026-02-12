package com.ucasoft.kortex.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.ucasoft.kortex.client.KortexClient
import com.ucasoft.kortex.client.KortexContext
import com.ucasoft.kortex.client.requests.EventType
import com.ucasoft.kortex.config.ConfigLoader
import com.ucasoft.kortex.entities.Entities
import com.ucasoft.kortex.entities.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

val configModule = module {
    single { ConfigLoader.load() }
}

val LocalKortex = staticCompositionLocalOf<KortexContext> {
    error("Kortex context not found. Please wrap your application in KortexMultiplatformApplication.")
}

@OptIn(KoinExperimentalAPI::class)
@Composable
fun KortexApplication(
    content: @Composable Entities?.() -> Unit
) {
    KoinMultiplatformApplication(
        config = koinConfiguration {
            modules(configModule)
        }
    ) {
        val states = remember { MutableStateFlow<Map<String, State>>(emptyMap()) }

        fun update(state: State) {
            states.update { it + (state.entityId to state) }
        }

        var context by remember { mutableStateOf<KortexContext?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        context?.let {
            CompositionLocalProvider(
                LocalKortex provides it
            ) {
                if (isLoading) {
                    content(Entities(it, states))
                } else {
                    content(null)
                }
            }
        }

        var initialStateIds by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(Unit) {
            KortexClient().connect(
                {
                    context = it
                    initialStateIds = it.getStates()
                    it.subscribeEvents(EventType.STATE_CHANGED)
                },
                { id, result ->
                    when (id) {
                        initialStateIds -> {
                            val initialStates = result as List<State>
                            initialStates.forEach { update(it) }
                            isLoading = true
                        }
                    }
                },
                {
                    if (it.type == EventType.STATE_CHANGED.toString().lowercase()) {
                        update(it.data.newState)
                    }
                }
            )
        }
    }
}