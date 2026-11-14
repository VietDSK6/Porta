import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

dependencies {
    implementation(projects.shared)
    implementation(projects.designsystem)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
}

compose.desktop {
    application {
        mainClass = "io.github.vietdsk6.porta.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.vietdsk6.porta"
            packageVersion = "1.0.0"
            macOS { iconFile.set(project.file("src/main/resources/porta.icns")) }
            windows { iconFile.set(project.file("src/main/resources/porta.ico")) }
            linux { iconFile.set(project.file("src/main/resources/porta-icon.png")) }
            // DataStore Preferences' protobuf runtime uses sun.misc.Unsafe on the JVM.
            // jlink otherwise omits this module and the packaged app remains on its splash.
            modules("jdk.unsupported")
        }
    }
}
