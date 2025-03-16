plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    id("kotlin-kapt")
    alias(libs.plugins.compose.compiler)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.testapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.testapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    hilt {
        enableAggregatingTask = false
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Retrofit
    implementation (libs.retrofit)
    implementation(libs.logging.interceptor.v4100)
    implementation (libs.converter.moshi)
    implementation(libs.moshi.adapters)
    implementation (libs.converter.gson)
    implementation (libs.squareup.moshi.kotlin)

    //Firebase
    implementation(libs.firebase.storage.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    //Coil
    implementation (libs.coil.compose)
    implementation (libs.coil.gif)

    //Navigation
    implementation (libs.androidx.navigation.compose)

    //Datastore
    implementation (libs.androidx.datastore.preferences)

    //JWT
    implementation (libs.android.jwtdecode)

    //Hilt
    implementation (libs.hilt.android)
    kapt (libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    //Constraint
    implementation (libs.androidx.constraintlayout.compose)

    //Scalable DP and SP
    implementation (libs.sdp.android)
    implementation (libs.ssp.android)

    //Gemini
    implementation(libs.generativeai)
    //OpenAI API
    //implementation(libs.openai.client)
    //implementation(libs.ktor.client.okhttp)
    //implementation(libs.ktor.client.core)
}