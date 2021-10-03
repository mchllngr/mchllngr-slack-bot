import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath(Square.sqlDelight.gradlePlugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm")
    id("application")
    id("com.squareup.sqldelight")
}

repositories {
    mavenCentral()
}

group = "de.check24.hamappbot"
version = "2.0"

val compileKotlin: KotlinCompile by tasks
val compileTestKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_16.toString()
}
compileTestKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_16.toString()
}

application {
    mainClass.set("BotKt")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.slack.api:bolt-socket-mode:_")
    implementation("javax.websocket:javax.websocket-api:_")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:_")
    implementation("org.slf4j:slf4j-simple:_")
    implementation(Square.sqlDelight.drivers.jdbc)
    implementation("com.zaxxer:HikariCP:_")
    implementation("org.mariadb.jdbc:mariadb-java-client:_")
}

sqldelight {
    database("Database") {
        packageName = "db"
        sourceFolders = listOf("sqldelight")
        dialect = "mysql"
        deriveSchemaFromMigrations = true
        verifyMigrations = true
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "BotKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}
