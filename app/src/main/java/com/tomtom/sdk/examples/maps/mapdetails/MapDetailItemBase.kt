package com.tomtom.sdk.examples.maps.mapdetails

open class MapDetailItemBase(title: String, imageId: Int) {
    private var _title: String = title
    private var _imageId: Int = imageId

    val title: String
        get() {
            return this._title
        }

    val imageId: Int
        get() {
            return this._imageId
        }
}