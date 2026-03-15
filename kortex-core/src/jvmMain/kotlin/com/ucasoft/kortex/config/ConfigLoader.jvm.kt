package com.ucasoft.kortex.config

import com.typesafe.config.ConfigFactory
import com.ucasoft.kortex.client.ClientConfig

internal actual fun loadConfig(): ClientConfig {
    val root = ConfigFactory.load().withFallback(
        ConfigFactory.parseMap(
            mapOf(
                "kortex" to mapOf(
                    "hostname" to "homeassistant.local",
                    "port" to 8123
                )
            )
        )
    ).getConfig("kortex")

    return ClientConfig(
        hostname = root.getString("hostname"),
        port = root.getInt("port"),
        token = root.getString("token")
    )
}