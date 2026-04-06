package com.ucasoft.kortex.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val name: String? = null,
    @SerialName("name_by_user")
    val nameByUser: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    @SerialName("model_id")
    val modelId: String? = null,
    @SerialName("sw_version")
    val swVersion: String? = null,
    @SerialName("hw_version")
    val hwVersion: String? = null,
    @SerialName("serial_number")
    val serialNumber: String? = null,
    @SerialName("area_id")
    val areaId: String? = null,
    @SerialName("config_entries")
    val configEntries: List<String> = emptyList(),
    val disabled: Boolean = false,
    val labels: List<String> = emptyList(),
) {
    val displayName: String
        get() = nameByUser ?: name ?: id
}
