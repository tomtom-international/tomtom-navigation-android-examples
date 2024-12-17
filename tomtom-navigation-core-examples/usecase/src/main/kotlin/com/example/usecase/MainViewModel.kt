package com.example.usecase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.usecase.BuildConfig.TOMTOM_API_KEY
import com.tomtom.quantity.Distance
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStoreConfiguration
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.navigation.ActiveRouteChangedListener
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.online.Configuration
import com.tomtom.sdk.navigation.online.OnlineTomTomNavigationFactory
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.ExtendedSections
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.options.guidance.InstructionPhoneticsType
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle
import com.tomtom.sdk.vehicle.VehicleProviderFactory

class MainViewModel(private val application: Application): AndroidViewModel(application), OnLocationUpdateListener {

    private val applicationContext = application.applicationContext
    private val apiKey = TOMTOM_API_KEY

    private lateinit var navigationTileStore: NavigationTileStore
    lateinit var locationProvider: LocationProvider
        private set
    private lateinit var routePlanner: RoutePlanner
    private lateinit var routePlanningOptions: RoutePlanningOptions
    lateinit var tomTomNavigation: TomTomNavigation
        private set
    private lateinit var navigationFragment: NavigationFragment

    private val _location = MutableLiveData<GeoLocation>()
    val location: LiveData<GeoLocation>
        get() = _location
    private val _routePlan = MutableLiveData<RoutePlan>()
    val routePlan: LiveData<RoutePlan>
        get() = _routePlan
    private val _routingFailure = MutableLiveData<RoutingFailure>()
    val routingFailure: LiveData<RoutingFailure>
        get() = _routingFailure

    private val _distanceAlongRoute = MutableLiveData<Distance>()
    val distanceAlongRoute: LiveData<Distance>
        get() = _distanceAlongRoute

    private val _activeRoute = MutableLiveData<Route>()
    val activeRoute: LiveData<Route>
        get() = _activeRoute

    init {
        initLocationProvider()
        initNavigationTileStore()
        initRouting()
        initNavigation()
    }

    /**
     * The SDK provides a [NavigationTileStore] class that is used between different modules to get tile data based
     * on the online map.
     */
    private fun initNavigationTileStore() {
        navigationTileStore = NavigationTileStore.create(
            context = applicationContext,
            navigationTileStoreConfig = NavigationTileStoreConfiguration(
                apiKey = apiKey
            )
        )
    }

    /**
     * The SDK provides a [LocationProvider] interface that is used between different modules to get location updates.
     * This examples uses the default [LocationProvider].
     * Under the hood, the engine uses Android’s system location services.
     */
    fun initLocationProvider() {
        locationProvider = DefaultLocationProviderFactory.create(context = applicationContext)
    }

    /**
     * Plans the route by initializing by using the online route planner and default route replanner.
     */
    private fun initRouting() {
        routePlanner = OnlineRoutePlanner.create(context = applicationContext, apiKey = apiKey)
    }

    /**
     * To use navigation in the application, start by by initialising the navigation configuration.
     */
    private fun initNavigation() {
        val configuration = Configuration(
            context = applicationContext,
            navigationTileStore = navigationTileStore,
            locationProvider = locationProvider,
            routePlanner = routePlanner,
            vehicleProvider = VehicleProviderFactory.create(vehicle = Vehicle.Car())
        )
        tomTomNavigation = OnlineTomTomNavigationFactory.create(configuration)
    }

    override fun onLocationUpdate(location: GeoLocation) {
        _location.value = location
        locationProvider.removeOnLocationUpdateListener(this)
    }

    fun registerLocationUpdatesListener() {
        locationProvider.addOnLocationUpdateListener(this)
    }

    /**
     * Used to calculate a route using the following parameters:
     * - InstructionPhoneticsType - This specifies whether to include phonetic transcriptions in the response.
     * - ExtendedSections - This specifies whether to include extended guidance sections in the response, such as sections of type road shield, lane, and speed limit.
     */
    fun calculateRoute(origin: GeoPoint, destination: GeoPoint) {
        val itinerary = Itinerary(origin = origin, destination = destination)
        routePlanningOptions = RoutePlanningOptions(
            itinerary = itinerary,
            guidanceOptions = GuidanceOptions(
                phoneticsType = InstructionPhoneticsType.Ipa,
                extendedSections = ExtendedSections.All
            ),
            vehicle = Vehicle.Car()
        )
        routePlanner.planRoute(routePlanningOptions, routePlanningCallback)
    }

    /**
     * The RoutePlanningCallback itself has three methods.
     * - The `onFailure()` method is triggered if the request fails.
     * - The `onSuccess()` method returns RoutePlanningResponse containing the routing results.
     * - The `onRoutePlanned()` method is triggered when each route is successfully calculated.
     *
     * This example draws the first retrieved route on the map.
     * You can show the overview of the added routes using the TomTomMap.zoomToRoutes(Int) method.
     * Note that its padding parameter is expressed in pixels.
     */
    private val routePlanningCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResponse) {
            _routePlan.value = RoutePlan(result.routes.first(), routePlanningOptions)
        }

        override fun onFailure(failure: RoutingFailure) {
            _routingFailure.value = failure
        }

        override fun onRoutePlanned(route: Route) = Unit
    }

    private val progressUpdatedListener = ProgressUpdatedListener {
        _distanceAlongRoute.value = it.distanceAlongRoute
//        tomTomMap.routes.first().progress = it.distanceAlongRoute
    }

    private val activeRouteChangedListener = ActiveRouteChangedListener { route ->
        _activeRoute.value = route
//        tomTomMap.removeRoutes()
//        drawRoute(route)
    }

    fun navigationStarted() {
        tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.addActiveRouteChangedListener(activeRouteChangedListener)
    }

    fun navigationStopped() {
        tomTomNavigation.removeProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.removeActiveRouteChangedListener(activeRouteChangedListener)
        val oldLocationProvider = locationProvider
        initLocationProvider()
        locationProvider.enable()
        oldLocationProvider.close()
    }

    /**
     * Use the SimulationLocationProvider for testing purposes.
     */
    fun setSimulationLocationProviderToNavigation() {
        val route = routePlan.value?.route ?: return
        val routeGeoLocations = route.geometry.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(routeGeoLocations)
        locationProvider = SimulationLocationProvider.create(strategy = simulationStrategy)
        tomTomNavigation.locationProvider = locationProvider
        locationProvider.enable()
    }

    /**
     * Checks whether navigation is currently running.
     */
    fun isNavigationRunning(): Boolean = tomTomNavigation.navigationSnapshot != null

    override fun onCleared() {
        super.onCleared()
        tomTomNavigation.close()
        navigationTileStore.close()
        locationProvider.close()
    }



}