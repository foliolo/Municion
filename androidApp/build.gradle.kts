import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Release signing config from keystore.properties (kept out of VCS).
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) load(FileInputStream(keystorePropertiesFile))
}

// AdMob ids come from local.properties / CI; fall back to Google's public test
// app id so debug builds launch (play-services-ads' init provider validates it).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun localProperty(name: String): String = localProperties.getProperty(name) ?: ""
//FIXME: Handel this variable in a way it is not hardcoded
val admobAppId = localProperty("ADMOB_APPLICATION_ID")
    .ifBlank { "ca-app-pub-3940256099942544~3347511713" }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

android {
    namespace = "al.ahgitdevelopment.municion"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "al.ahgitdevelopment.municion"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Release builds via Fastlane override these with -PappVersionCode/-PappVersionName.
        versionCode = (project.findProperty("appVersionCode") as String?)?.toInt() ?: 46
        versionName = (project.findProperty("appVersionName") as String?) ?: "4.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        resValue("string", "admob_app_id", admobAppId)
    }

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties["storeFile"] as String?
            if (!storeFilePath.isNullOrBlank() && file(storeFilePath).exists()) {
                storeFile = file(storeFilePath)
                storePassword = keystoreProperties["storePassword"] as String?
                keyAlias = keystoreProperties["keyAlias"] as String?
                keyPassword = keystoreProperties["keyPassword"] as String?
            }
        }
    }

    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
            // TODO(release hardening): enable minify + shrink with the R8 rules in
            // proguard-rules.pro once the full feature set is ported (phase 10/11).
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val release = signingConfigs.getByName("release")
            if (release.storeFile != null) {
                signingConfig = release
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        resValues = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            merges.add("META-INF/LICENSE-notice.md")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)

    // Koin BOM aligns koin-android's version (the catalog entry is versionless).
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    // FileKit Core: needed in the Application to manually initialize the picker subsystem
    // (the dialogs/compose artifact lives in :shared as `implementation`, so it is not transitive).
    implementation(libs.filekit.core)

    // Firebase Cloud Messaging (native Android SDK) for push notifications. :shared uses GitLive,
    // but FCM has no GitLive module, so the messaging service lives in the Android app.
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    // Firebase App Check. Play Integrity attests release builds; the Debug provider is added
    // ONLY to debug builds so it never ships in production. The provider is installed per-variant
    // in src/{debug,release}/kotlin/.../AppCheckInstaller.kt.
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
}
