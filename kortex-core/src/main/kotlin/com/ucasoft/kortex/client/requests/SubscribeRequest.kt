package com.ucasoft.kortex.client.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class SubscribeRequest(
    @Transient
    private val subscriptionId: Int = 0,
    @Transient
    private val subscriptionType: SubscriptionType = SubscriptionType.UNKNOWN
) : Request(subscriptionId, subscriptionType.toString())

enum class SubscriptionType(val value: String) {
    EVENTS("events"),
    UNKNOWN("unknown");

    override fun toString() = "subscribe_$value"
}