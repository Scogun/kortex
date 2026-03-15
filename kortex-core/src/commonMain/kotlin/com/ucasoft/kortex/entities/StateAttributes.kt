package com.ucasoft.kortex.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class StateAttributes {
    @SerialName("friendly_name")
    val friendlyName: String = ""
}