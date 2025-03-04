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
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE")

    // Database
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("mysql:mysql-connector-java:8.0.33")        // MySQL
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")  // MariaDB
    implementation("org.postgresql:postgresql:42.6.0")         // PostgreSQL
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")         // SQLite
    implementation("com.h2database:h2:2.2.220")
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
            include(dependency("io.lettuce:lettuce-core"))
            include(dependency("com.zaxxer:HikariCP"))
            include(dependency("mysql:mysql-connector-java"))
            include(dependency("org.mariadb.jdbc:mariadb-java-client"))
            include(dependency("org.postgresql:postgresql"))
            include(dependency("org.xerial:sqlite-jdbc"))
            include(dependency("com.google.code.gson:gson"))
            include(dependency("org.yaml:snakeyaml"))
        }

        // Relocate all important dependencies to avoid conflicts
        relocate("io.lettuce", "com.gizmo.brennon.velocity.lib.lettuce")
        relocate("com.google.gson", "com.gizmo.brennon.velocity.lib.gson")
        relocate("org.yaml.snakeyaml", "com.gizmo.brennon.velocity.lib.yaml")
        relocate("com.zaxxer.hikari", "com.gizmo.brennon.velocity.lib.hikari")
        relocate("reactor", "com.gizmo.brennon.velocity.lib.reactor") // Required for Lettuce
        relocate("io.netty", "com.gizmo.brennon.velocity.lib.netty")
        relocate("com.zaxxer.hikari", "com.gizmo.brennon.velocity.lib.hikari")
        relocate("mysql", "com.gizmo.brennon.velocity.lib.mysql")
        relocate("org.mariadb.jdbc", "com.gizmo.brennon.velocity.lib.mariadb")
        relocate("org.postgresql", "com.gizmo.brennon.velocity.lib.postgresql")
        relocate("org.sqlite", "com.gizmo.brennon.velocity.lib.sqlite")

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