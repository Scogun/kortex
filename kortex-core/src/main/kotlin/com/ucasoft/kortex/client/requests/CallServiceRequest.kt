package com.ucasoft.kortex.client.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
data class CallServiceRequest(
    @Transient
    val callId: Int = 0,
    val domain: String,
    val service: String,
    @SerialName("service_data")
    val data: Map<String, JsonElement>,
    @SerialName("return_response")
    val returnResponse: Boolean? = null
): Request(callId, type = "call_service")
