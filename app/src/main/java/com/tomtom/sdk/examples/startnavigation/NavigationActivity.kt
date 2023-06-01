package com.tomtom.sdk.examples.startnavigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.tomtom.sdk.examples.BuildConfig
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.navigation.NavigationConfiguration
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.TomTomNavigationFactory
import com.tomtom.sdk.navigation.routereplanner.RouteReplanner
import com.tomtom.sdk.navigation.routereplanner.online.OnlineRouteReplannerFactory
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.*
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle

class NavigationActivity : AppCompatActivity() {
    private lateinit var tomTomNavigation: TomTomNavigation

    private lateinit var locationProvider: LocationProvider
    private lateinit var routePlanner: RoutePlanner
    private lateinit var routeReplanner: RouteReplanner
    private var route: Route? = null
    private var routePlanningOptions: RoutePlanningOptions? = null

    private val apiKey = BuildConfig.TOMTOM_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRouting()
        planRoute(amsterdam, rotterdam)
    }

    private fun initRouting() {
        routePlanner = OnlineRoutePlanner.create(context = this, apiKey = apiKey)
        routeReplanner = OnlineRouteReplannerFactory.create(routePlanner)
    }

    private fun planRoute(origin: GeoPoint, destination: GeoPoint) {
        val itinerary = Itinerary(origin = origin, destination = destination)
        routePlanningOptions = RoutePlanningOptions(
            itinerary = itinerary,
            guidanceOptions = GuidanceOptions(progressPoints = ProgressPoints.All),
            vehicle = Vehicle.Car()
        )
        routePlanner.planRoute(routePlanningOptions!!, routePlanningCallback)
    }

    private val routePlanningCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResponse) {
            route = result.routes.first()
            initLocationProvider()
            initNavigation()
            startNavigation()
        }

        override fun onFailure(failure: RoutingFailure) {}

        override fun onRoutePlanned(route: Route) = Unit
    }

    private fun initLocationProvider() {
        val routeGeoLocations = route!!.geometry.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(routeGeoLocations)
        locationProvider = SimulationLocationProvider.create(strategy = simulationStrategy)
        locationProvider.enable()
    }

    private fun initNavigation() {
        val navigationConfiguration = NavigationConfiguration(
            context = this,
            apiKey = apiKey,
            locationProvider = locationProvider,
            routeReplanner = routeReplanner
        )
        tomTomNavigation = TomTomNavigationFactory.create(navigationConfiguration)
    }

    private fun startNavigation() {
        tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)

        val routePlan = RoutePlan(route!!, routePlanningOptions!!)
        val navigationOptions = NavigationOptions(routePlan)
        tomTomNavigation.start(navigationOptions)        
    }

    private val progressUpdatedListener = ProgressUpdatedListener { routeProgress ->
        val distanceAlongRoute = routeProgress.distanceAlongRoute
        val remainingTimeOnRoute = routeProgress.remainingTime
        Log.v("NavigationtionActivity", "distanceAlongRoute: " + distanceAlongRoute)
        Log.v("NavigationActivity", "remainingTimeOnRoute: " + remainingTimeOnRoute)
    }

    companion object {
        val amsterdam = GeoPoint(
            latitude = 52.37616,
            longitude = 4.90828
        )

        val rotterdam = GeoPoint(
            latitude = 51.90546,
            longitude = 4.46662
        )
    }
}
