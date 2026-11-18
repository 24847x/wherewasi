import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.aware.SplitModeAware

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.0-RC"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0-RC" apply false
    // Generates the client stubs for @Rpc interfaces (2026.2 is built with Kotlin 2.4.0).
    id("rpc") version "2.4.0-RC-0.1" apply false
    id("org.jetbrains.intellij.platform")
}

group = "com.loudyo.wherewasi"
version = "1.1.1"

val pluginId = "com.loudyo.wherewasi"
val pluginName = "WhereWasI"
val pluginVersion = project.version.toString()
// The TerminalView API used by this plugin is available in the 2026.2+ platform.
val platformVersion = providers.gradleProperty("platformVersion").orElse("2026.2.4")
val pluginSinceBuild = providers.gradleProperty("pluginSinceBuild").orElse("262.8665")

subprojects {
    apply(plugin = "org.jetbrains.intellij.platform.module")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "rpc")

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(platformVersion.get())
        bundledPlugin("org.jetbrains.plugins.terminal")

        pluginModule(implementation(project(":shared")))
        pluginModule(implementation(project(":frontend")))
        pluginModule(implementation(project(":backend")))
    }
}

intellijPlatform {
    // `./gradlew runIde -PsplitMode=true` starts a backend IDE plus a JetBrains Client,
    // the same setup as Gateway connected to a remote IDE.
    splitMode = providers.gradleProperty("splitMode").map { it.toBoolean() }.orElse(false)
    pluginInstallationTarget = SplitModeAware.PluginInstallationTarget.BOTH

    // `./gradlew verifyPlugin` checks the remote-development servers this plugin targets;
    // `-PverifyIdePath=/path/to/IDE` verifies against one local install instead (no download).
    pluginVerification {
        ides {
            val localIde = providers.gradleProperty("verifyIdePath").orNull
            if (localIde != null) {
                local(file(localIde))
            } else {
                listOf("2026.2.1", "2026.2.3").forEach { create(IntelliJPlatformType.IntellijIdeaUltimate, it) }
                create(IntelliJPlatformType.PyCharm, "2026.2.3")
                create(IntelliJPlatformType.Rider, "2026.2.3")
            }
        }
    }

    pluginConfiguration {
        id = pluginId
        name = pluginName
        version = pluginVersion
        changeNotes = """
            <p>1.1.0</p>
            <ul>
              <li>Works with Remote Development (JetBrains Gateway / JetBrains Client), including a Windows client connected to a Linux backend.</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = pluginSinceBuild.get()
        }
        vendor {
            name = "24847x"
        }
    }
    instrumentCode = false
    buildSearchableOptions = false
}

kotlin {
    jvmToolchain(25)
}
