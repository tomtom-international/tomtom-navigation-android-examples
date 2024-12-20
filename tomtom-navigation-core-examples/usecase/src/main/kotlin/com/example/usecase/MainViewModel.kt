package com.example.usecase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.usecase.BuildConfig.TOMTOM_API_KEY
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStoreConfiguration
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.mapmatched.MapMatchedLocationProviderFactory
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualization
import com.tomtom.sdk.map.display.visualization.navigation.NavigationVisualizationFactory
import com.tomtom.sdk.map.display.visualization.routing.RouteClickedListener
import com.tomtom.sdk.map.display.visualization.routing.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.online.Configuration
import com.tomtom.sdk.navigation.online.OnlineTomTomNavigationFactory
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

class MainViewModel(application: Application): AndroidViewModel(application), OnLocationUpdateListener {

    private val applicationContext = application.applicationContext
    private val apiKey = TOMTOM_API_KEY

    lateinit var mapLocationProvider: LocationProvider
        private set
    private lateinit var navigationLocationProvider: LocationProvider
    private var _mapMatchedLocationProvider: LocationProvider? = null
    val mapMatchedLocationProvider: LocationProvider
        get() = checkNotNull(_mapMatchedLocationProvider) {
            "MapMatchedLocationProvider is not initialized"
        }

    private lateinit var routePlanner: RoutePlanner
    private lateinit var navigationTileStore: NavigationTileStore
    lateinit var tomTomNavigation: TomTomNavigation
        private set
    private lateinit var navigationVisualization: NavigationVisualization

    private val _location = MutableLiveData<GeoLocation>()
    val location: LiveData<GeoLocation>
        get() = _location
    private val _routingFailure = MutableLiveData<RoutingFailure>()
    val routingFailure: LiveData<RoutingFailure>
        get() = _routingFailure

    val selectedRoute: Route?
        get() {
            return if (::navigationVisualization.isInitialized) {
                navigationVisualization.selectedRoute
            } else {
                null
            }
        }

    data class NavigationStarted(
        val routePlan: com.tomtom.sdk.navigation.RoutePlan,
        val tomTomNavigation: TomTomNavigation,
        val locationProvider: LocationProvider,
    )
    private var _navigationStarted = MutableLiveData<NavigationStarted>()
    val navigationStarted: LiveData<NavigationStarted>
        get() = _navigationStarted

    private var _routePlanningOptions: RoutePlanningOptions? = null
    val routePlanningOptions: RoutePlanningOptions
        get() = checkNotNull(_routePlanningOptions) {
            "RoutePlanningOptions is not initialized"
        }

    init {
        initMapLocationProvider()
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
    private fun initMapLocationProvider() {
        mapLocationProvider = DefaultLocationProviderFactory.create(context = applicationContext)
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
        navigationLocationProvider = DefaultLocationProviderFactory.create(context = applicationContext)
        val configuration = Configuration(
            context = applicationContext,
            navigationTileStore = navigationTileStore,
            locationProvider = navigationLocationProvider,
            routePlanner = routePlanner,
            vehicleProvider = VehicleProviderFactory.create(vehicle = Vehicle.Car())
        )
        tomTomNavigation = OnlineTomTomNavigationFactory.create(configuration)
    }

    fun initNavigationVisualization(tomTomMap: TomTomMap) {
        if (::navigationVisualization.isInitialized) {
            navigationVisualization.replaceMap(tomTomMap)
        } else {
            navigationVisualization = NavigationVisualizationFactory.create(
                tomtomMap = tomTomMap,
                tomtomNavigation = tomTomNavigation,
            )
            navigationVisualization.addRouteClickedListener(routeClickListener)
        }
    }

    override fun onLocationUpdate(location: GeoLocation) {
        _location.value = location
        mapLocationProvider.removeOnLocationUpdateListener(this)
    }

    fun registerLocationUpdatesListener() {
        mapLocationProvider.addOnLocationUpdateListener(this)
    }

    /**
     * Used to start navigation based on a tapped route, if navigation is not already running.
     * - Then start the navigation using the selected route.
     */
    private val routeClickListener = RouteClickedListener { routingRoute, _ ->
        if (!isNavigationRunning()) {
            startNavigation(routingRoute)
        }
    }

    private fun startNavigation(route: Route) {
        setSimulationLocationProviderToNavigation(route)
        setUpMapMatchedLocationProvider()
        _navigationStarted.value = NavigationStarted(
            routePlan = com.tomtom.sdk.navigation.RoutePlan(route, routePlanningOptions),
            tomTomNavigation = tomTomNavigation,
            locationProvider = mapMatchedLocationProvider
        )
    }

    /**
     * Used to calculate a route using the following parameters:
     * - InstructionPhoneticsType - This specifies whether to include phonetic transcriptions in the response.
     * - ExtendedSections - This specifies whether to include extended guidance sections in the response, such as sections of type road shield, lane, and speed limit.
     */
    fun calculateRoute(origin: GeoPoint, destination: GeoPoint) {
        val itinerary = Itinerary(origin = origin, destination = destination)
        _routePlanningOptions = RoutePlanningOptions(
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
            val routePlan = RoutePlan(result.routes)
            navigationVisualization.displayRoutePlan(routePlan)
            navigationVisualization.selectRoute(routePlan.routes.first().id)
        }

        override fun onFailure(failure: RoutingFailure) {
            _routingFailure.value = failure
        }

        override fun onRoutePlanned(route: Route) = Unit
    }

    /**
     * Use the SimulationLocationProvider for testing purposes.
     */
    private fun setSimulationLocationProviderToNavigation(route: Route) {
        val routeGeoLocations = route.geometry.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(routeGeoLocations)
        val oldNavigationLocationProvider = navigationLocationProvider
        navigationLocationProvider = SimulationLocationProvider.create(strategy = simulationStrategy)
        tomTomNavigation.locationProvider = navigationLocationProvider
        navigationLocationProvider.enable()
        oldNavigationLocationProvider.close()
    }

    /**
     * Once navigation is started, the camera is set to follow the user position, and the location indicator is changed to a chevron.
     * To match raw location updates to the routes, create a LocationProvider instance using MapMatchedLocationProviderFactory and set it to the TomTomMap.
     */
    private fun setUpMapMatchedLocationProvider() {
        _mapMatchedLocationProvider = MapMatchedLocationProviderFactory.create(tomTomNavigation)
        _mapMatchedLocationProvider?.enable()
        mapLocationProvider.disable()
    }


    fun navigationStopped() {
        mapLocationProvider.enable()
        navigationLocationProvider.disable()
        _mapMatchedLocationProvider?.close()
    }
    /**
     * Checks whether navigation is currently running.
     */
    fun isNavigationRunning(): Boolean = tomTomNavigation.navigationSnapshot != null

    override fun onCleared() {
        super.onCleared()
        _mapMatchedLocationProvider?.close()
        tomTomNavigation.close()
        navigationLocationProvider.close()
        navigationTileStore.close()
        mapLocationProvider.close()
        navigationVisualization.removeRouteClickedListener(routeClickListener)
        navigationVisualization.close()
    }
}
