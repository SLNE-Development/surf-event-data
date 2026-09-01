package dev.slne.surf.event.data.client

import dev.slne.surf.event.data.EventData

internal object EventDataClient {

    suspend fun getActiveEvents(): List<EventData> =
        TODO("Implement the logic to fetch active events from the data source")
}