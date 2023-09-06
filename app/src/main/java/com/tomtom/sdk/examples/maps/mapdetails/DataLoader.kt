package com.tomtom.sdk.examples.maps.mapdetails

import android.content.Context
import android.net.Uri
import com.tomtom.sdk.examples.BuildConfig
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.maps.mapdetails.preference.MapPreferenceType
import com.tomtom.sdk.examples.maps.mapdetails.preference.MapPreference
import com.tomtom.sdk.examples.maps.mapdetails.style.MapStyle
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.style.StyleMode

object DataLoader {

    fun getMapPreferences(context: Context): MutableMap<MapPreference, Boolean> = mutableMapOf(
        MapPreference(
            context.getString(R.string.hill_shading),
            R.drawable.img_tomtom_light_hill_shading_on,
            R.drawable.img_tomtom_light_hill_shading_off,
            MapPreferenceType.SHOW_HILL_SHADING,
        ) to false,
        MapPreference(
            context.getString(R.string.house_numbers),
            R.drawable.img_tomtom_light_house_numbers_on,
            R.drawable.img_tomtom_light_house_numbers_off,
            MapPreferenceType.SHOW_HOUSE_NUMBERS,
        ) to false,
        MapPreference(
            context.getString(R.string.traffic_flow),
            R.drawable.img_tomtom_light_traffic_flow_on,
            R.drawable.img_tomtom_light_traffic_flow_off,
            MapPreferenceType.SHOW_TRAFFIC_FLOW,
        ) to false,
        MapPreference(
            context.getString(R.string.vehicle_restriction),
            R.drawable.img_tomtom_light_truck_restrictions_on,
            R.drawable.img_tomtom_light_truck_restrictions_off,
            MapPreferenceType.SHOW_VEHICLE_RESTRICTIONS,
        ) to false,
        MapPreference(
            context.getString(R.string.lane_highlighting),
            R.drawable.img_tomtom_light_lane_level_guidance_on,
            R.drawable.img_tomtom_light_lane_level_guidance_off,
            MapPreferenceType.SHOW_LANE_HIGHLIGHTING,
        ) to false,
    )

    fun getBaseMapStyles(context: Context): ArrayList<MapStyle> = arrayListOf(
        MapStyle(
            context.getString(R.string.light_browsing),
            R.drawable.img_tomtom_light_road_mode_browsing,
            StyleMode.MAIN,
            StandardStyles.BROWSING,
        ),
        MapStyle(
            context.getString(R.string.light_driving),
            R.drawable.img_tomtom_light_road_mode_driving,
            StyleMode.MAIN,
            StandardStyles.DRIVING,
        ),
        MapStyle(
            context.getString(R.string.dark_browsing),
            R.drawable.img_tomtom_dark_road_mode_browsing,
            StyleMode.DARK,
            StandardStyles.BROWSING,
        ),
        MapStyle(
            context.getString(R.string.dark_driving),
            R.drawable.img_tomtom_dark_road_mode_driving,
            StyleMode.DARK,
            StandardStyles.DRIVING,
        ),
        MapStyle(
            context.getString(R.string.monochrome),
            R.drawable.img_tomtom_monochrome_style,
            StyleMode.MAIN,
            StyleDescriptor(
                uri = Uri.parse(
                    "https://api.tomtom.com/style/1/style/22.2.*" +
                            "?key=${BuildConfig.TOMTOM_API_KEY}" +
                            "&map=2/basic_mono-light" +
                            "&traffic_incidents=2/incidents_light" +
                            "&traffic_flow=2/flow_relative-light" +
                            "&hillshade=2-test/hillshade_dem"
                )
            ),
        ),
        MapStyle(
            context.getString(R.string.satellite),
            R.drawable.img_tomtom_satellite_mode,
            StyleMode.MAIN,
            StandardStyles.SATELLITE,
        ),
    )
}
