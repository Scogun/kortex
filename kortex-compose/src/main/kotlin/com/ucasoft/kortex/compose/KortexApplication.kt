package com.ucasoft.kortex.compose

import androidx.compose.runtime.*
import com.ucasoft.kortex.client.ClientConfig
import com.ucasoft.kortex.client.KortexClient
import com.ucasoft.kortex.client.KortexContext
import com.ucasoft.kortex.client.requests.EventType
import com.ucasoft.kortex.di.configModule
import com.ucasoft.kortex.di.webSocketClient
import com.ucasoft.kortex.entities.Entities
import com.ucasoft.kortex.entities.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.KoinApplication
import org.koin.compose.LocalKoinApplication
import org.koin.compose.getKoin
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

val LocalKortexApplication = staticCompositionLocalOf<KortexApplicationState> {
    error("Kortex Application state not found. Please wrap your application in KortexApplication.")
}

@Composable
fun KortexApplication(token: String, host: String = "homeassistant.local", port: Int = 8123, content: @Composable () -> Unit) {
    KortexApplication(
        modules = listOf(module {
            single { ClientConfig(host, port, token) }
        }),
        content
    )
}

@Composable
fun KortexApplication(
    modules: List<Module> = emptyList(),
    content: @Composable () -> Unit
) {
    KoinApplication(
        configuration = koinConfiguration {
            val updatedModules = modules.toMutableList().also {
                it.addAll(0, listOf(configModule, webSocketClient))
            }
            modules(updatedModules)
        }
    ) {
        val states = remember { MutableStateFlow<Map<String, State>>(emptyMap()) }

        fun update(state: State) {
            states.update { it + (state.entityId to state) }
        }

        var applicationState by remember { mutableStateOf(KortexApplicationState()) }

        CompositionLocalProvider(
            LocalKortexApplication provides applicationState
        ) {
            content()
        }

        var initialStateIds by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(getKoin()) {
            try {
                KortexClient().connect(
                    {
                        applicationState = applicationState.copy(
                            isLoading = true,
                            context = it,
                            error = null
                        )
                        initialStateIds = it.getStates()
                        it.subscribeEvents(EventType.STATE_CHANGED)
                    },
                    { id, result ->
                        when (id) {
                            initialStateIds -> {
                                val initialStates = result as List<State>
                                initialStates.forEach { update(it) }
                                applicationState = applicationState.copy(
                                    isLoading = false,
                                    entities = Entities(applicationState.context!!, states),
                                    error = null
                                )
                            }
                            else -> println(result)
                        }
                    },
                    {
                        if (it.type == EventType.STATE_CHANGED.toString().lowercase()) {
                            update(it.data.newState)
                        }
                    }
                )
            } catch (e: Exception) {
                applicationState = applicationState.copy(
                    isLoading = false,
                    error = e.message ?: e.toString()
                )
            }
        }
    }
}

data class KortexApplicationState(
    val isLoading: Boolean = true,
    val entities: Entities? = null,
    val context: KortexContext? = null,
    val error: String? = null
)