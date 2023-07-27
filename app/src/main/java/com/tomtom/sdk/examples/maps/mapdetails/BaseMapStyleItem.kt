package com.tomtom.sdk.examples.maps.mapdetails

import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.style.StyleMode

data class BaseMapStyleItem(
    override val title: String,
    override val imageId: Int,
    val styleMode: StyleMode,
    val styleDescriptor: StyleDescriptor) : MapDetailItemBase(title, imageId) {

}