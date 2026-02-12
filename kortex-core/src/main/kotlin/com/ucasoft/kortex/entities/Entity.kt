package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import com.ucasoft.kortex.json
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.decodeFromJsonElement

abstract class Entity<T: StateAttributes>(
    val stateFlow: StateFlow<State>,
    protected val context: KortexContext
) {
    val entityId = stateFlow.value.entityId

    val domain = entityId.substringBefore('.')

    abstract val attributes: T

    inline fun <reified T : StateAttributes> getAttributeAs(): T = json.decodeFromJsonElement(attributesJson())

    fun <T> observe(
        mapper: (State) -> T
    ) = stateFlow
        .map(mapper)
        .distinctUntilChanged()
        .stateIn(
            context.scope,
            started = SharingStarted.Eagerly,
            mapper(stateFlow.value)
        )

    @PublishedApi
    internal fun attributesJson() = stateFlow.value.attributes
}