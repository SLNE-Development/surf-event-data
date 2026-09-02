package dev.slne.surf.event.data.example

import dev.slne.surf.event.data.EventDataSource
import kotlinx.coroutines.runBlocking

fun main() {
    println("Fetching Events...")

    val startTime = System.currentTimeMillis()

    val events = runBlocking {
        EventDataSource.fetchEvents()
    }

    println("Fetched ${events.size} events:")
    events.forEach {
        println("- ${it.displayName} (${it.startDate} - ${it.endDate})")
    }

    println("Caching Events...")

    runBlocking {
        EventDataSource.refreshCache()
    }

    println(
        "First active event: ${
            EventDataSource.getActiveEvents().firstOrNull()?.displayName ?: "None"
        }"
    )
    println(
        "First pending event: ${
            EventDataSource.getEvents()
                .firstOrNull { !it.done && !it.active }?.displayName ?: "None"
        }"
    )


    println("Cleaning up...")
    EventDataSource.close()
    println("Done in ${System.currentTimeMillis() - startTime}ms")
}