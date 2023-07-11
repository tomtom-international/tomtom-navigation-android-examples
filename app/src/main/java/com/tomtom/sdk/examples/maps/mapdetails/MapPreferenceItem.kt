package com.tomtom.sdk.examples.maps.mapdetails

class MapPreferenceItem(title: String, imageIdShow: Int, imageIdHide: Int, methodShow: MapPreference, methodHide: MapPreference) : MapDetailItemBase(title, imageIdShow) {
    private var _imageIdHide: Int = imageIdHide
    private var _methodShow: MapPreference = methodShow
    private var _methodHide: MapPreference = methodHide

    val imageIdHide: Int
        get() {
            return this._imageIdHide
        }

    val methodShow: MapPreference
        get() {
            return this._methodShow
        }

    val methodHide: MapPreference
        get() {
            return this._methodHide
        }
}