package dev.slne.surf.event.data.client

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.event.data.EventData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object EventDataClient {
    private const val GITHUB_OWNER = "SLNE-DEVELOPMENT"
    private const val GITHUB_REPOSITORY = "surf-event-data"
    private const val GITHUB_PATH = "surf-event-data-store/events"
    private val GITHUB_BRANCH: String? = "dev"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val logger = logger()

    private val client = HttpClient(OkHttp) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }
    }

    fun close() {
        client.close()
    }

    suspend fun fetchEvents(): List<EventData> {
        val response = client.get(buildString {
            append("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPOSITORY/contents/$GITHUB_PATH")

            GITHUB_BRANCH?.let {
                append("?ref=$it")
            }
        })

        if (!response.status.isSuccess()) {
            return emptyList()
        }

        val files = response.body<List<GitHubFile>>()

        return files
            .filter { it.type == "file" && it.name.endsWith(".json") }
            .mapNotNull { file ->
                val eventResponse = client.get(file.downloadUrl)

                if (!eventResponse.status.isSuccess()) {
                    println("[surf-event-data/EventDataClient]: Failed to fetch event data from ${file.downloadUrl}: ${eventResponse.status}")
                    return@mapNotNull null
                }

                runCatching {
                    eventResponse.body<EventData>()
                }.getOrNull()
            }
    }

    @Serializable
    private data class GitHubFile(
        val name: String,
        val type: String,
        val downloadUrl: String
    )
}