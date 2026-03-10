package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Vacuum(stateFlow: StateFlow<State>, context: KortexContext): Entity<VacuumAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<VacuumAttributes>()

    val isPausedSupported = (attributes.supportedFeatures and PAUSE) != 0
    val isStoppedSupported = (attributes.supportedFeatures and STOP) != 0
    val isReturnHomeSupported = (attributes.supportedFeatures and RETURN_HOME) != 0
    val isFanSpeedSupported = (attributes.supportedFeatures and FAN_SPEED) != 0
    val isBatterySupported = (attributes.supportedFeatures and BATTERY) != 0
    val isSendCommandSupported = (attributes.supportedFeatures and SEND_COMMAND) != 0
    val isLocateSupported = (attributes.supportedFeatures and LOCATE) != 0
    val isCleanSpotSupported = (attributes.supportedFeatures and CLEAN_SPOT) != 0
    val isMapSupported = (attributes.supportedFeatures and MAP) != 0
    val isStateSupported = (attributes.supportedFeatures and STATE) != 0
    val isStartSupported = (attributes.supportedFeatures and START) != 0
    val isCleanAreaSupported = (attributes.supportedFeatures and CLEAN_AREA) != 0

    private companion object {
        const val PAUSE = 4
        const val STOP = 8
        const val RETURN_HOME = 16
        const val FAN_SPEED = 32
        const val BATTERY = 64
        const val SEND_COMMAND = 256
        const val LOCATE = 512
        const val CLEAN_SPOT = 1024
        const val MAP = 2048
        const val STATE = 4096
        const val START = 8192
        const val CLEAN_AREA = 16384
    }
}

@Serializable
open class VacuumAttributes(
    @SerialName("fan_speed")
    val fanSpeed: String? = null,
    @SerialName("fan_speed_list")
    val fanSpeedList: List<String>? = null,
): FeaturedAttributes()