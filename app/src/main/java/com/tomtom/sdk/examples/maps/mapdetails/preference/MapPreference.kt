package com.tomtom.sdk.examples.maps.mapdetails.preference

data class MapPreference(
    val title: String,
    val preferenceEnabledImageId: Int,
    val preferenceDisabledImageId: Int,
    val preferenceType: MapPreferenceType,
)
