package com.tomtom.sdk.examples.maps.mapdetails.preference

fun interface MapPreferenceChangeListener {
    fun onMapPreferenceChange(mapPreference: MapPreference, isEnabled: Boolean)
}
