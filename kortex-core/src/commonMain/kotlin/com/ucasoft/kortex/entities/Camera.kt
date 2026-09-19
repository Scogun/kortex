package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Camera(stateFlow: StateFlow<State>, context: KortexContext) : EntityState<CameraAttributes>(stateFlow, context) {
    override val attributesFlow = mapAttributes<CameraAttributes>()
}

@Serializable
class CameraAttributes : StateAttributes() {
    @SerialName("access_token")
    val accessToken: String? = null
    @SerialName("entity_picture")
    val entityPicture: String? = null
    val brand: String? = null
    @SerialName("model_name")
    val modelName: String? = null
    @SerialName("motion_detection")
    val motionDetection: Boolean? = null
}