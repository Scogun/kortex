package com.ucasoft.kortex.di

import com.ucasoft.kortex.config.ConfigLoader
import com.ucasoft.kortex.json
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import org.koin.dsl.module

val configModule = module {
    single { ConfigLoader.load() }
}

val webSocketClient = module {
    single {
        HttpClient(CIO) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(json)
            }
        }
    }
}