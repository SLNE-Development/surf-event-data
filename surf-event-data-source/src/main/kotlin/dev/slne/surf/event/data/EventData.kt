package dev.slne.surf.event.data

import dev.slne.surf.api.core.serializer.java.uuid.SerializableStringUUID
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class EventData(
    val displayName: String,
    val description: String,
    val startDate: Instant,
    val endDate: Instant,
    val gameServerIds: List<SerializableStringUUID>,
    val playtimeServer: String,
    val stats: EventStats,
    val features: FeatureData,
    val associatedContent: List<String>,
    val active: Boolean,
    val done: Boolean
)

@Serializable
data class EventStats(
    val uniquePlayers: Int,
    val peakPlayers: Int
)

@Serializable
data class FeatureData(
    val homes: Boolean = false,
    val tpa: Boolean = false,
    val spawnCommand: Boolean = false
)