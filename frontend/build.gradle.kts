dependencies {
    intellijPlatform {
        bundledPlugin("org.jetbrains.plugins.terminal")
        bundledModule("intellij.platform.frontend")
        bundledModule("intellij.platform.rpc")
        bundledModule("intellij.terminal.frontend")
    }

    implementation(project(":shared"))
}
