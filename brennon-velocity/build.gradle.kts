plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.gizmo"
version = "1.0-SNAPSHOT"

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

    // JSON //yaml
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.yaml:snakeyaml:2.0")
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

        relocate("com.gizmo.brennon.core", "com.gizmo.brennon.velocity.lib.core")
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

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}