package com.ucasoft.kortex.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class DeviceClassAttributes<T>: StateAttributes() {
    @SerialName("device_class")
    val deviceClass: T? = null
}