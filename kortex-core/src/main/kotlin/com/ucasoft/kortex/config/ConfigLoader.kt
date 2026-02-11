package com.ucasoft.kortex.config

import com.typesafe.config.ConfigFactory
import com.ucasoft.kortex.client.ClientConfig

object ConfigLoader {
    private val root = ConfigFactory.load().withFallback(
        ConfigFactory.parseMap(
            mapOf(
                "kortex" to mapOf(
                    "hostname" to "homeassistant.local",
                    "port" to 8123
                )
            )
        )
    ).getConfig("kortex")

    fun load() =
        ClientConfig(
            root.getString("hostname"),
            root.getInt("port"),
            root.getString("token")
        )
}