plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.gizmo.brennon"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Brennon Core
    implementation(project(":brennon-core"))

    // Velocity API
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks {
    shadowJar {
        archiveBaseName.set("brennon-velocity")
        archiveClassifier.set("")

        // Relocate dependencies if needed
        relocate("com.google.gson", "com.gizmo.brennon.libs.gson")
        relocate("com.google.guava", "com.gizmo.brennon.libs.guava")
    }

    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}