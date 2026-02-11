package com.ucasoft.kortex.client.requests

import kotlinx.serialization.Serializable

@Serializable
open class Request(
    val id: Int,
    val type: String
)

enum class RequestType {

    CALL_SERVICE,
    DEVICE_LIST,
    GET_STATES,
    SUBSCRIBE
}