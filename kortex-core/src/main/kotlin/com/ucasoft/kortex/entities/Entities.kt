package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn

class Entities(
    private val context: KortexContext,
    private val states: StateFlow<Map<String, State>>
) {
    private val entityIds = states.value.keys

    val buttons by lazy { mapEntities(::Button) }

    val calendars by lazy { mapEntities(::Calendar) }

    val lights by lazy { mapEntities(::Light) }

    val sensors by lazy { mapEntities(::Sensor) }

    val switches by lazy { mapEntities(::Switch) }

    val timers by lazy { mapEntities(::Timer) }

    private inline fun <reified T> mapEntities(noinline factory: (StateFlow<State>, KortexContext) -> T, domain: String = T::class.simpleName!!.lowercase()) =
        entityIds.asSequence()
            .filter { it.substringBefore('.') == domain }
            .map { id ->
                val state = states.mapNotNull { it[id] }.stateIn(
                    context.scope,
                    SharingStarted.Eagerly,
                    states.value[id]!!
                )
                factory(state, context)
            }.toList()
}