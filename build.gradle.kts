@file:Suppress("GradlePackageUpdate")

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
    id("com.github.gmazzo.buildconfig")
}

repositories {
    mavenCentral()
}

group = "de.mchllngr.slackbot"
version = System.getenv("SLACK_BOT_VERSION") ?: "local"

val compileKotlin: KotlinCompile by tasks
val compileTestKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_18.toString()
    freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
compileTestKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_18.toString()
}

application {
    mainClass.set("BotKt")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:_"))
    implementation(Kotlin.stdlib.jdk8)
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

buildConfig {
    packageName("buildconfig")

    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String", "COMMIT_HASH", "\"${System.getenv("SLACK_BOT_COMMIT_HASH") ?: "local"}\"")
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
