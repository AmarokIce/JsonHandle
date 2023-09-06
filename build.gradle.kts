plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
    application
}

group = "club.someoneice.jsonprocessor"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/AmarokIce/amarokjsonforjava")
        credentials {
            username = "AmarokIce"
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // Get from GitPackage
    // implementation("club.someoneice.json:amarok-json-for-java:1.2")

    // Get from Jitpack.io
    implementation("com.github.AmarokIce:AmarokJsonForJava:1.2")
    implementation("com.google.guava:guava:32.1.2-jre")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}