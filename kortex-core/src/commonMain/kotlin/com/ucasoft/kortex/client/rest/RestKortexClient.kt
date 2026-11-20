package com.ucasoft.kortex.client.rest

import com.ucasoft.kortex.client.ClientConfig
import com.ucasoft.kortex.json
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import kotlinx.serialization.json.JsonObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class RestKortexClient: KoinComponent {

    private val client by inject<HttpClient>(named("restClient"))

    private val config by inject<ClientConfig>()

    suspend fun deleteAutomationConfig(id: String) {
        val response = client.delete("api/config/automation/config/$id")

        if (response.status != HttpStatusCode.OK) {
            error("Automation config delete for $id failed: ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun getAutomationConfig(id: String): JsonObject? {
        val response = client.get("api/config/automation/config/$id")

        return when (response.status) {
            HttpStatusCode.OK -> json.decodeFromString<JsonObject>(response.bodyAsText())
            HttpStatusCode.NotFound -> null
            else -> error("Automation config request for $id failed: ${response.status}")
        }
    }

    suspend fun saveAutomationConfig(id: String, config: JsonObject) {
        val response = client.post("api/config/automation/config/$id") {
            contentType(ContentType.Application.Json)
            setBody(config.toString())
        }

        if (response.status != HttpStatusCode.OK) {
            error("Automation config save for $id failed: ${response.status} ${response.bodyAsText()}")
        }
    }

    fun buildImageUrl(entityPicture: String): String = URLBuilder().apply {
        protocol = if (config.secure) URLProtocol.HTTPS else URLProtocol.HTTP
        host = config.hostname
        port = config.port
    }.takeFrom(entityPicture).buildString()
}