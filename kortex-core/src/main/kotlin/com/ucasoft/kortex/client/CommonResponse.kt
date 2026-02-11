package com.ucasoft.kortex.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class CommonResponse(
    val id: Int? = null,
    val type: String,
    val event: JsonObject? = null,
    val message: String? = null,
    val result: JsonElement? = null,
    val success: Boolean? = null
)
