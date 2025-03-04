plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":brennon-core"))

    // Update Velocity to latest version
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    // JSON/YAML
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.yaml:snakeyaml:2.0")

    // Remove explicit Guice and javax.inject as they're provided by Velocity
    // implementation("com.google.inject:guice:5.1.0")
    // implementation("javax.inject:javax.inject:1")

    // Redis
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE") {
        exclude(group = "io.netty") // Avoid conflicts with Velocity's netty
    }
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    shadowJar {
        archiveBaseName.set("brennon-velocity")
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        dependencies {
            include(project(":brennon-core"))
        }

        // Relocate all important dependencies to avoid conflicts
        relocate("io.lettuce", "com.gizmo.brennon.velocity.lib.lettuce")
        relocate("com.google.gson", "com.gizmo.brennon.velocity.lib.gson")
        relocate("org.yaml.snakeyaml", "com.gizmo.brennon.velocity.lib.yaml")
        relocate("com.zaxxer.hikari", "com.gizmo.brennon.velocity.lib.hikari")

        // Don't relocate core as it's your own code
        // relocate("com.gizmo.brennon.core", "com.gizmo.brennon.velocity.lib.core")

        // Don't relocate Guice or javax.inject as they're provided by Velocity
        // relocate("com.google.inject", "com.gizmo.brennon.velocity.lib.inject")
        // relocate("javax.inject", "com.gizmo.brennon.velocity.lib.javax.inject")

        // Make sure service files are merged properly
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filesMatching("velocity-plugin.json") {
            expand(
                "version" to project.version,
                "name" to project.name,
                "description" to project.description.orEmpty()
            )
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}