package com.ucasoft.kortex.client.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class RegistryRequest(
    @Transient
    private val registryId: Int = 0,
    @Transient
    private val registryType: RegistryType = RegistryType.UNKNOWN,
): Request(registryId, registryType.toString()) {
}

enum class RegistryType(val value: String) {
    DEVICE("device"),
    ENTITY("entity"),
    UNKNOWN("unknown");

    override fun toString() = "config/${value}_registry/list"
}