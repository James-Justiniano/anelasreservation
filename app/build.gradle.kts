plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") // Add Kotlin Serialization plugin
}

android {
    namespace = "com.example.anelasreservationsystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.anelasreservationsystem"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8" // Ensure Kotlin uses Java 8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Using Firebase BOM for dependency management
    implementation(platform("com.google.firebase:firebase-bom:33.2.0")) // Ensure this is the latest BOM version

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth") // Uses version defined in BOM

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database") // Uses version defined in BOM

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics") // Optional: Add if you want to use Analytics

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.storage)
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Add Kotlin Serialization dependencies if needed
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Add Gson dependency
    implementation("com.google.code.gson:gson:2.10.1") // Ensure this is the latest version

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}