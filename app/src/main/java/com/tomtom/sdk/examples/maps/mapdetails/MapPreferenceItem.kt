package com.tomtom.sdk.examples.maps.mapdetails

data class MapPreferenceItem(
    override val title: String,
    val imageIdShow: Int,
    val imageIdHide: Int,
    val methodShow: MapPreference,
    val methodHide: MapPreference) : MapDetailItemBase(title, imageIdShow) {
}