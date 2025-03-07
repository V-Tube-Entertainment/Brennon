plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta10" apply false
}

allprojects {
    group = "com.gizmo.brennon"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://libraries.minecraft.net")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://repo.lucko.me/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://repo.diogotc.com/snapshots/")
        maven("https://mvn.exceptionflug.de/repository/exceptionflug-public/")
        maven("https://repo.endoy.dev/endoy-public")
        maven("https://repository.apache.org/content/repositories/snapshots/")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}