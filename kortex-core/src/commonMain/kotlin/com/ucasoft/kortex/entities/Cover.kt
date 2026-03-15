package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

class Cover(stateFlow: StateFlow<State>, context: KortexContext): Entity<CoverAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<CoverAttributes>()

    val isOpenSupported = (attributes.supportedFeatures and OPEN) != 0
    val isCloseSupported = (attributes.supportedFeatures and CLOSE) != 0
    val isSetPositionSupported = (attributes.supportedFeatures and SET_POSITION) != 0
    val isStopSupported = (attributes.supportedFeatures and STOP) != 0
    val isOpenTiltSupported = (attributes.supportedFeatures and OPEN_TILT) != 0
    val isCloseTiltSupported = (attributes.supportedFeatures and CLOSE_TILT) != 0
    val isStopTiltSupported = (attributes.supportedFeatures and STOP_TILT) != 0
    val isSetTiltPositionSupported = (attributes.supportedFeatures and SET_TILT_POSITION) != 0

    suspend fun open() {
        if (!isOpenSupported) {
            throw UnsupportedOperationException("Cover does not support open")
        }
        callService("open")
    }

    suspend fun close() {
        if (!isCloseSupported) {
            throw UnsupportedOperationException("Cover does not support close")
        }
        callService("close")
    }

    suspend fun setPosition(position: Int) {
        if (!isSetPositionSupported) {
            throw UnsupportedOperationException("Cover does not support set position")
        }

        require(position in 0..100) {
            "Position must be between 0 and 100"
        }

        callService("set_position", "position" to JsonPrimitive(position))
    }

    suspend fun stop() {
        if (!isStopSupported) {
            throw UnsupportedOperationException("Cover does not support stop")
        }
        callService("stop")
    }

    private companion object {
        const val OPEN = 1
        const val CLOSE = 2
        const val SET_POSITION = 4
        const val STOP = 8
        const val OPEN_TILT = 16
        const val CLOSE_TILT = 32
        const val STOP_TILT = 64
        const val SET_TILT_POSITION = 128
    }
}

@Serializable
open class CoverAttributes: DeviceClassAttributes<CoverDeviceClass>() {
    @SerialName("current_position")
    val currentPosition: Int? = null
    @SerialName("current_tilt_position")
    val currentTiltPosition: Int? = null
    @SerialName("is_closed")
    val isClosed: Boolean? = null
    @SerialName("is_closing")
    val isClosing: Boolean? = null
    @SerialName("is_opening")
    val isOpening: Boolean? = null
    @SerialName("supported_features")
    val supportedFeatures: Int = 0
}

enum class CoverDeviceClass {
    AWNING,
    BLIND,
    CURTAIN,
    DAMPER,
    DOOR,
    GARAGE,
    GATE,
    SHADE,
    SHUTTER,
    WINDOW
}