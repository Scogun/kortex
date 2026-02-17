package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Person(stateFlow: StateFlow<State>, context: KortexContext): Entity<PersonAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<PersonAttributes>()
}

@Serializable
data class PersonAttributes(
    val id: String,
    @SerialName("device_trackers")
    val deviceTrackers: List<String>,
    val editable: Boolean,
    val latitude: Double,
    val longitude: Double,
    val source: String,
    @SerialName("user_id")
    val userId: String? = null,
): StateAttributes()