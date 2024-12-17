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

package com.example.usecase


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.camera.CameraChangeListener
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.common.screen.Padding
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteClickListener
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton.VisibilityPolicy
import com.tomtom.sdk.navigation.ActiveRouteChangedListener
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.online.Configuration
import com.tomtom.sdk.navigation.online.OnlineTomTomNavigationFactory
import com.tomtom.sdk.navigation.routereplanner.RouteReplanner
import com.tomtom.sdk.navigation.routereplanner.online.OnlineRouteReplannerFactory
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.navigation.ui.NavigationUiOptions
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.ExtendedSections
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.options.guidance.InstructionPhoneticsType
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle
import com.tomtom.sdk.vehicle.VehicleProviderFactory

/**
 * This example shows how to build a simple navigation application using the TomTom Navigation SDK for Android.
 * The application displays a map and shows the user’s location. After the user selects a destination with a long click, the app plans a route and draws it on the map.
 * Navigation is started in a simulation mode, once the user taps on the route.
 * The application will display upcoming manoeuvres, remaining distance, estimated time of arrival (ETA), current speed, and speed limit information.
 *
 * For more details on this example, check out the tutorial: https://developer.tomtom.com/navigation/android/build-a-navigation-app/building-a-navigation-app
 **/

class MainActivity : AppCompatActivity() {
    private var _navigationFragment: NavigationFragment? = null
    private val navigationFragment: NavigationFragment
        get() =
            _navigationFragment
                ?: throw IllegalStateException("Navigation fragment was not initialized")
    private lateinit var mapFragment: MapFragment
    private lateinit var tomTomMap: TomTomMap

    private val apiKey = TOMTOM_API_KEY

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ensureLocationPermissions {
            // Enables the location provider to start receiving location updates.
            viewModel.locationProvider.enable()
        }

        initMap {
            /**
             * The LocationProvider itself only reports location changes.
             * It does not interact internally with the map or navigation.
             * Therefore, to show the user’s location on the map you have to set the LocationProvider to the TomTomMap.
             * The TomTomMap will then use the LocationProvider to show a location marker on the map.
             */
            tomTomMap.setLocationProvider(viewModel.locationProvider)

//            showUserLocation()
            setUpMapListeners()
            viewModel.activeRoute.observe(this) {
                tomTomMap.removeRoutes()
                drawRoute(it)
            }
            createNavigationFragment()
            if(viewModel.isNavigationRunning()) {
                resumeNavigation()
            } else {
                showUserLocation()
            }
        }

    }

//    override fun onStart() {
//        super.onStart()
//        _navigationFragment?.addNavigationListener(navigationListener)
//    }
//
//    override fun onStop() {
//        super.onStop()
//        _navigationFragment?.removeNavigationListener(navigationListener)
//    }

    /**
     * [MapOptions] is required to initialize the map with [MapFragment.newInstance]
     * Use [MapFragment.getMapAsync] to render the map.
     *
     * Optional: You can further configure the map by setting various properties of the MapOptions object.
     * You can learn more in the Map Configuration guide.
     * The next step is adding the MapFragment to the previously created container.
     * The map is ready to use once the [MapFragment.getMapAsync] method is called and the map is fetched,
     * after which the [onMapReady] callback is triggered.
     */
    private fun initMap(onMapReady: () -> Unit) {
        mapFragment = supportFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG)?.let {
            it as MapFragment
        } ?: run {
            val mapOptions = MapOptions(mapKey = apiKey)
            MapFragment.newInstance(mapOptions).also {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.map_container, it, MAP_FRAGMENT_TAG)
                    .commit()
            }
        }
        mapFragment.getMapAsync { map ->
            tomTomMap = map
            onMapReady()
        }
    }

    /**
     * The application must use the device’s location services, which requires the appropriate permissions.
     */
    private fun ensureLocationPermissions(onLocationPermissionsGranted: () -> Unit) {
        if (areLocationPermissionsGranted()) {
            onLocationPermissionsGranted()
        } else {
            requestLocationPermission(onLocationPermissionsGranted)
        }
    }

    /**
     * Manually enables the location marker.
     * It can be configured using the LocationMarkerOptions class.
     *
     * Read more about user location on the map in the Showing User Location guide.
     */
    private fun showUserLocation() {
        // zoom to current location at city level
        viewModel.location.observe(this) {
            val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
            tomTomMap.enableLocationMarker(locationMarker)
            tomTomMap.moveCamera(CameraOptions(it.position, zoom = 8.0))
        }
        viewModel.registerLocationUpdatesListener()
    }

    /**
     * In this example on planning a route, the origin is the user’s location and the destination is determined by the user selecting a location on the map.
     * Navigation is started once the user taps on the route.
     *
     * To mark the destination on the map, add the MapLongClickListener event handler to the map view.
     * To start navigation, add the addRouteClickListener event handler to the map view.
     */
    private fun setUpMapListeners() {
        tomTomMap.addMapLongClickListener(mapLongClickListener)
        tomTomMap.addRouteClickListener(routeClickListener)
    }

    /**
     * Used to calculate a route based on a selected location.
     * - The method removes all polygons, circles, routes, and markers that were previously added to the map.
     * - It then creates a route between the user’s location and the selected location.
     * - The method needs to return a boolean value when the callback is consumed.
     */
    private val mapLongClickListener = MapLongClickListener { geoPoint ->
        clearMap()
        calculateRouteTo(geoPoint)
        true
    }

    /**
     * Used to start navigation based on a tapped route, if navigation is not already running.
     * - Hide the location button
     * - Then start the navigation using the selected route.
     */
    private val routeClickListener = RouteClickListener {
        if (!viewModel.isNavigationRunning()) {
            viewModel.routePlan.value?.let { routePlan ->
                mapFragment.currentLocationButton.visibilityPolicy = VisibilityPolicy.Invisible
                startNavigation(routePlan)
            }
        }
    }

    private fun calculateRouteTo(destination: GeoPoint) {
        val userLocation =
            tomTomMap.currentLocation?.position ?: return
        viewModel.routePlan.observe(this) {
            drawRoute(it.route)
            tomTomMap.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }
        viewModel.routingFailure.observe(this) {
            Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
        }
        viewModel.calculateRoute(userLocation, destination)
    }

    /**
     * Used to draw route on the map
     */
    private fun drawRoute(route: Route) {
        val instructions = route.mapInstructions()
        val routeOptions = RouteOptions(
            geometry = route.geometry,
            destinationMarkerVisible = true,
            departureMarkerVisible = true,
            instructions = instructions,
            routeOffset = route.routePoints.map { it.routeOffset }
        )
        tomTomMap.addRoute(routeOptions)
    }

    /**
     * For the navigation use case, the instructions can be drawn on the route in form of arrows that indicate maneuvers.
     * To do this, map the Instruction object provided by routing to the Instruction object used by the map.
     * Note that during navigation, you need to update the progress property of the drawn route to display the next instructions.
     */
    private fun Route.mapInstructions(): List<Instruction> {
        val routeInstructions = legs.flatMap { routeLeg -> routeLeg.instructions }
        return routeInstructions.map {
            Instruction(
                routeOffset = it.routeOffset
            )
        }
    }

    /**
     * Used to start navigation by
     * - initializing the NavigationFragment to display the UI navigation information,
     * - passing the Route object along which the navigation will be done, and RoutePlanningOptions used during the route planning,
     * - handling the updates to the navigation states using the NavigationListener.
     * Note that you have to set the previously-created TomTom Navigation object to the NavigationFragment before using it.
     */
    private fun startNavigation(routePlan: RoutePlan) {
        displayNavigationFragment()
        navigationFragment.setTomTomNavigation(viewModel.tomTomNavigation)
        navigationFragment.startNavigation(routePlan)
        navigationFragment.addNavigationListener(navigationListener)
        viewModel.distanceAlongRoute.observe(this) {
            tomTomMap.routes.first().progress = it
        }

        viewModel.navigationStarted()
    }

    private fun resumeNavigation() {
        displayNavigationFragment()
        navigationFragment.setTomTomNavigation(viewModel.tomTomNavigation)
        navigationFragment.addNavigationListener(navigationListener)
        viewModel.distanceAlongRoute.observe(this) {
            tomTomMap.routes.first().progress = it
        }
        viewModel.activeRoute.observe(this) {
            tomTomMap.removeRoutes()
            drawRoute(it)
        }
        setUpMapForNavigation()
    }

    private fun setUpMapForNavigation() {
        tomTomMap.addCameraChangeListener(cameraChangeListener)
        tomTomMap.cameraTrackingMode = CameraTrackingMode.FollowRouteDirection
        tomTomMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Chevron))
        setMapMatchedLocationProvider()
        setMapNavigationPadding()
    }

    /**
     * Handle the updates to the navigation states using the NavigationListener
     * - Use CameraChangeListener to observe camera tracking mode and detect if the camera is locked on the chevron. If the user starts to move the camera, it will change and you can adjust the UI to suit.
     * - Use the SimulationLocationProvider for testing purposes.
     * - Once navigation is started, the camera is set to follow the user position, and the location indicator is changed to a chevron. To match raw location updates to the routes, create LocationProvider using MapMatchedLocationProviderFactory and set it to the TomTomMap.
     * - Set the bottom padding on the map. The padding sets a safe area of the MapView in which user interaction is not received. It is used to uncover the chevron in the navigation panel.
     */
    private val navigationListener = object : NavigationFragment.NavigationListener {
        override fun onStarted() {
            setUpMapForNavigation()
            viewModel.setSimulationLocationProviderToNavigation()
        }

        override fun onStopped() {
            stopNavigation()
        }
    }

    /**
     * Used to initialize the NavigationFragment to display the UI navigation information,
     */
    private fun displayNavigationFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navigation_fragment_container, navigationFragment, NAVIGATION_FRAGMENT_TAG)
            .commitNow()
    }

    private fun createNavigationFragment() {
        _navigationFragment =
            supportFragmentManager.findFragmentByTag(NAVIGATION_FRAGMENT_TAG) as? NavigationFragment
        if (_navigationFragment == null) {
            _navigationFragment = NavigationFragment.newInstance(
                NavigationUiOptions(
                    keepInBackground = true
                )
            )
        }
    }

    /**
     * Stop the navigation process using NavigationFragment.
     * This hides the UI elements and calls the TomTomNavigation.stop() method.
     * Don’t forget to reset any map settings that were changed, such as camera tracking, location marker, and map padding.
     */
    private fun stopNavigation() {
        navigationFragment.stopNavigation()
        mapFragment.currentLocationButton.visibilityPolicy =
            VisibilityPolicy.InvisibleWhenRecentered
        tomTomMap.removeCameraChangeListener(cameraChangeListener)
        tomTomMap.cameraTrackingMode = CameraTrackingMode.None
        tomTomMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Pointer))
        resetMapPadding()
        navigationFragment.removeNavigationListener(navigationListener)
        clearMap()
        viewModel.navigationStopped()
        tomTomMap.setLocationProvider(viewModel.locationProvider)
        showUserLocation()
    }

    /**
     * Set the bottom padding on the map. The padding sets a safe area of the MapView in which user interaction is not received. It is used to uncover the chevron in the navigation panel.
     */
    private fun setMapNavigationPadding() {
        val paddingBottom = resources.getDimensionPixelOffset(R.dimen.map_padding_bottom)
        val padding = Padding(0, 0, 0, paddingBottom)
        tomTomMap.setPadding(padding)
    }

    private fun resetMapPadding() {
        tomTomMap.setPadding(Padding(0, 0, 0, 0))
    }

    /**
     * Once navigation is started, the camera is set to follow the user position, and the location indicator is changed to a chevron.
     * To match raw location updates to the routes, create a LocationProvider instance using MapMatchedLocationProviderFactory and set it to the TomTomMap.
     */
    private fun setMapMatchedLocationProvider() {
        val mapMatchedLocationProvider = MapMatchedLocationProviderFactory.create(viewModel.tomTomNavigation)
        tomTomMap.setLocationProvider(mapMatchedLocationProvider)
        mapMatchedLocationProvider.enable()
    }

    /**
     *
     * The method removes all polygons, circles, routes, and markers that were previously added to the map.
     */
    private fun clearMap() {
        tomTomMap.clear()
    }

    private val cameraChangeListener = CameraChangeListener {
        val cameraTrackingMode = tomTomMap.cameraTrackingMode
        if (cameraTrackingMode == CameraTrackingMode.FollowRouteDirection) {
            navigationFragment.navigationView.showSpeedView()
        } else {
            navigationFragment.navigationView.hideSpeedView()
        }
    }

    /**
     * Method to verify permissions:
     * - [Manifest.permission.ACCESS_FINE_LOCATION]
     * - [Manifest.permission.ACCESS_COARSE_LOCATION]
     */
    private fun areLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED


    private fun requestLocationPermission(onLocationPermissionsGranted: () -> Unit) =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                onLocationPermissionsGranted()
            } else {
                Toast.makeText(
                    this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT
                ).show()
            }
        }.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

    override fun onDestroy() {
        tomTomMap.setLocationProvider(null)
        tomTomMap.removeRouteClickListener(routeClickListener)
        tomTomMap.removeMapLongClickListener(mapLongClickListener)
        // TODO is it needed???
//        if (_navigationFragment != null) {
//            supportFragmentManager.beginTransaction().remove(navigationFragment).commitNowAllowingStateLoss()
//        }
        super.onDestroy()
    }

    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
        private const val NAVIGATION_FRAGMENT_TAG = "NAVIGATION_FRAGMENT_TAG"
        private const val MAP_FRAGMENT_TAG = "MAP_FRAGMENT_TAG"
    }
}
