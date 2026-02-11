package com.ucasoft.kortex.client.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val type: String = "auth",
    @SerialName("access_token")
    val token: String
)
