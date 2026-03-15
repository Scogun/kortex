package com.ucasoft.kortex

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    decodeEnumsCaseInsensitive = true
}