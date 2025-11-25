/*
 * build.gradle.kts (App-level)
 *
 * Gradle build file for the Glyph Matrix Toy Editor Android app.
 * Includes Nothing Glyph Developer Kit (GDK) integration.
 *
 * GDK Setup Instructions:
 * 1. Download the GlyphMatrixSDK.aar from:
 *    https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit
 * 2. Place the .aar file in the app/libs directory
 * 3. Enable Glyph debugging on your Nothing Phone via ADB:
 *    adb shell settings put global nt_glyph_interface_debug_enable 1
 *
 * Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
 * Licensed under the MIT License
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.glyphmatrix.toyeditor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.glyphmatrix.toyeditor"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.11.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // JSON serialization for export
    implementation("com.google.code.gson:gson:2.11.0")

    // Nothing Glyph Matrix SDK
    // To add the SDK:
    // 1. Download GlyphMatrixSDK.aar from the official repository:
    //    https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit
    // 2. Create app/libs directory if it doesn't exist
    // 3. Place the .aar file in app/libs/
    // 4. Uncomment the line below:
    // implementation(files("libs/GlyphMatrixSDK.aar"))

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.11.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
