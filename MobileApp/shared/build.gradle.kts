@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.roborazzi)
}

kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("mobile") {
                group("ios")
                // Match by platformType so the new AGP 9 KMP library Android target is included.
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
            }

            group("nonMobile") {
                withJs()
                withWasmJs()
                withJvm()
            }

            group("nonWeb") {
                group("ios")
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                withJvm()
            }
        }
    }

    android {
        namespace = "io.github.vietdsk6.porta.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        withHostTest { isIncludeAndroidResources = true }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
            // Explicit bundle id for crash symbolication; silences the "Cannot infer a bundle ID" link warning.
            binaryOption("bundleId", "io.github.vietdsk6.porta.shared")
        }
    }

    jvm()
    // Browser test tasks need a headless browser (Karma) that isn't available in CI / `check`,
    // and they'd just re-run the same commonTest already covered by jvmTest + testAndroidHostTest.
    // Disable the test run only; the browser() app target is unaffected.
    js { browser { testTask { enabled = false } } }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser { testTask { enabled = false } } }

    sourceSets {
        commonMain.dependencies {
            api(projects.engine)
            implementation(projects.designsystem)
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.koin.core)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material.icons)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.napier)
            implementation(libs.room.runtime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation3)
            implementation(libs.lifecyle.runtime)
            implementation(libs.uuid)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.nav3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
        }

        // Android host tests run on the JVM via Robolectric. Roborazzi snapshots
        // every @Preview discovered by ComposablePreviewScanner.
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.robolectric)
                implementation(libs.roborazzi)
                implementation(libs.roborazzi.compose)
                implementation(libs.roborazzi.junit.rule)
                implementation(libs.roborazzi.compose.preview.scanner.support)
                implementation(libs.composable.preview.scanner.android)
                implementation(libs.androidx.uitest.junit4)
                implementation(libs.androidx.uitest.testManifest)
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
        }

        getByName("nonWebMain") {
            dependencies {
                implementation(libs.sqlite.bundled)
            }
        }

        getByName("webMain") {
            dependencies {
                implementation(libs.sqlite.web)
                implementation(npm("@sqlite.org/sqlite-wasm", "3.50.1-build1"))
                implementation(npm("sqlite-wasm-worker", project.file("sqlite-wasm-worker")))
            }
        }
    }
}

compose.resources {
    packageOfResClass = "io.github.vietdsk6.porta.generated.resources"
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/androidHostTest/snapshots"))
}

// Propagate `-PgenerateStoreScreenshots=true` to the test JVM as a system property so
// StoreScreenshotGeneratorTest's parameter list opens up. Without the flag the test
// is a no-op, keeping PR-check runs fast.
tasks.withType<Test>().configureEach {
    systemProperty(
        "generateStoreScreenshots",
        providers.gradleProperty("generateStoreScreenshots").orElse("false").get(),
    )

    // Run tests on JDK 21 while everything still COMPILES against 17 (jvmToolchain(17)).
    // Robolectric loads real dependency bytecode, and filekit >= 0.14 ships Java 21 class
    // files (major 65). On a 17 test JVM the preview scanner dies with
    // `UnsupportedClassVersionError: io/github/vinceglb/filekit/PlatformFile`, which takes
    // out the whole Roborazzi run. The foojay resolver in settings.gradle.kts provisions
    // the JDK automatically, so no manual install is needed.
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

tasks.register("generateStoreScreenshots") {
    group = "porta"
    description = "Render every @Preview @StoreScreenshot into distribution/store_screenshots/."
    dependsOn("recordRoborazziAndroidHostTest")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
    add("kspWasmJs", libs.room.compiler)
    add("kspJs", libs.room.compiler)

    // Compose UI tooling — runtime-only artifact for inspecting the composition tree at runtime.
    // Per the JetBrains KMP-library AGP 9 skill, `androidRuntimeClasspath` keeps it off compile.
    "androidRuntimeClasspath"(libs.compose.uiTooling)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
    packageName("io.github.vietdsk6.porta.common")
    // AGP 9's `com.android.kotlin.multiplatform.library` plugin does NOT generate BuildConfig the way the
    // old `com.android.application` plugin did. Define the `DEBUG` flag here so commonMain code that
    // depended on `BuildConfig.DEBUG` keeps working. Defaults to `false` (production-safe); the consuming
    // app can override via `-PisDebugBuild=true`.
    buildConfigField(
        "DEBUG",
        (project.findProperty("isDebugBuild") as String?)?.toBoolean() ?: false,
    )
}
