package com.ucasoft.kortex.client.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class SubscribeEventRequest(
    @Transient
    private val subscriptionEventId: Int = 0,
    @SerialName("event_type")
    val eventType: EventType
) : SubscribeRequest(subscriptionEventId, SubscriptionType.EVENTS)

enum class EventType {

    @SerialName("state_changed")
    STATE_CHANGED
}