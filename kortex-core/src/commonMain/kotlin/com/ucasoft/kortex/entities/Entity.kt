package com.ucasoft.kortex.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Entity(
    @SerialName("entity_id")
    val entityId: String,
    @SerialName("unique_id")
    val uniqueId: String? = null,
    val platform: String? = null,
    @SerialName("device_id")
    val deviceId: String? = null,
    @SerialName("area_id")
    val areaId: String? = null,
    @SerialName("config_entry_id")
    val configEntryId: String? = null,
    @SerialName("disabled_by")
    val disabledBy: String? = null,
    val labels: List<String> = emptyList(),
)