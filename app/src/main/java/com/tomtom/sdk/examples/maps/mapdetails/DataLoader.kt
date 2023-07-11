package com.tomtom.sdk.examples.maps.mapdetails

import com.tomtom.sdk.examples.R
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.style.StyleMode

object DataLoader {
    fun initMapPreferencesList(): ArrayList<MapPreferenceItem> {
        return ArrayList<MapPreferenceItem>().apply {
            add(MapPreferenceItem("Hill Shading", R.drawable.img_tomtom_light_hill_shading_on, R.drawable.img_tomtom_light_hill_shading_off, MapPreference.SHOW_HILL_SHADING, MapPreference.HIDE_HILL_SHADING))
            add(MapPreferenceItem("Traffic Flow", R.drawable.img_tomtom_light_traffic_flow_on, R.drawable.img_tomtom_light_traffic_flow_off, MapPreference.SHOW_TRAFFIC_FLOW, MapPreference.HIDE_TRAFFIC_FLOW))
            add(MapPreferenceItem("Vehicle Restriction", R.drawable.img_tomtom_light_truck_restrictions_on, R.drawable.img_tomtom_light_truck_restrictions_off, MapPreference.SHOW_VEHICLE_RESTRICTIONS, MapPreference.HIDE_VEHICLE_RESTRICTIONS))
        }
    }

    fun initBaseMapStyleList(): ArrayList<BaseMapStyleItem> {
        return ArrayList<BaseMapStyleItem>().apply {
            add(BaseMapStyleItem("Light Browsing", R.drawable.img_tomtom_light_road_mode_browsing, StyleMode.MAIN, StandardStyles.BROWSING))
            add(BaseMapStyleItem("Light Driving", R.drawable.img_tomtom_light_road_mode_driving, StyleMode.MAIN, StandardStyles.DRIVING))
            add(BaseMapStyleItem("Dark Browsing", R.drawable.img_tomtom_dark_road_mode_browsing, StyleMode.DARK, StandardStyles.BROWSING))
            add(BaseMapStyleItem("Dark Driving", R.drawable.img_tomtom_dark_road_mode_driving, StyleMode.DARK, StandardStyles.DRIVING))
            add(BaseMapStyleItem("Satellite", R.drawable.img_tomtom_satellite_mode, StyleMode.MAIN, StandardStyles.SATELLITE))
        }
    }
}