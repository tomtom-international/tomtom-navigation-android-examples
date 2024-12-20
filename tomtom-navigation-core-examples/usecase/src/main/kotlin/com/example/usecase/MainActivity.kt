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
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.common.screen.Padding
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.route.RouteClickListener
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton.VisibilityPolicy
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.navigation.ui.NavigationUiOptions
import com.tomtom.sdk.routing.route.Route

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
            // Enables the location provider to start receiving location updates only if we are in browsing mode.
            if (savedInstanceState == null) {
                viewModel.mapLocationProvider.enable()
            }
        }
        createNavigationFragment()

        initMap {
            viewModel.initNavigationVisualization(tomTomMap)
            if(!viewModel.isNavigationRunning()) {
                /**
                 * The LocationProvider itself only reports location changes.
                 * It does not interact internally with the map or navigation.
                 * Therefore, to show the user’s location on the map you have to set the LocationProvider to the TomTomMap.
                 * The TomTomMap will then use the LocationProvider to show a location marker on the map.
                 */
                tomTomMap.setLocationProvider(viewModel.mapLocationProvider)
                showUserLocation()
            } else {
                setMapNavigationPadding()
                /**
                 * When already navigating the MapMatchedLocationProvider is set to the TomTomMap.
                 * It may happen when there is screen rotation.
                 */
                tomTomMap.setLocationProvider(viewModel.mapMatchedLocationProvider)
            }
            setUpMapListeners()
            setUpViewModelObservers()
        }

    }

    private fun setUpViewModelObservers() {
        viewModel.routingFailure.observe(this) {
            Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        _navigationFragment?.addNavigationListener(navigationListener)
        if (::tomTomMap.isInitialized && tomTomMap.getLocationProvider() == null) {
            if (viewModel.isNavigationRunning()) {
                tomTomMap.setLocationProvider(viewModel.mapMatchedLocationProvider)
            } else {
                tomTomMap.setLocationProvider(viewModel.mapLocationProvider)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        _navigationFragment?.removeNavigationListener(navigationListener)
        // onCleared in ViewModel is called before onDestroy in Activity so the clean up
        // has to be done in onStop - https://issuetracker.google.com/issues/363903522
        if(::tomTomMap.isInitialized) {
            tomTomMap.setLocationProvider(null)
        }
    }

    override fun onDestroy() {
        tomTomMap.removeRouteClickListener(routeClickListener)
        tomTomMap.removeMapLongClickListener(mapLongClickListener)
        super.onDestroy()
    }

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
            viewModel.selectedRoute?.let { route ->
                mapFragment.currentLocationButton.visibilityPolicy = VisibilityPolicy.Invisible
                startNavigation(route)
            }
        }
    }

    private fun calculateRouteTo(destination: GeoPoint) {
        val userLocation =
            tomTomMap.currentLocation?.position ?: return
        viewModel.calculateRoute(userLocation, destination)
    }

    /**
     * Used to start navigation by
     * - display the UI navigation information,
     * - passing the Route object along which the navigation will be done, and RoutePlanningOptions used during the route planning,
     * - handling the updates to the navigation states using the NavigationListener.
     * Note that you have to set the previously-created TomTom Navigation object to the NavigationFragment before using it.
     */
    private fun startNavigation(route: Route) {
        displayNavigationFragment()
        navigationFragment.setTomTomNavigation(viewModel.tomTomNavigation)
        viewModel.navigationStart(route)
        tomTomMap.setLocationProvider(viewModel.mapMatchedLocationProvider)
        navigationFragment.startNavigation(RoutePlan(route, viewModel.routePlanningOptions))
    }

    /**
     * Used to display the UI navigation information,
     */
    private fun displayNavigationFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navigation_fragment_container, navigationFragment, NAVIGATION_FRAGMENT_TAG)
            .commitNow()
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
            tomTomMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Chevron))
            setMapNavigationPadding()
        }

        override fun onStopped() {
            stopNavigation()
        }
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
//        tomTomMap.removeCameraChangeListener(cameraChangeListener)
        tomTomMap.cameraTrackingMode = CameraTrackingMode.None
        resetMapPadding()
//        clearMap()
        tomTomMap.setLocationProvider(viewModel.mapLocationProvider)
        viewModel.navigationStopped()
        showUserLocation()
    }

    /**
     * Set the bottom padding on the map. The padding sets a safe area of the MapView in which user interaction is not received. It is used to uncover the chevron in the navigation panel.
     */
    private fun setMapNavigationPadding() {
        val paddingBottom = resources.getDimensionPixelOffset(R.dimen.map_padding_bottom)
        val paddingLeft = resources.getDimensionPixelOffset(R.dimen.map_padding_left)
        val paddingRight = resources.getDimensionPixelOffset(R.dimen.map_padding_right)
        val paddingTop = resources.getDimensionPixelOffset(R.dimen.map_padding_top)
        val padding = Padding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        tomTomMap.setPadding(padding)
    }

    private fun resetMapPadding() {
        tomTomMap.setPadding(Padding(0, 0, 0, 0))
    }

    /**
     *
     * The method removes all polygons, circles, routes, and markers that were previously added to the map.
     */
    private fun clearMap() {
        tomTomMap.clear()
    }

//    private val cameraChangeListener = CameraChangeListener {
//        val cameraTrackingMode = tomTomMap.cameraTrackingMode
//        if (cameraTrackingMode == CameraTrackingMode.FollowRouteDirection) {
//            navigationFragment.navigationView.showSpeedView()
//        } else {
//            navigationFragment.navigationView.hideSpeedView()
//        }
//    }

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

    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
        private const val NAVIGATION_FRAGMENT_TAG = "NAVIGATION_FRAGMENT_TAG"
        private const val MAP_FRAGMENT_TAG = "MAP_FRAGMENT_TAG"
    }
}
