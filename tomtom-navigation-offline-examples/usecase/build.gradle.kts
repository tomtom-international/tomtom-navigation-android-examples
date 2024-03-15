/*
 * © 2023 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.usecase"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
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
        allWarningsAsErrors = true
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
    implementation(libs.ndsStore)
    implementation(libs.ndsStoreUpdater)
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
