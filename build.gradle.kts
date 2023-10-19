plugins {
    val kotlinVersion: String by System.getProperties()
    val kvisionVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion apply false
    kotlin("js") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.jpa") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
    kotlin("multiplatform") version kotlinVersion apply false
    id("io.kvision") version kvisionVersion apply false
}
