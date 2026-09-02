package dev.slne.surf.event.data.client

import dev.slne.surf.event.data.EventData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object EventDataClient {
    private const val GITHUB_OWNER = "SLNE-DEVELOPMENT"
    private const val GITHUB_REPOSITORY = "surf-event-data"
    private const val GITHUB_PATH = "surf-event-data-store/events"
    private val GITHUB_BRANCH: String? = null

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = HttpClient(OkHttp) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        defaultRequest {
            header(HttpHeaders.UserAgent, "surf-event-data-client")
        }
    }

    fun close() {
        client.close()
    }

    suspend fun fetchEvents(): List<EventData> = coroutineScope {
        fetchDirectory(GITHUB_PATH)
            .filter { it.type == "file" && it.name.endsWith(".json") }
            .map { file ->
                async {
                    val downloadUrl = file.downloadUrl ?: return@async null

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
            .awaitAll()
            .filterNotNull()
    }

    private suspend fun fetchDirectory(path: String): List<GitHubFile> = coroutineScope {
        val response = runCatching {
            client.get(
                buildString {
                    append("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPOSITORY/contents/$path")

                    GITHUB_BRANCH?.let {
                        append("?ref=$it")
                    }
                }
            )
        }.getOrNull() ?: return@coroutineScope emptyList()

        if (!response.status.isSuccess()) {
            return@coroutineScope emptyList()
        }

        val entries = runCatching {
            response.body<List<GitHubFile>>()
        }.getOrElse {
            return@coroutineScope emptyList()
        }

        entries.map { entry ->
            async {
                when (entry.type) {
                    "file" -> listOf(entry)
                    "dir" -> fetchDirectory(entry.path)
                    else -> emptyList()
                }
            }
        }.awaitAll().flatten()
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