plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.lucko.me/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    // Database
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("mysql:mysql-connector-java:8.0.33")        // MySQL
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")  // MariaDB
    implementation("org.postgresql:postgresql:42.6.0")         // PostgreSQL
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")         // SQLite
    implementation("com.h2database:h2:2.2.220")               // H2

    // Redis
    api("io.lettuce:lettuce-core:6.3.1.RELEASE") {
        exclude(group = "io.netty") // Avoid conflicts with Minecraft's netty
    }

    // Adventure API
    api(platform("net.kyori:adventure-bom:4.15.0"))
    api("net.kyori:adventure-api:4.15.0")
    api("net.kyori:adventure-text-minimessage:4.15.0")
    api("net.kyori:adventure-text-serializer-gson:4.15.0")
    api("net.kyori:adventure-text-serializer-legacy:4.15.0")

    // Configuration
    api("org.spongepowered:configurate-hocon:4.1.2")
    api("org.spongepowered:configurate-gson:4.1.2")

    // Dependency Injection
    api("com.google.inject:guice:7.0.0") {
        exclude(group = "com.google.guava", module = "guava")
    }

    // JSON //yaml
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.yaml:snakeyaml:2.0")

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
}