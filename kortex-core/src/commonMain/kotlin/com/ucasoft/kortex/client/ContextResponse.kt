package com.ucasoft.kortex.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class ContextResponse(
    val context: JsonElement,
    val response: JsonObject
)
