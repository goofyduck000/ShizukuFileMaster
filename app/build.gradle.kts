plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" // Pour Compose Compiler
}

android {
    namespace = "com.example.shizukufilemaster"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shizukufilemaster"
        minSdk = 24 // Android 7.0 (Shizuku nécessite min 24)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Met true + proguard pour prod
            signingConfig = signingConfigs.getByName("debug") // Signe en debug pour test direct
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android / Lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM (Bill of Materials) - Gère les versions compatibles
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Shizuku API (La clé de voûte pour accéder à Android/data)
    implementation("com.rikka.shizuku:api:13.5.3")

    // Coroutines & Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Sérialisation JSON (pour config future)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
