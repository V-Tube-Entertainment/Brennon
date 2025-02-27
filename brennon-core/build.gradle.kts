plugins {
    id("java-library")
}

repositories {
    maven("https://repo.lucko.me/")
}

dependencies {
    // Database
    api("com.zaxxer:HikariCP:5.1.0")
    api("org.mariadb.jdbc:mariadb-java-client:3.3.2")

    // Redis
    api("io.lettuce:lettuce-core:6.3.1.RELEASE") {
        exclude(group = "io.netty") // Avoid conflicts with Minecraft's netty
    }

    // Adventure API
    api(platform("net.kyori:adventure-bom:4.15.0"))
    api("net.kyori:adventure-api")
    api("net.kyori:adventure-text-minimessage")
    api("net.kyori:adventure-text-serializer-gson")

    // Configuration
    api("org.spongepowered:configurate-hocon:4.1.2")
    api("org.spongepowered:configurate-gson:4.1.2")

    // Dependency Injection
    api("com.google.inject:guice:7.0.0") {
        exclude(group = "com.google.guava", module = "guava") // Use Minecraft's Guava
    }

    // JSON
    api("com.google.code.gson:gson:2.10.1")

    // Utilities
    api("com.google.guava:guava:33.0.0-jre")
    api("org.apache.commons:commons-lang3:3.14.0")
    api("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // Logging
    api("org.slf4j:slf4j-api:2.0.11")
    api("ch.qos.logback:logback-classic:1.4.14")

    // LuckPerms API
    compileOnly("net.luckperms:api:5.4") {
        exclude(group = "com.google.guava", module = "guava")
    }

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    withJavadocJar()
    withSourcesJar()
}