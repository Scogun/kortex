package com.ucasoft.kortex.config

import com.ucasoft.kortex.client.ClientConfig

object ConfigLoader {
    fun load() = loadConfig()
}

internal expect fun loadConfig(): ClientConfig