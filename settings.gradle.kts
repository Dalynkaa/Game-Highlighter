pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://jitpack.io")
        maven("https://maven.fabricmc.net")
    }
}
plugins {
    id("dev.kikugie.stonecutter") version "0.5.1"
}
stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions("1.21.1","1.21.2", "1.21.4","1.21.5")
        vcsVersion = "1.21.1"
    }
}

rootProject.name = "highlighter"