import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.hilt.android)
}

// Load keystore properties from keystore.properties file
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

android {
    signingConfigs {
        create("config") {
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storeFile = keystoreProperties["storeFile"]?.let { file(it.toString()) }
            storePassword = keystoreProperties["storePassword"] as String?
        }
    }

    namespace = "al.ahgitdevelopment.municion"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "al.ahgitdevelopment.municion"
        minSdk = 26
        targetSdk = 36
        versionCode = 37
        versionName = "3.0.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("config")
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            isMinifyEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Enable K2 compiler (Kotlin 2.0+)
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // ========== KOTLIN STDLIB ==========
    implementation(libs.kotlin.stdlib)

    // ========== ANDROIDX CORE ==========
    implementation(libs.bundles.androidx.core)
    implementation(libs.material)

    // ========== ROOM DATABASE ==========
    implementation(libs.bundles.room)
    kapt(libs.androidx.room.compiler)

    // ========== HILT DEPENDENCY INJECTION ==========
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // ========== LIFECYCLE ==========
    implementation(libs.bundles.lifecycle)

    // ========== COROUTINES ==========
    implementation(libs.bundles.coroutines)

    // ========== NAVIGATION COMPONENT ==========
    implementation(libs.bundles.navigation)

    // ========== DATASTORE ==========
    implementation(libs.androidx.datastore.preferences)

    // ========== BIOMETRIC + SECURITY ==========
    implementation(libs.bundles.security)

    // ========== WORK MANAGER ==========
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)

    // ========== FIREBASE ==========
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // ========== UTILITIES ==========
    implementation(libs.gson)

    // ========== IMAGE LOADING ==========
    implementation(libs.bundles.coil)

    // ========== JETPACK COMPOSE ==========
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Core Compose
    implementation(libs.bundles.compose.core)

    // Activity + Navigation Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Lifecycle + ViewModel Compose
    implementation(libs.bundles.lifecycle.compose)

    // Hilt + Compose
    implementation(libs.androidx.hilt.navigation.compose)

    // Serialization para Navigation type-safe
    implementation(libs.kotlinx.serialization.json)

    // Compose Debug tools
    debugImplementation(libs.bundles.compose.debug)

    // ========== TESTING ==========
    // JUnit 5
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // MockK
    testImplementation(libs.bundles.mockk)

    // Coroutines testing
    testImplementation(libs.kotlinx.coroutines.test)

    // Room testing
    testImplementation(libs.androidx.room.testing)

    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)

    // Espresso UI testing
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.fragment.testing)

    // LeakCanary (debug only)
    debugImplementation(libs.leakcanary)
}
