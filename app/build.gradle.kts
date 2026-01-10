plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id  ("kotlin-kapt")
}

android {
    namespace = "com.kredily.tracking"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.kredily.tracking"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.google.android.gms:play-services-location:21.0.1")

    // Room
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")

    // Lifecycle
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}