/*
 * Copyright © 2023. Change this to your own official copyright statement.
 */

plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.usecase"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "TOMTOM_API_KEY", "\"${extra["tomtomApiKey"].toString()}\"")
        buildConfigField("String", "NDS_MAP_LICENSE", "\"${extra["ndsMapLicense"].toString()}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // TomTom SDK dependencies.
    implementation(libs.dataProviderOffline)
    implementation(libs.dataStorageUpdater)
    implementation(libs.onboardNdsStore)
    implementation(libs.featureToggle)
    implementation(libs.locationProvider)
    implementation(libs.locationSimulation)
    implementation(libs.locationMapmatched)
    implementation(libs.mapsDisplay)
    implementation(libs.navigationOffline)
    implementation(libs.navigationUi)
    implementation(libs.routeReplannerOffline)
    implementation(libs.styleProviderOffline)

    // Default NDS map.
    implementation(libs.defaultMap)

    // Android dependencies.
    implementation(libs.bundles.androidCommon)
}
