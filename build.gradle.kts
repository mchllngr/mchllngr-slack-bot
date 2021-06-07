plugins {
    kotlin("jvm") version "1.5.10"
    id("application")
}

repositories {
    mavenCentral()
}

group = "de.mchllngr"
version = "1.0"

application {
    mainClass.set("BotKt")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.slack.api:bolt-socket-mode:1.8.1")
    implementation("javax.websocket:javax.websocket-api:1.1")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:1.17")
    implementation("org.slf4j:slf4j-simple:1.7.30")
}
