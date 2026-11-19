package com.ucasoft.kortex.di

import com.ucasoft.kortex.client.ClientConfig
import com.ucasoft.kortex.config.ConfigLoader
import com.ucasoft.kortex.json
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.bearerAuth
import io.ktor.http.URLProtocol
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
            defaultRequest {
                val config = get<ClientConfig>()
                url {
                    protocol = if (config.secure) URLProtocol.HTTPS else URLProtocol.HTTP
                    host = config.hostname
                    port = config.port
                }
                bearerAuth(config.token)
            }
        }
    }
}