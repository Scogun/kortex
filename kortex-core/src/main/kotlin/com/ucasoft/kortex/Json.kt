package com.ucasoft.kortex

import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}