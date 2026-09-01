package dev.slne.surf.event.data

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class EventData(
    val displayName: String,
    val description: String,
    val startDate: Instant,
    val endDate: Instant,
    val gameServerIds: List<SerializableUUID>,
    val playtimeServer: String,
    val stats: EventStats
)

@Serializable
data class EventStats(
    val uniquePlayers: Int,
    val peakPlayers: Int
)