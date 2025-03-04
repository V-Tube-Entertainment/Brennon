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

    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")

    // JSON/YAML
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.yaml:snakeyaml:2.0")

    // Make sure Redis client is included
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE") {
        exclude(group = "io.netty") // Avoid conflicts with Velocity's netty
    }
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    shadowJar {
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        dependencies {
            include(dependency(":brennon-core"))
        }

        // Relocate all important dependencies to avoid conflicts
        relocate("com.gizmo.brennon.core", "com.gizmo.brennon.velocity.lib.core")
        relocate("io.lettuce", "com.gizmo.brennon.velocity.lib.lettuce")
        relocate("com.google.gson", "com.gizmo.brennon.velocity.lib.gson")
        relocate("com.google.inject", "com.gizmo.brennon.velocity.lib.inject")
        relocate("org.yaml.snakeyaml", "com.gizmo.brennon.velocity.lib.yaml")
        relocate("com.zaxxer.hikari", "com.gizmo.brennon.velocity.lib.hikari")

        // Make sure service files are merged properly
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        filesMatching("**/*.json") {
            expand("version" to project.version)
        }
    }
}