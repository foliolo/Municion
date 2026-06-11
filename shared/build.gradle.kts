import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ktlint.jlleitschuh)
    alias(libs.plugins.mokkery)
}

val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

fun localProperty(name: String): String = localProperties.getProperty(name) ?: ""

kotlin {
    compilerOptions {
        // Room generates an `actual object` for @ConstructedBy; silences the KT-61573 beta note.
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
        iosTarget.compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    // Required by GitLive Firebase, Room/Native and purchases-kmp iOS bindings.
                    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
    }

    androidLibrary {
        namespace = "al.ahgitdevelopment.municion.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        // GitLive and kotlin.uuid publish bytecode built at JVM 17.
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            // MANDATORY: without this, Compose resources in this KMP library are not
            // packaged into the consuming :androidApp APK under AGP 9 (CMP-9547).
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Navigation
            implementation(libs.androidx.navigation.compose)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // DI (Koin)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)

            // Room runtime (annotation processors wired separately via KSP)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Settings (KMP key-value store)
            implementation(libs.multiplatform.settings.noArg)
            implementation(libs.multiplatform.settings.coroutines)

            // Images
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.ktor.client.core)

            // Gallery/file picker (Android + iOS, no cinterop)
            implementation(libs.filekit.dialogs.compose)

            // Firebase (multiplatform via GitLive) — Munición uses Realtime Database
            implementation(libs.firebase.gitlive.common)
            implementation(libs.firebase.gitlive.auth)
            implementation(libs.firebase.gitlive.database)
            implementation(libs.firebase.gitlive.storage)
            implementation(libs.firebase.gitlive.analytics)
            implementation(libs.firebase.gitlive.crashlytics)

            // RevenueCat (common API; iOS native dep handled by Gradle, no SPM/cocoapods)
            implementation(libs.purchases.kmp.core)
            implementation(libs.purchases.kmp.result)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.turbine)
            implementation(libs.mokkery.runtime)
        }

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)

            // Coroutines Android dispatcher
            implementation(libs.kotlinx.coroutines.android)

            // Ktor engine (Coil network on Android)
            implementation(libs.ktor.client.okhttp)

            // Koin Android
            implementation(libs.koin.android)

            // Background sync (WorkManager) + EXIF image rotation
            implementation(libs.androidx.work.runtime)
            implementation(libs.androidx.exifinterface)

            // Native Firebase Android (App Check provider factories + FCM — not in GitLive)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.appcheck.playintegrity)
            implementation(libs.firebase.messaging)

            // AdMob
            implementation(libs.play.services.ads)
            implementation(libs.user.messaging.platform)
        }

        iosMain.dependencies {
            // Ktor engine (Coil network on iOS)
            implementation(libs.ktor.client.darwin)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "al.ahgitdevelopment.municion.resources"
    generateResClass = always
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Room KSP per target. App Check Debug provider is wired in :androidApp (debug only).
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    androidRuntimeClasspath(libs.compose.uiTooling)
}

buildConfig {
    packageName("al.ahgitdevelopment.municion")
    useKotlinOutput { internalVisibility = false }

    // Release builds inject -PappBuildType=release; local/dev fall back to "debug" so
    // development never touches production data.
    val appBuildType = (project.findProperty("appBuildType") as String?) ?: "debug"
    buildConfigField("BUILD_TYPE", appBuildType)

    // AdMob — per-platform unit ids (banner + native advanced). BuildConfig is common; the
    // per-platform selector lives in AdUnitIds (expect/actual). Field names mirror local.properties.
    buildConfigField("ADMOB_APPLICATION_ID", localProperty("ADMOB_APPLICATION_ID"))
    buildConfigField("ADMOB_BOTTOM_BANNER_ID_ANDROID", localProperty("ADMOB_BOTTOM_BANNER_ID_ANDROID"))
    buildConfigField("ADMOB_NATIVE_ADVANCED_ID_ANDROID", localProperty("ADMOB_NATIVE_ADVANCED_ID_ANDROID"))
    buildConfigField("ADMOB_APPLICATION_ID_IOS", localProperty("ADMOB_APPLICATION_ID_IOS"))
    buildConfigField("ADMOB_BOTTOM_BANNER_ID_IOS", localProperty("ADMOB_BOTTOM_BANNER_ID_IOS"))
    buildConfigField("ADMOB_NATIVE_ADVANCED_ID_IOS", localProperty("ADMOB_NATIVE_ADVANCED_ID_IOS"))

    // RevenueCat SDK keys (remove-ads entitlement).
    buildConfigField("REVENUECAT_PLAY_SDK_KEY", localProperty("REVENUECAT_PLAY_SDK_KEY"))
    buildConfigField("REVENUECAT_PLAY_SDK_KEY_TEST", localProperty("REVENUECAT_PLAY_SDK_KEY_TEST"))
    buildConfigField("REVENUECAT_APPSTORE_SDK_KEY", localProperty("REVENUECAT_APPSTORE_SDK_KEY"))
}

ktlint {
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
        exclude { element ->
            element.file.path.contains("${File.separator}build${File.separator}generated${File.separator}")
        }
        include("**/kotlin/**")
    }
}
