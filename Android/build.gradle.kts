// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val kotlinVersion="1.9.20"
    val kspVersion="1.9.20-1.0.14"
    id("com.android.application") version "8.1.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.appdistribution") version "4.0.0" apply false
    id("org.sonarqube") version "4.4.1.3373"
    id("com.google.devtools.ksp") version kspVersion apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    kotlin("jvm") version kotlinVersion apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

sonar {
    properties {
        property("sonar.projectKey", "alexgolovko_Android_DeepMapper")
        property("sonar.organization", "alexgolovko")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}


tasks {
    register("release") {
        dependsOn("app:assembleRelease", "app:appDistributionUploadRelease")
        description = "Builds and distributes the release version of the app."
    }
}