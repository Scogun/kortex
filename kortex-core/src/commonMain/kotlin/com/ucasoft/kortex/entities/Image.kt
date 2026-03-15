package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

class Image(stateFlow: StateFlow<State>, context: KortexContext): Entity<ImageAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<ImageAttributes>()
}

@Serializable
open class ImageAttributes: StateAttributes() {
    @SerialName("access_token")
    val accessToken: String? = null
    @SerialName("content_type")
    val contentType: String = "image/jpeg"
    @SerialName("entity_picture")
    val entityPicture: String? = null
    @SerialName("image_last_updated")
    val imageLastUpdated: Instant? = null
    @SerialName("image_url")
    val imageUrl: String? = null
}