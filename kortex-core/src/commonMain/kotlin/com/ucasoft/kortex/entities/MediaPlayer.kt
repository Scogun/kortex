package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

class MediaPlayer(stateFlow: StateFlow<State>, context: KortexContext): EntityState<MediaPlayerAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<MediaPlayerAttributes>()

    val isPausedSupported = (attributes.supportedFeatures and PAUSE) != 0
    val isSeekSupported = (attributes.supportedFeatures and SEEK) != 0
    val isVolumeSetSupported = (attributes.supportedFeatures and VOLUME_SET) != 0
    val isVolumeMuteSupported = (attributes.supportedFeatures and VOLUME_MUTE) != 0
    val isPreviousTrackSupported = (attributes.supportedFeatures and PREVIOUS_TRACK) != 0
    val isNextTrackSupported = (attributes.supportedFeatures and NEXT_TRACK) != 0
    val isTurnOnSupported = (attributes.supportedFeatures and TURN_ON) != 0
    val isTurnOffSupported = (attributes.supportedFeatures and TURN_OFF) != 0
    val isPlayMediaSupported = (attributes.supportedFeatures and PLAY_MEDIA) != 0
    val isVolumeStepSupported = (attributes.supportedFeatures and VOLUME_STEP) != 0
    val isSelectSourceSupported = (attributes.supportedFeatures and SELECT_SOURCE) != 0
    val isStopSupported = (attributes.supportedFeatures and STOP) != 0
    val isClearPlaylistSupported = (attributes.supportedFeatures and CLEAR_PLAYLIST) != 0
    val isPlaySupported = (attributes.supportedFeatures and PLAY) != 0
    val isShuffleSetSupported = (attributes.supportedFeatures and SHUFFLE_SET) != 0
    val isSelectSoundModeSupported = (attributes.supportedFeatures and SELECT_SOUND_MODE) != 0
    val isBrowseMediaSupported = (attributes.supportedFeatures and BROWSE_MEDIA) != 0
    val isRepeatSetSupported = (attributes.supportedFeatures and REPEAT_SET) != 0
    val isGroupingSupported = (attributes.supportedFeatures and GROUPING) != 0
    val isMediaAnnounceSupported = (attributes.supportedFeatures and MEDIA_ANNOUNCE) != 0
    val isMediaEnqueueSupported = (attributes.supportedFeatures and MEDIA_ENQUEUE) != 0
    val isSearchMediaSupported = (attributes.supportedFeatures and SEARCH_MEDIA) != 0

    private companion object {
        const val PAUSE = 1
        const val SEEK = 2
        const val VOLUME_SET = 4
        const val VOLUME_MUTE = 8
        const val PREVIOUS_TRACK = 16
        const val NEXT_TRACK = 32
        const val TURN_ON = 128
        const val TURN_OFF = 256
        const val PLAY_MEDIA = 512
        const val VOLUME_STEP = 1024
        const val SELECT_SOURCE = 2048
        const val STOP = 4096
        const val CLEAR_PLAYLIST = 8192
        const val PLAY = 16384
        const val SHUFFLE_SET = 32768
        const val SELECT_SOUND_MODE = 65536
        const val BROWSE_MEDIA = 131072
        const val REPEAT_SET = 262144
        const val GROUPING = 524288
        const val MEDIA_ANNOUNCE = 1048576
        const val MEDIA_ENQUEUE = 2097152
        const val SEARCH_MEDIA = 4194304
    }
}

@Serializable
data class MediaPlayerAttributes(
    @SerialName("device_class")
    val deviceClass: DeviceClass? = null,
    @SerialName("media_artist")
    val mediaArtist: String? = null,
    @SerialName("media_duration")
    val mediaDuration: Double? = null,
    @SerialName("media_position")
    val mediaPosition: Double? = null,
    @SerialName("media_title")
    val mediaTitle: String? = null,
    @SerialName("media_position_updated_at")
    val mediaPositionUpdatedAt: Instant? = null,
): FeaturedAttributes()

enum class DeviceClass {
    TV,
    SPEAKER,
    RECEIVER
}