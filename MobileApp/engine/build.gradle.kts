plugins {
    id("configure-kmp-library-module")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        namespace = "io.github.vietdsk6.porta.engine"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
