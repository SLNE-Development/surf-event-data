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


    println("Cleaning up...")
    EventDataSource.close()
    println("Done in ${System.currentTimeMillis() - startTime}ms")
}