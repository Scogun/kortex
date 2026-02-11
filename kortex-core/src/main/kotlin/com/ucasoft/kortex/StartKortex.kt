package com.ucasoft.kortex

import com.ucasoft.kortex.client.KortexClient
import com.ucasoft.kortex.client.KortexContext
import com.ucasoft.kortex.client.requests.EventType
import com.ucasoft.kortex.config.ConfigLoader
import com.ucasoft.kortex.entities.Entities
import com.ucasoft.kortex.entities.Event
import com.ucasoft.kortex.entities.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module

suspend fun startKortex(
    onAuthenticated: suspend (context: KortexContext) -> Unit,
    onResult: suspend (requestId: Int, result: Any) -> Unit = { _, _ -> },
    onEvent: suspend (event: Event) -> Unit
) {

    val config = ConfigLoader.load()
    val configModule = module {
        single { config }
    }

    startKoin {
        modules(configModule)
    }

    KortexClient().connect(onAuthenticated, onResult, onEvent)
}

suspend fun startKortexObservable(
    onStarted: suspend (entities: Entities) -> Unit,
) {

    val states = MutableStateFlow<Map<String, State>>(emptyMap())

    fun update(state: State) {
        states.update { it + (state.entityId to state) }
    }

    var initialStateIds: Int? = null
    var context: KortexContext? = null

    startKortex(
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
                    context!!.scope.launch {
                        onStarted(Entities(context, states))
                    }
                }
            }
        }
    ) {
        if (it.type == EventType.STATE_CHANGED.toString().lowercase()) {
            update(it.data.newState)
        }
    }
}