plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta10"
}

repositories {
    mavenCentral()
    maven("https://repo.endoy.dev/endoy-public")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://libraries.minecraft.net")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.lucko.me/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.diogotc.com/snapshots/")
    maven("https://mvn.exceptionflug.de/repository/exceptionflug-public/")
    maven("https://repository.apache.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":Brennon-core"))

    implementation("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-native:3.4.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.19.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.19.0")
    implementation("net.kyori:adventure-text-minimessage:4.19.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.19.0")
    implementation("net.kyori:adventure-nbt:4.19.0")
    implementation("org.slf4j:slf4j-api:1.7.30")
    compileOnly("org.bstats:bstats-velocity:3.0.0")
    compileOnly("com.github.ProxioDev.ValioBungee:RedisBungee-Velocity:0.12.3")
    implementation("dev.endoy.configuration:ConfigurationAPI:4.0.2")
    implementation("org.mongodb:mongodb-driver-sync:4.4.2")
    implementation("org.mozilla:rhino:1.7.14")
    implementation("de.christophkraemer:rhino-script-engine:1.2.1")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("com.rabbitmq:amqp-client:5.18.0")
    implementation("io.github.karlatemp:unsafe-accessor:1.7.0")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    implementation("io.netty:netty-all:4.1.107.Final") // Add this line
    implementation("io.netty:netty-transport:4.1.107.Final")
    implementation("io.netty:netty-handler:4.1.107.Final")
    implementation("io.netty:netty-codec:4.1.107.Final")
    implementation("io.netty:netty-buffer:4.1.107.Final")
    implementation("io.netty:netty-common:4.1.107.Final")
    implementation("io.netty:netty-resolver:4.1.107.Final")
    implementation("io.netty:netty-resolver-dns:4.1.107.Final")
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    shadowJar {
        archiveBaseName.set("Brennon-velocity")
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        dependencies {
            include(project(":Brennon-core"))
            include(dependency("io.github.karlatemp:unsafe-accessor"))
            include(dependency("org.jsoup:jsoup"))
            include(dependency("com.rabbitmq:amqp-client"))
            include(dependency("de.christophkraemer:rhino-script-engine"))
            include(dependency("org.apache.commons:commons-pool2"))
            include(dependency("org.mozilla:rhino"))
            include(dependency("org.bstats:bstats-velocity"))
            include(dependency("dev.endoy.configuration:ConfigurationAPI"))
            include(dependency("com.mongodb:mongodb-driver-sync"))
            include(dependency("io.lettuce:lettuce-core"))
            include(dependency("org.reactivestreams:reactive-streams"))
            include(dependency("io.netty:netty-resolver"))
            include(dependency("io.netty:netty-resolver-dns"))
            include(dependency("io.projectreactor:reactor-core"))
            include(dependency("com.zaxxer:HikariCP"))
            include(dependency("com.mysql:mysql-connector-j"))
            include(dependency("org.mariadb.jdbc:mariadb-java-client"))
            include(dependency("org.postgresql:postgresql"))
            include(dependency("org.xerial:sqlite-jdbc"))
            include(dependency("com.google.code.gson:gson"))
            include(dependency("org.yaml:snakeyaml"))
            include(dependency("com.h2database:h2"))
            include(dependency("io.netty:netty-all"))
            include(dependency("io.netty:netty-transport"))
            include(dependency("io.netty:netty-handler"))
            include(dependency("io.netty:netty-codec"))
            include(dependency("io.netty:netty-buffer"))
            include(dependency("io.netty:netty-common"))
        }

        // Relocate all important dependencies to avoid conflicts
        relocate("org.bstats", "com.gizmo.brennon.internal..bstats")
        relocate("io.github.karlatemp", "com.gizmo.brennon.internal.karlatemp")
        relocate("org.jsoup", "com.gizmo.brennon.internal.jsoup")
        relocate("com.rabbitmq", "com.gizmo.brennon.internal.rabbitmq")
        relocate("org.apache.commons.pool2", "com.gizmo.brennon.internal.commons.pool2")
        relocate("org.mozilla", "com.gizmo.brennon.internal.mozilla")
        relocate("de.christophkraemer", "com.gizmo.brennon.internal.christophkraemer")
        relocate("dev.endoy.configuration", "com.gizmo.brennon.internal.configuration")
        relocate("io.lettuce", "com.gizmo.brennon.internal.lettuce")//
        relocate("org.reactivestreams", "com.gizmo.brennon.internal.reactive")//
        relocate("com.google.gson", "com.gizmo.brennon.internal.gson")
        relocate("org.yaml.snakeyaml", "com.gizmo.brennon.internal.yaml")
        relocate("com.zaxxer.hikari", "com.gizmo.brennon.internal.hikari")//
        relocate("reactor", "com.gizmo.brennon.internal.reactor") //
        relocate("io.netty", "com.gizmo.brennon.internal.netty")
        relocate("com.mysql", "com.gizmo.brennon.internal.mysql")//
        relocate("org.mariadb.jdbc", "com.gizmo.brennon.internal.mariadb")//
        relocate("org.postgresql", "com.gizmo.brennon.internal.postgresql")//
        relocate("org.sqlite", "com.gizmo.brennon.internal.sqlite")
        relocate("com.mongodb", "com.gizmo.brennon.internal.mongodb")//
        relocate("org.h2", "com.gizmo.brennon.internal.h2")
        relocate("reactor", "com.gizmo.brennon.internal.reactor")
        relocate("io.projectreactor", "com.gizmo.brennon.internal.reactor")//
        relocate("io.netty", "com.gizmo.brennon.internal.netty") {
            // Exclude Velocity's own Netty classes to avoid conflicts
            exclude("io.netty.velocity.**")
        }

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
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}