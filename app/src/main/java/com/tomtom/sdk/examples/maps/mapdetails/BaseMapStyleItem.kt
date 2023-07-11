package com.tomtom.sdk.examples.maps.mapdetails

import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.style.StyleMode

class BaseMapStyleItem(title: String, imageId: Int, styleMode: StyleMode, styleDescriptor: StyleDescriptor) : MapDetailItemBase(title, imageId) {
    private var _styleMode: StyleMode = styleMode
    private var _styleDescriptor: StyleDescriptor = styleDescriptor

    val styleMode: StyleMode
        get() {
            return this._styleMode
        }

    val styleDescriptor: StyleDescriptor
        get() {
            return this._styleDescriptor
        }
}