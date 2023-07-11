package com.tomtom.sdk.examples.maps.mapdetails

interface OnRecyclerViewItemClickListener {
    fun onBaseMapStyleItemClick(baseMapStyleItem: BaseMapStyleItem)
    fun onMapPreferenceItemClick(title: String, mapPreferenceItemMethod: MapPreference)
}