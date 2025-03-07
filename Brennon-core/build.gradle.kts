plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.lucko.me/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.diogotc.com/snapshots/")
    maven("https://mvn.exceptionflug.de/repository/exceptionflug-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.endoy.dev/endoy-public")
    maven("https://repository.apache.org/content/repositories/snapshots/")
}

dependencies {
    implementation("dev.endoy.configuration:ConfigurationAPI:4.0.2") //

    implementation( "io.lettuce:lettuce-core:6.5.4.RELEASE") //
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation("com.google.code.gson:gson:2.10") //
    implementation("com.google.guava:guava:31.1-jre") //
    implementation("org.mozilla:rhino:1.7.14") //
    implementation("de.christophkraemer:rhino-script-engine:1.2.1") //
    implementation("org.apache.commons:commons-pool2:2.11.1") //
    implementation("org.jsoup:jsoup:1.18.3") //
    implementation("net.luckperms:api:5.4") //
    implementation("com.rabbitmq:amqp-client:5.18.0") //
    implementation("dev.simplix:protocolize-api:2.4.1") //
    implementation("com.rexcantor64.triton:api:4.0.0-SNAPSHOT") //
    implementation("org.mongodb:mongo-java-driver:3.12.11")  //
    implementation("com.zaxxer:HikariCP:5.0.1") //


    implementation("io.github.karlatemp:unsafe-accessor:1.7.0") //
    implementation("me.lucko:jar-relocator:1.5") //
    implementation("net.kyori:adventure-api:4.13.1") //
    implementation("net.kyori:adventure-text-serializer-legacy:4.13.1") //
    implementation("net.kyori:adventure-text-minimessage:4.13.1") //

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0") //
    testImplementation("org.testcontainers:testcontainers:1.17.6") //
    testImplementation("org.testcontainers:postgresql:1.17.6") //
    testImplementation("org.testcontainers:mysql:1.17.6") //
    testImplementation("mysql:mysql-connector-java:8.0.30") //
    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.0.6") //
    testImplementation("org.yaml:snakeyaml:2.0") //
    testImplementation("org.postgresql:postgresql:42.7.2") //
    testImplementation("org.xerial:sqlite-jdbc:3.41.2.2") //
    testImplementation("org.mockito:mockito-core:4.8.0") //
    testImplementation("org.assertj:assertj-core:3.23.1") //

}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}