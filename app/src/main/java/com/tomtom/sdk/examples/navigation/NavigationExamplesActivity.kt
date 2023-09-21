/*
 * © 2023 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */
package com.tomtom.sdk.examples.navigation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStoreConfiguration
import com.tomtom.sdk.examples.BuildConfig
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.databinding.ActivityNavigationExamplesBinding
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.online.Configuration
import com.tomtom.sdk.navigation.online.OnlineTomTomNavigationFactory
import com.tomtom.sdk.navigation.routereplanner.online.OnlineRouteReplannerFactory
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.options.guidance.ProgressPoints
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle

class NavigationExamplesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationExamplesBinding
    private lateinit var routePlanner: RoutePlanner
    private lateinit var routePlanningOptions: RoutePlanningOptions
    private lateinit var tomTomNavigation: TomTomNavigation
    private lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigationExamplesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        planRoute()
    }

    override fun onDestroy() {
        super.onDestroy()
        tomTomNavigation.close()
        locationProvider.close()
    }

    private fun planRoute() {
        routePlanner = OnlineRoutePlanner.create(context = this, apiKey = API_KEY)
        val amsterdam = GeoPoint(latitude = 52.37616, longitude = 4.90828)
        val rotterdam = GeoPoint(latitude = 51.90546, longitude = 4.46662)
        val itinerary = Itinerary(origin = amsterdam, destination = rotterdam)
        routePlanningOptions = RoutePlanningOptions(
            itinerary = itinerary,
            guidanceOptions = GuidanceOptions(progressPoints = ProgressPoints.All),
            vehicle = Vehicle.Car()
        )
        routePlanner.planRoute(
            routePlanningOptions = routePlanningOptions,
            callback = routePlanningCallback
        )
    }

    private fun initLocationProvider(route: Route) {
        val routeGeoLocations = route.geometry.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(routeGeoLocations)
        locationProvider = SimulationLocationProvider.create(strategy = simulationStrategy)
        locationProvider.enable()
    }

    private fun initNavigation() {
        val navigationTileStore = NavigationTileStore.create(
            context = this,
            navigationTileStoreConfig = NavigationTileStoreConfiguration(apiKey = API_KEY)
        )
        val routeReplanner = OnlineRouteReplannerFactory.create(routePlanner)
        val configuration = Configuration(
            context = this,
            locationProvider = locationProvider,
            navigationTileStore = navigationTileStore,
            routeReplanner = routeReplanner
        )
        tomTomNavigation = OnlineTomTomNavigationFactory.create(configuration)
        tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)
    }

    private fun startNavigation(route: Route) {
        val routePlan = RoutePlan(route, routePlanningOptions)
        val navigationOptions = NavigationOptions(routePlan)
        tomTomNavigation.start(navigationOptions)
    }

    private val routePlanningCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResponse) {
            val route = result.routes.first()
            initLocationProvider(route)
            initNavigation()
            startNavigation(route)
        }

        override fun onFailure(failure: RoutingFailure) {
            Log.e(TAG, "Unable to calculate a route: " + failure.message)
        }

        override fun onRoutePlanned(route: Route) = Unit
    }

    private val progressUpdatedListener = ProgressUpdatedListener { routeProgress ->
        binding.navProgressUpdates.text = String.format(
            "%s\n%s",
            getString(
                R.string.distance_along_the_route,
                routeProgress.distanceAlongRoute.toString()
            ),
            getString(
                R.string.remaining_travel_time,
                routeProgress.remainingTime.toString()
            )
        )
    }

    companion object {
        private const val TAG = "NavigationExamplesActivity"
        private const val API_KEY = BuildConfig.TOMTOM_API_KEY
    }
}