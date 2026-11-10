package com.ucasoft.kortex.client

data class ClientConfig(
    val hostname: String,
    val port: Int,
    val token: String,
    val secure: Boolean = false
)
