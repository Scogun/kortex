package com.ucasoft.kortex.config

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.getenv
import com.ucasoft.kortex.client.ClientConfig

@OptIn(ExperimentalForeignApi::class)
internal actual fun loadConfig(): ClientConfig = buildClientConfig(
    rawContent = readFile("application.conf"),
    getEnv = { getenv(it)?.toKString() }
)

@OptIn(ExperimentalForeignApi::class)
private fun readFile(path: String): String? {
    val file = fopen(path, "r") ?: return null
    return try {
        buildString {
            val buffer = ByteArray(4096)
            while (fgets(buffer.refTo(0), buffer.size, file) != null) {
                append(buffer.toKString())
            }
        }
    } finally {
        fclose(file)
    }
}