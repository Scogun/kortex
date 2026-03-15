package com.ucasoft.kortex.entities

import com.ucasoft.kortex.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Instant

@Serializable
data class State(
    @SerialName("entity_id")
    val entityId: String,
    val state: String,
    val attributes: JsonObject,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_reported")
    val lastReported: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant
) {
    inline fun <reified T: StateAttributes> getAttributeAs() = json.decodeFromJsonElement<T>(attributes)

}