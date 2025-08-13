plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.victorhugo.boleiragem"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.victorhugo.boleiragem"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.3.0"

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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation(libs.androidx.material3)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation("androidx.navigation:navigation-common-ktx:2.6.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.6.0")

    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Material Icons (Extended)
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Material 3 Components
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0")

    // Opcional: suporte para componentes experimentais
    implementation("androidx.compose.material3:material3-android:1.2.0")
    implementation("androidx.compose.material:material:1.6.0")

    // Animations
    implementation("androidx.compose.animation:animation:1.6.0")
    implementation("androidx.compose.animation:animation-core:1.6.0")
    implementation("androidx.compose.animation:animation-graphics:1.6.0")

    // Google Maps para Compose
    implementation("com.google.maps.android:maps-compose:2.15.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.libraries.places:places:3.3.0")

    // KotlinX Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)
    implementation(libs.inputmapping)
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Gson para serialização/deserialização JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // SplashScreen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}