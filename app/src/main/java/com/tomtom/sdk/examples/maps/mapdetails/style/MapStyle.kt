package com.tomtom.sdk.examples.maps.mapdetails.style

import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.style.StyleMode

data class MapStyle(
    val title: String,
    val imageId: Int,
    val styleMode: StyleMode,
    val styleDescriptor: StyleDescriptor,
)
