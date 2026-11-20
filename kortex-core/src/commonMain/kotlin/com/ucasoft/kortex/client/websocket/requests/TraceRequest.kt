package com.ucasoft.kortex.client.websocket.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TraceRequest(
    @Transient
    val traceId: Int = 0,
    @SerialName("item_id")
    val itemId: String,
    @SerialName("run_id")
    val runId: String,
    val domain: String = "automation"
): Request(traceId, type = "trace/get")
