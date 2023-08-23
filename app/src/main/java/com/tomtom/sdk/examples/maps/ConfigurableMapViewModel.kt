package com.tomtom.sdk.examples.maps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tomtom.sdk.examples.maps.mapdetails.style.MapStyle
import com.tomtom.sdk.examples.maps.mapdetails.DataLoader
import com.tomtom.sdk.examples.maps.mapdetails.preference.MapPreference
import com.tomtom.sdk.utils.SingleLiveEvent

class ConfigurableMapViewModel(application: Application) : AndroidViewModel(application) {

    val baseMapStyles = DataLoader.getBaseMapStyles(application)
    val mapPreferences = DataLoader.getMapPreferences(application)

    var currentStyleItem = baseMapStyles.first()
        private set

    val mapPreferenceChanged = SingleLiveEvent<Pair<MapPreference, Boolean>>()

    fun changeMapStyle(newMapStyle: MapStyle) {
        if (currentStyleItem != newMapStyle) {
            currentStyleItem = newMapStyle
        }
    }

    fun changeMapPreference(mapPreference: MapPreference, isEnabled: Boolean) {
        mapPreferences[mapPreference] = isEnabled
        mapPreferenceChanged.postValue(Pair(mapPreference, isEnabled))
    }
}
