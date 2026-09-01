package dev.slne.surf.event.data

import dev.slne.surf.event.data.client.EventDataClient

object EventDataSource {
    suspend fun getActiveEvents() = EventDataClient
}