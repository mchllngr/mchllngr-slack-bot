plugins {
    kotlin("jvm") version "1.5.20"
    id("application")
    id("com.github.ben-manes.versions") version "0.39.0" // ./gradlew dependencyUpdates -Drevision=release
}

repositories {
    mavenCentral()
}

group = "de.check24.hamappbot"
version = "2.0"

application {
    mainClass.set("BotKt")
}

dependencies {
    val slack_bolt_version = "1.8.1"
    val javax_websocket_version = "1.1"
    val tyrus_version = "1.17"
    val slf4j_version = "1.7.31"

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.slack.api:bolt-socket-mode:$slack_bolt_version")
    implementation("javax.websocket:javax.websocket-api:$javax_websocket_version")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:$tyrus_version")
    implementation("org.slf4j:slf4j-simple:$slf4j_version")
}
