plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    group = "com.gizmo.brennon"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.lucko.me/") // LuckPerms repository
        maven("https://libraries.minecraft.net") // Minecraft libraries
        maven("https://repo.spongepowered.org/repository/maven-public/") // Configurate
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}