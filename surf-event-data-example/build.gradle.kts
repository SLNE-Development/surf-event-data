plugins {
    id("dev.slne.surf.api.gradle.standalone")
}

dependencies {
    implementation(project(":surf-event-data-source"))
    implementation("ch.qos.logback:logback-classic:1.6.3")
}
