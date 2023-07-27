package com.tomtom.sdk.examples.maps.mapdetails

/*
This enum aims to store at one place all possible preferences options to achieve the desired map along with their respective method declaration.
 */
enum class MapPreference(val methodName: String) {
    SHOW_TRAFFIC_FLOW("showTrafficFlow"),
    HIDE_TRAFFIC_FLOW("hideTrafficFlow"),
    SHOW_HILL_SHADING("showHillShading"),
    HIDE_HILL_SHADING("hideHillShading"),
    SHOW_VEHICLE_RESTRICTIONS("showVehicleRestrictions"),
    HIDE_VEHICLE_RESTRICTIONS("hideVehicleRestrictions")
}