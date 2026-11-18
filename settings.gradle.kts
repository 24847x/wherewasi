import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // Hosts the `rpc` compiler plugin used for the frontend ↔ backend API.
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.jetbrains.intellij.platform.settings") version "2.20.1"
}

rootProject.name = "where-was-i-plugin"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }
}

// Content modules of the plugin. In split mode (Gateway / JetBrains Client) the frontend module
// loads in the client and the backend module in the remote IDE; a regular IDE loads all of them.
include("shared")
include("frontend")
include("backend")
