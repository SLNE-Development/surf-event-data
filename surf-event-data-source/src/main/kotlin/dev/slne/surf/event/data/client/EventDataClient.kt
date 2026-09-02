package dev.slne.surf.event.data.client

import dev.slne.surf.event.data.EventData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object EventDataClient {
    private const val GITHUB_OWNER = "SLNE-DEVELOPMENT"
    private const val GITHUB_REPOSITORY = "surf-event-data"
    private const val GITHUB_PATH = "surf-event-data-store/events"
    private const val GITHUB_BRANCH = "dev"

    private val json = Json {
        ignoreUnknownKeys = true
    }

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
        return fetchDirectory(GITHUB_PATH)
            .filter { it.type == "file" && it.name.endsWith(".json") }
            .mapNotNull { file ->
                val downloadUrl = file.downloadUrl ?: return@mapNotNull null

                runCatching {
                    val eventResponse = client.get(downloadUrl)

                    if (!eventResponse.status.isSuccess()) {
                        println(
                            "[surf-event-data/EventDataClient]: Failed to fetch event data from " +
                                    "$downloadUrl: ${eventResponse.status}"
                        )
                        return@runCatching null
                    }

                    json.decodeFromString<EventData>(eventResponse.bodyAsText())
                }.onFailure {
                    println("Failed to parse ${file.name}:")
                    it.printStackTrace()
                }.getOrNull()
            }
    }

    private suspend fun fetchDirectory(path: String): List<GitHubFile> {
        val response = runCatching {
            client.get(
                "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPOSITORY/contents/$path?ref=$GITHUB_BRANCH"
            )
        }.getOrNull() ?: return emptyList()

        if (!response.status.isSuccess()) {
            return emptyList()
        }

        val entries = runCatching {
            response.body<List<GitHubFile>>()
        }.getOrElse {
            return emptyList()
        }

        val files = mutableListOf<GitHubFile>()

        for (entry in entries) {
            when (entry.type) {
                "file" -> files += entry
                "dir" -> files += fetchDirectory(entry.path)
            }
        }

        return files
    }

    @Serializable
    private data class GitHubFile(
        val name: String,
        val path: String,
        val type: String,
        @SerialName("download_url")
        val downloadUrl: String? = null
    )
}