@file:OptIn(ExperimentalWasmJsInterop::class)

package com.ucasoft.kortex.config

import com.ucasoft.kortex.client.ClientConfig
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

internal actual fun loadConfig(): ClientConfig = _root_ide_package_.com.ucasoft.kortex.config.buildClientConfig(
    rawContent = readFile("application.conf"),
    getEnv = { name ->
        js("(typeof process !== 'undefined' && process.env) ? process.env[name] : undefined")
            .unsafeCast<String?>()
    }
)

private fun readFile(path: String): String? = try {
    js("require('fs').readFileSync(path, 'utf8')").unsafeCast<String>()
} catch (e: Throwable) {
    null
}