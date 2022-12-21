package com.tomtom.demo.navdemoapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tomtom.demo.fragment.RouteProcessFragment
import com.tomtom.sdk.common.location.GeoPoint
import com.tomtom.sdk.common.vehicle.Vehicle
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.location.mapmatched.MapMatchedLocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.common.screen.Padding

import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.ui.MapFragment

import com.tomtom.sdk.navigation.NavigationConfiguration
import com.tomtom.sdk.navigation.NavigationError
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.routereplanner.RouteReplanner
import com.tomtom.sdk.navigation.routereplanner.default.DefaultRouteReplanner

import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.navigation.ui.NavigationUiOptions
import com.tomtom.sdk.route.Route
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResult

import com.tomtom.sdk.routing.common.RoutingError
import com.tomtom.sdk.routing.common.options.Itinerary
import com.tomtom.sdk.routing.common.options.RoutePlanningOptions
import com.tomtom.sdk.routing.common.options.guidance.*
import com.tomtom.sdk.routing.online.OnlineRoutePlanner

import com.tomtom.sdk.search.SearchApi
import com.tomtom.sdk.search.online.OnlineSearchApi

import com.tomtom.sdk.search.ui.SearchFragment
import com.tomtom.sdk.search.ui.SearchFragmentListener
import com.tomtom.sdk.search.ui.model.Place
import com.tomtom.sdk.search.ui.model.SearchApiParameters
import com.tomtom.sdk.search.ui.model.SearchProperties


class BasicNavActivity : AppCompatActivity() , RouteProcessFragment.NavigateOptionsInterface{
    // this is the frame layout container of the map and navigation fragments
    private lateinit var navGroupContainer: FrameLayout

    // Only marker in the map : the selected destination
    private var searchMarker: Marker? = null

    // The view that contain the "Navigate" button when we select a destination
    private lateinit var routingFragment: RouteProcessFragment

    // The NavSDK view
    private lateinit var navigationFragment: NavigationFragment

    // the navigation
    private lateinit var tomtomNavigation: TomTomNavigation

    // The route object created when we select a destination
    private lateinit var route: Route

    // Route options as type of vehicle, size, etc
    private lateinit var planRouteOptions: RoutePlanningOptions

    // Who is providing the GPS location?
    private lateinit var locationEngine: AndroidLocationProvider

    // The Map view object
    private lateinit var tomTomMap: TomTomMap

    // Search API
    private lateinit var searchApi: SearchApi

    // Routing API
    private lateinit var routingPlanner: RoutePlanner

    // Dynamic routing engine for creating routes.
    private lateinit var RoutingReplanner: RouteReplanner

    // Location update listener - for when we get GPS updates
    private lateinit var locationUpdateListener:OnLocationUpdateListener

    // API Key for map and apis
    private val apikey= BuildConfig.TomTomApiKey // https://developer.tomtom.com/user/register

    // Default map center
    private val amsterdamCenter = GeoPoint(52.377956, 4.897070)

    // SearchFragment Configuration
    private val searchApiParameters = SearchApiParameters(
        limit = 5,
        position = amsterdamCenter
    )

    private val searchProperties = SearchProperties(
        searchApiKey = apikey,
        searchApiParameters = searchApiParameters,
        commands = listOf("TomTom")
    )

    val searchFragment = SearchFragment.newInstance(searchProperties)

    private fun addRoutingOptionsFragment(place: Place) {
        routingFragment = RouteProcessFragment.newInstance(place, this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.route_fragment_container, routingFragment)
            .commitNow()
    }

    // Call this to add the search functionality to the activity
    private fun addSearchFragment() {
        // Add the search UI map fragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.search_fragment_container, searchFragment)
            .commitNow()

        // add the search API to the search fragment
        searchFragment.setSearchApi(searchApi)

        // Add the listener when someone click on a suggestion
        val searchFragmentListener = object : SearchFragmentListener {
            override fun onSearchBackButtonClick() {
                removeMarker()
                searchFragment.clear()
            }

            override fun onSearchResultClick(place: Place) {
                // make nav and map visible again
                navGroupContainer.visibility = View.VISIBLE
                // now we take the place, let's get the coordinates

                try {
                    tomTomMap.moveCamera(CameraOptions(position = place.position))
                } catch (exception: Exception) {
                    // do nothing because the map could be already
                    // invalidated.
                }
                
                setMarker(place.position)
                searchFragment.clear()
                removeSearchFragment()

                //Let's hide the keyboard if we have it
                if (currentFocus != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                }

                // add the Routing process fragment
                addRoutingOptionsFragment(place)
            }

            override fun onSearchError(throwable: Throwable) {
                /* YOUR CODE GOES HERE */
            }

            override fun onSearchQueryChanged(input: String) {
                // hide and show the map...
                if (input.isNotEmpty()) {
                    navGroupContainer.visibility = View.GONE
                } else {
                    navGroupContainer.visibility = View.VISIBLE
                }
            }

            override fun onCommandInsert(command: String) {
                /* YOUR CODE GOES HERE */
            }
        }
        searchFragment.setFragmentListener(searchFragmentListener)

    }

    private fun removeMarker() {
        searchMarker?.remove()
        searchMarker = null

    }
    // set a marker to the coordinates. It gets replaced everytime
    private fun setMarker(position: GeoPoint) {
        searchMarker?.remove()
        val markerOptions = MarkerOptions(
            coordinate = position,
            pinImage = ImageFactory.fromResource(R.drawable.ic_tomtom_pin)
        )
        searchMarker = this.tomTomMap.addMarker(markerOptions)
    }

    // Remove the search fragment
    private fun removeSearchFragment() {
        supportFragmentManager.beginTransaction()
            .remove(searchFragment)
            .commitNow()
    }

    // Remove the routing options
    private fun removeRoutingOptionsFragment() {
        supportFragmentManager.beginTransaction()
            .remove(routingFragment)
            .commitNow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchApi = OnlineSearchApi.create(this, apikey)

        // Add a map fragment
        val mapOptions = MapOptions(mapKey = apikey)
        val mapFragment = MapFragment.newInstance(mapOptions)
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        // Location Engine
        locationEngine = AndroidLocationProvider(context = this)

        // Lets check for FINE LOCATION permissions ...
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val COARSE_REQUEST_CODE = 101
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                COARSE_REQUEST_CODE)
            return
        }

        // Check for Media permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            val COARSE_REQUEST_CODE = 102
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                COARSE_REQUEST_CODE)
            return
        }

        locationEngine.enable()


        // Add routing API
        routingPlanner = OnlineRoutePlanner.create(context = this, apiKey = apikey)
        RoutingReplanner = DefaultRouteReplanner.create(routingPlanner)

        // Adding Navigation
        val navigationConfiguration = NavigationConfiguration(
            context = this,
            apiKey = apikey,
            locationProvider = locationEngine,
            routeReplanner = RoutingReplanner
        )
        tomtomNavigation = TomTomNavigation.create(navigationConfiguration)

        mapFragment.getMapAsync { map ->
            tomTomMap = map
            val initialOptions = CameraOptions(zoom = 16.0, position = amsterdamCenter )
            tomTomMap.moveCamera(initialOptions)
            enableUserLocation()
            setUpMapListeners()
            addSearchFragment()
        }

        val navigationUiOptions = NavigationUiOptions(
            keepInBackground = true
        )
        navigationFragment = NavigationFragment.newInstance(navigationUiOptions)
        supportFragmentManager.beginTransaction()
            .add(R.id.navigation_fragment_container, navigationFragment)
            .commitNow()
        navigationFragment.setTomTomNavigation(tomtomNavigation)
        navGroupContainer = findViewById(R.id.nav_group_container)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@BasicNavActivity,"Give me access to location!", Toast.LENGTH_LONG).show()
                }
            }
            102 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@BasicNavActivity,"Give me access to Storage!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setMapMatchedLocationEngine() {
        val mapMatchedLocationEngine = MapMatchedLocationProvider(tomtomNavigation)
        tomTomMap.setLocationProvider(mapMatchedLocationEngine)
        mapMatchedLocationEngine.enable()
    }

    private fun stopNavigation() {
        navigationFragment.stopNavigation()
        tomTomMap.changeCameraTrackingMode(CameraTrackingMode.NONE)
        tomTomMap.enableLocationMarker(LocationMarkerOptions(type = LocationMarkerOptions.Type.POINTER))
        tomTomMap.removeRoutes()
    }

    private fun setMapNavigationPadding() {
        val paddingBottom =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                resources.getDimension(R.dimen.map_padding_bottom),
                resources.displayMetrics
            ).toInt()
        val padding = Padding(0, 0, 0, paddingBottom)
        tomTomMap.setPadding(padding)
    }

    private val navigationListener = object : NavigationFragment.NavigationListener {
        override fun onStarted() {
            tomTomMap.changeCameraTrackingMode(CameraTrackingMode.FOLLOW_ROUTE)
            tomTomMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.CHEVRON))
            setMapMatchedLocationEngine()
            setMapNavigationPadding()
            removeMarker()
        }

        override fun onFailed(error: NavigationError) {
            Toast.makeText(this@BasicNavActivity, error.message, Toast.LENGTH_SHORT).show()
            stopNavigation()
            showBottomOptionsContainer()

        }

        override fun onStopped() {
            stopNavigation()
            showBottomOptionsContainer()
            addSearchFragment()
        }
    }

    private fun navigate() {
        if ( this::route.isInitialized ) { // start the navigation with a set route
            hideBottomOptionsContainer() // we want full screen for navigation
            removeRoutingOptionsFragment()
            try {
                val routePlan = RoutePlan(route, planRouteOptions)
                navigationFragment.startNavigation(routePlan)
                navigationFragment.addNavigationListener(navigationListener)
            } catch (exception: IllegalArgumentException) {
                Toast.makeText(this@BasicNavActivity, "Error. Maybe the navigation already started?", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideBottomOptionsContainer() {
        val container = findViewById<FrameLayout>(R.id.bottom_group_container)
        container.visibility = View.GONE
    }

    private fun showBottomOptionsContainer() {
        val container = findViewById<FrameLayout>(R.id.bottom_group_container)
        container.visibility = View.VISIBLE
    }

    private fun enableUserLocation() {

        // Getting locations to the map
        tomTomMap.setLocationProvider(locationEngine)
        val locationMarker = LocationMarkerOptions(type= LocationMarkerOptions.Type.POINTER)
        tomTomMap.enableLocationMarker(locationMarker)
    }

    private val routePlanningCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResult) {
            route = result.routes.first()
            drawRoute(route)
        }

        override fun onError(error: RoutingError) {
            Toast.makeText(this@BasicNavActivity, error.message, Toast.LENGTH_SHORT).show()
        }

        override fun onRoutePlanned(route: Route) {
            this@BasicNavActivity.route = route
            drawRoute(route)
        }
    }

    private fun Route.mapInstructions(): List<Instruction> {
        val routeInstructions = legs.flatMap { routeLeg -> routeLeg.instructions }
        return routeInstructions.map {
            Instruction(
                routeOffset = it.routeOffset,
                combineWithNext = it.combineWithNext
            )
        }
    }

    private fun drawRoute(route: Route) {
        val instructions = route.mapInstructions()
        val geometry = route.legs.flatMap { it.points }
        val routeOptions = RouteOptions(
            geometry = geometry,
            destinationMarkerVisible = true,
            departureMarkerVisible = true,
            instructions = instructions
        )
        tomTomMap.addRoute(routeOptions)
        val zoomPadding = 20
        tomTomMap.zoomToRoutes(zoomPadding)

    }

    private fun createRoute(destination: GeoPoint) {
        val userLocation = tomTomMap.currentLocation?.position ?: return
        val itinerary = Itinerary(origin = userLocation, destination = destination)
        planRouteOptions = RoutePlanningOptions(
            itinerary = itinerary,
            guidanceOptions = GuidanceOptions(
                instructionType = InstructionType.TEXT,
                phoneticsType = InstructionPhoneticsType.IPA,
                announcementPoints = AnnouncementPoints.All,
                extendedSections = ExtendedSections.All,
                progressPoints = ProgressPoints.All
            ),
            vehicle = Vehicle.Car()
        )
        routingPlanner.planRoute( planRouteOptions, routePlanningCallback)
    }

    private fun setUpMapListeners() {

        tomTomMap.addOnMapClickListener {
            navigate()
            return@addOnMapClickListener true
        }


        // We are going to listen to the current location to move the map
        // initially, but when we are navigating this is done automatically,
        // so this listener should be deactivated.
        locationUpdateListener = OnLocationUpdateListener { location ->
            try {
                tomTomMap.moveCamera(CameraOptions(position = location.position))
            } catch (exception: Exception) {
                // do nothing because the map could be already
                // invalidated.
            }
            locationEngine.removeOnLocationUpdateListener(locationUpdateListener)
        }
        locationEngine.addOnLocationUpdateListener(locationUpdateListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::locationUpdateListener.isInitialized) {
            locationEngine.removeOnLocationUpdateListener(locationUpdateListener)
        }
    }

    override fun onNavigate(destination: GeoPoint) {
        navigate()
    }

    override fun onCancel() {
        removeRoutingOptionsFragment()
        addSearchFragment()
        removeMarker()
        tomTomMap.removeRoutes()
    }

    override fun removeRoute() {
        tomTomMap.removeRoutes()
    }

    override fun onRoute(destination: GeoPoint) {
        createRoute(destination)
    }
}