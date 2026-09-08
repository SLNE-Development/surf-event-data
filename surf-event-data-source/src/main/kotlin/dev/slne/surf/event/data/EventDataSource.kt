package dev.slne.surf.event.data

import dev.slne.surf.event.data.client.EventDataClient

object EventDataSource {
    private val eventCache = mutableListOf<EventData>()
    suspend fun fetchEvents() = EventDataClient.fetchEvents()

    suspend fun refreshCache() {
        eventCache.clear()
        eventCache.addAll(fetchEvents())
    }

    fun close() = EventDataClient.close()

    fun getActiveEvents() = eventCache.filter { it.active }
    fun getEvents() = eventCache.toList()
}