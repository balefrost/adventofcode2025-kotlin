plugins {
    kotlin("jvm") version "2.2.21"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.hamcrest:hamcrest:3.0")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}