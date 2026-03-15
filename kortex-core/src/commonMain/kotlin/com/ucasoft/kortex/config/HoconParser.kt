package com.ucasoft.kortex.config

import com.ucasoft.kortex.client.ClientConfig

internal fun buildClientConfig(
    rawContent: String?,
    getEnv: (String) -> String?
): ClientConfig {
    val properties = rawContent?.parseHocon(getEnv) ?: emptyMap()

    val hostname = properties["kortex.hostname"] ?: "homeassistant.local"
    val port = (properties["kortex.port"] ?: "8123").toInt()
    val token = properties["kortex.token"]
        ?: error("kortex.token is required in application.conf")

    return ClientConfig(hostname = hostname, port = port, token = token)
}

private fun String.parseHocon(getEnv: (String) -> String?): Map<String, String> {
    val result = mutableMapOf<String, String>()
    var currentBlock: String? = null

    for (rawLine in lines()) {
        val line = rawLine.trim()
        when {
            line.isEmpty() || line.startsWith("#") || line.startsWith("//") -> continue
            line.endsWith("{") -> currentBlock = line.removeSuffix("{").trim()
            line == "}" -> currentBlock = null
            line.contains("=") -> {
                val (rawKey, rawValue) = line.split("=", limit = 2).map { it.trim() }
                val key = if (currentBlock != null) "$currentBlock.$rawKey" else rawKey
                val value = resolveEnvPlaceholder(rawValue, getEnv) ?: continue
                result[key] = value
            }
        }
    }
    return result
}

private fun resolveEnvPlaceholder(value: String, getEnv: (String) -> String?): String? {
    val match = Regex("""\$\{\?(\w+)}""").matchEntire(value) ?: return value
    return getEnv(match.groupValues[1])
}