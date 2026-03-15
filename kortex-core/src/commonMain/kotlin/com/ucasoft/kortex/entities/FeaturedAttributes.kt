package com.ucasoft.kortex.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class FeaturedAttributes: StateAttributes() {
    @SerialName("supported_features")
    val supportedFeatures: Int = 0
}