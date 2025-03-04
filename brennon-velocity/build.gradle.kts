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

    // Injection
    implementation("com.google.inject:guice:5.1.0") {
        exclude(group = "com.google.guava") // Avoid conflicts with Velocity's guava
    }
    implementation("javax.inject:javax.inject:1")

    // Redis
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE") {
        exclude(group = "io.netty") // Avoid conflicts with Velocity's netty
    }
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    shadowJar {
        archiveBaseName.set("brennon-velocity")
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        dependencies {
            include(project(":brennon-core"))
        }

        // Relocate all important dependencies to avoid conflicts
        relocate("com.gizmo.brennon.core", "com.gizmo.brennon.velocity.lib.core")
        relocate("io.lettuce", "com.gizmo.brennon.velocity.lib.lettuce")
        relocate("com.google.gson", "com.gizmo.brennon.velocity.lib.gson")
        relocate("com.google.inject", "com.gizmo.brennon.velocity.lib.inject")
        relocate("org.yaml.snakeyaml", "com.gizmo.brennon.velocity.lib.yaml")
        relocate("com.zaxxer.hikari", "com.gizmo.brennon.velocity.lib.hikari")
        relocate("javax.inject", "com.gizmo.brennon.velocity.lib.javax.inject")

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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}