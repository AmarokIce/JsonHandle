plugins {
    kotlin("jvm") version "1.7.0"
    `maven-publish`
    application
}

group = "club.someoneice.jsonprocessor"
version = "1.0"

repositories {
    mavenCentral()

    maven {
        url = uri("http://maven.snowlyicewolf.club")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation("club.someoneice.json:amarok-json-for-java:1.4.4")

    testImplementation("com.google.guava:guava:32.1.2-jre")
}

application {
    mainClass.set("MainKt")
}