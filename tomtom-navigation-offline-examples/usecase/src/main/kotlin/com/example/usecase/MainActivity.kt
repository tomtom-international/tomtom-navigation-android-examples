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
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.usecase.BuildConfig.NDS_MAP_LICENSE
import com.example.usecase.BuildConfig.TOMTOM_API_KEY
import com.example.usecase.assets.OnboardMapAssetsExtractor
import com.tomtom.quantity.Distance
import com.tomtom.quantity.Speed
import com.tomtom.sdk.annotations.InternalTomTomSdkApi
import com.tomtom.sdk.common.fold
import com.tomtom.sdk.datamanagement.nds.NdsStore
import com.tomtom.sdk.datamanagement.nds.NdsStoreAccessPermit
import com.tomtom.sdk.datamanagement.nds.NdsStoreConfiguration
import com.tomtom.sdk.datamanagement.nds.update.NdsStoreUpdaterConfiguration
import com.tomtom.sdk.datamanagement.nds.update.NdsStoreUpdater
import com.tomtom.sdk.datamanagement.nds.update.automatic.AutomaticNdsStoreUpdaterConfiguration
import com.tomtom.sdk.datamanagement.nds.update.automatic.AutomaticNdsStoreUpdaterConfiguration.RelevantRegions
import com.tomtom.sdk.featuretoggle.FeatureToggleController
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.location.mapmatched.MapMatchedLocationProvider
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.TomTomMapConfig
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.common.screen.Padding
import com.tomtom.sdk.map.display.dataprovider.offline.TileOfflineDataProviderFactory
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteClickListener
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.styleprovider.offline.StyleUriProvider
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.navigation.ActiveRouteChangedListener
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.guidance.AnnouncementMode
import com.tomtom.sdk.navigation.guidance.GuidanceEngineFactory
import com.tomtom.sdk.navigation.guidance.GuidanceEngineOptions
import com.tomtom.sdk.navigation.offline.Configuration
import com.tomtom.sdk.navigation.offline.OfflineTomTomNavigationFactory
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.navigation.ui.NavigationUiOptions
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.offline.OfflineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.ExtendedSections
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.options.guidance.InstructionPhoneticsType
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle
import kotlin.time.Duration.Companion.minutes

class MainActivity : AppCompatActivity() {
    private lateinit var tomTomMap: TomTomMap
    private lateinit var locationProvider: LocationProvider
    private lateinit var routePlanner: RoutePlanner
    private var route: Route? = null
    private lateinit var routePlanningOptions: RoutePlanningOptions
    private lateinit var tomTomNavigation: TomTomNavigation
    private lateinit var navigationFragment: NavigationFragment
    private lateinit var ndsStore: NdsStore
    private lateinit var ndsStoreUpdater: NdsStoreUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FeatureToggleController.initialize(this)

        setContentView(R.layout.activity_main)
        initNdsStore()
        initMap()
        initLocationProvider()
        initRouting()
        initNavigation()
    }

    private fun initNdsStore() {
        val externalDir = requireNotNull(getExternalFilesDir(null))
        val path = externalDir.resolve(NDS_DATA_DIR)
        val ndsStorePath = path.resolve(RELATIVE_NDS_STORE_PATH)
        val keystorePath = path.resolve(RELATIVE_KEYSTORE_PATH)

        OnboardMapAssetsExtractor.extractMapAssets(
            this,
            ndsStorePath,
            keystorePath,
            false
        )

        ndsStore = NdsStore.create(
            context = this, NdsStoreConfiguration(
                ndsStorePath,
                keystorePath,
                accessPermit = NdsStoreAccessPermit.MapLicense(NDS_MAP_LICENSE)
            )
        ).fold({ it }, {
            Toast.makeText(
                this, it.message, Toast.LENGTH_LONG
            ).show()
            throw IllegalStateException(it.message)
        })

        var relevantRegions : RelevantRegions? = null
        if (AUTOMATIC_UPDATES_RELEVANT_REGIONS_UPDATE) {
            relevantRegions = RelevantRegions(
                radius = AUTOMATIC_UPDATES_RELEVANT_REGIONS_RADIUS,
                updateInterval = AUTOMATIC_UPDATES_RELEVANT_REGIONS_UPDATE_INTERVAL
            )
        }
        ndsStoreUpdater = NdsStoreUpdater.create(
            context = this, ndsStore = ndsStore, configuration = NdsStoreUpdaterConfiguration(
                updateStoragePath = path.resolve(RELATIVE_UPDATE_STORAGE_PATH),
                persistentStoragePath = path.resolve(RELATIVE_MAP_UPDATE_PERSISTENCE_PATH),
                automaticUpdates = AutomaticNdsStoreUpdaterConfiguration(
                    relevantRegions = relevantRegions
                ),
                updateServerUri = Uri.parse(UPDATE_SERVER_URL),
                updateServerApiKey = TOMTOM_API_KEY
            )
        ).fold({ it }, {
            Toast.makeText(
                this, it.toString(), Toast.LENGTH_LONG
            ).show()
            throw IllegalStateException(it.toString())
        })

        ndsStoreUpdater.setUpdatesEnabled(true)
    }

    private fun initMap() {
        TomTomMapConfig.customDataProvidersFactoryFunction = {
            listOf(TileOfflineDataProviderFactory.createOfflineDataProvider(ndsStore))
        }
        val mapOptions = MapOptions(
            mapStyle = StyleDescriptor(
                StyleUriProvider.ONBOARD_BROWSING_LIGHT,
                StyleUriProvider.ONBOARD_BROWSING_DARK,
                StyleUriProvider.ONBOARD_LAYER_MAPPING,
                StyleUriProvider.ONBOARD_LAYER_MAPPING
            ), mapKey = TOMTOM_API_KEY
        )
        val mapFragment = MapFragment.newInstance(mapOptions)
        supportFragmentManager.beginTransaction().replace(R.id.map_container, mapFragment).commit()
        mapFragment.getMapAsync { map ->
            tomTomMap = map
            enableUserLocation()
            setUpMapListeners()
        }
    }

    private fun initLocationProvider() {
        locationProvider = AndroidLocationProvider(context = this)

        val updateListener = object : OnLocationUpdateListener {
            override fun onLocationUpdate(location: GeoLocation) {
                setNdsStorePosition(location.position)

                tomTomMap.moveCamera(
                    CameraOptions(position = location.position, zoom = 12.0)
                )
                locationProvider.removeOnLocationUpdateListener(this)
            }
        }

        locationProvider.addOnLocationUpdateListener(updateListener)
    }

    @OptIn(InternalTomTomSdkApi::class)
    private fun initRouting() {
        routePlanner = OfflineRoutePlanner.create(ndsStore = ndsStore)
    }

    private fun initNavigation() {
        val guidanceEngine = GuidanceEngineFactory.createDynamicGuidanceEngine(
            this, GuidanceEngineOptions(
                Distance.ZERO, AnnouncementMode.Comprehensive
            )
        )

        tomTomNavigation = OfflineTomTomNavigationFactory.create(
            Configuration(
                context = this,
                ndsStore = ndsStore,
                ndsMapUpdater = ndsStoreUpdater,
                locationProvider = locationProvider,
                routePlanner = routePlanner,
                guidanceEngine = guidanceEngine
            )
        )
    }

    private fun setUpMapListeners() {
        tomTomMap.addMapLongClickListener(mapLongClickListener)
        tomTomMap.addRouteClickListener(routeClickListener)
    }

    private val mapLongClickListener = MapLongClickListener {
        clearMap()
        calculateRouteTo(it)
        true
    }

    private val routeClickListener = RouteClickListener {
        route?.let { route -> startNavigation(route) }
    }

    private fun enableUserLocation() {
        if (areLocationPermissionsGranted()) {
            showUserLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun showUserLocation() {
        locationProvider.enable()
        tomTomMap.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap.enableLocationMarker(locationMarker)
    }

    private fun calculateRouteTo(destination: GeoPoint) {
        val userLocation = tomTomMap.currentLocation?.position ?: return
        val itinerary = Itinerary(origin = userLocation, destination = destination)
        routePlanningOptions = RoutePlanningOptions(
            itinerary = itinerary, guidanceOptions = GuidanceOptions(
                phoneticsType = InstructionPhoneticsType.Ipa,
                extendedSections = ExtendedSections.All
            ), vehicle = Vehicle.Car()
        )
        routePlanner.planRoute(routePlanningOptions, routingCallback)
    }

    private val routingCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResponse) {
            route = result.routes.first()
            drawRoute(route!!)
            tomTomMap.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }

        override fun onFailure(failure: RoutingFailure) {
            Toast.makeText(this@MainActivity, failure.message, Toast.LENGTH_SHORT).show()
        }

        override fun onRoutePlanned(route: Route) = Unit
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
    }

    private fun Route.mapInstructions(): List<Instruction> {
        val routeInstructions = legs.flatMap { routeLeg -> routeLeg.instructions }
        return routeInstructions.map {
            Instruction(
                routeOffset = it.routeOffset
            )
        }
    }

    private fun startNavigation(route: Route) {
        initNavigationFragment()
        navigationFragment.setTomTomNavigation(tomTomNavigation)

        val routePlan = RoutePlan(route, routePlanningOptions)
        navigationFragment.startNavigation(routePlan)
        navigationFragment.addNavigationListener(navigationListener)
        tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.addActiveRouteChangedListener(activeRouteChangedListener)
    }

    private fun initNavigationFragment() {
        val navigationUiOptions = NavigationUiOptions(
            keepInBackground = true
        )
        navigationFragment = NavigationFragment.newInstance(navigationUiOptions)
        supportFragmentManager.beginTransaction()
            .add(R.id.navigation_fragment_container, navigationFragment).commitNow()
    }

    private val navigationListener = object : NavigationFragment.NavigationListener {
        override fun onStarted() {
            tomTomMap.cameraTrackingMode = CameraTrackingMode.FollowRouteDirection
            tomTomMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Chevron))
            setMapMatchedLocationProvider()
            setSimulationLocationProviderToNavigation()
            setMapNavigationPadding()
        }

        override fun onStopped() {
            stopNavigation()
        }
    }

    private fun setSimulationLocationProviderToNavigation() {
        locationProvider = createSimulationLocationProvider(route!!)
        tomTomNavigation.locationProvider = locationProvider
        locationProvider.enable()
    }

    private val progressUpdatedListener = ProgressUpdatedListener {
        tomTomMap.routes.first().progress = it.distanceAlongRoute
    }

    private val activeRouteChangedListener by lazy {
        ActiveRouteChangedListener { route ->
            tomTomMap.removeRoutes()
            drawRoute(route)
        }
    }

    private fun stopNavigation() {
        navigationFragment.stopNavigation()
        tomTomMap.cameraTrackingMode = CameraTrackingMode.None
        tomTomMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Pointer))
        resetMapPadding()
        navigationFragment.removeNavigationListener(navigationListener)
        tomTomNavigation.removeProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.removeActiveRouteChangedListener(activeRouteChangedListener)
        clearMap()
        initLocationProvider()
        enableUserLocation()
    }

    private fun clearMap() {
        tomTomMap.clear()
    }

    private fun setMapNavigationPadding() {
        val paddingBottom = resources.getDimensionPixelOffset(R.dimen.map_padding_bottom)
        val padding = Padding(0, 0, 0, paddingBottom)
        tomTomMap.setPadding(padding)
    }

    private fun resetMapPadding() {
        tomTomMap.setPadding(Padding(0, 0, 0, 0))
    }

    private fun setMapMatchedLocationProvider() {
        val mapMatchedLocationProvider = MapMatchedLocationProvider(tomTomNavigation)
        tomTomMap.setLocationProvider(mapMatchedLocationProvider)
        mapMatchedLocationProvider.enable()
    }

    private fun createSimulationLocationProvider(route: Route): LocationProvider {
        val routeGeoPoints = route.legs.flatMap { it.points }
        val routeGeoLocations = routeGeoPoints.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(
            routeGeoLocations,
            currentSpeed = Speed.Companion.metersPerSecond(SPEED_METERS_PER_SECOND)
        )
        return SimulationLocationProvider.create(strategy = simulationStrategy)
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            showUserLocation()
        } else {
            Toast.makeText(
                this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun areLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun setNdsStorePosition(currentPosition: GeoPoint) {
        ndsStoreUpdater.updatePosition(currentPosition)
    }

    override fun onDestroy() {
        tomTomMap.setLocationProvider(null)
        supportFragmentManager.beginTransaction().remove(navigationFragment).commitNowAllowingStateLoss()
        super.onDestroy()
        tomTomNavigation.close()
        locationProvider.close()
        ndsStoreUpdater.close()
        ndsStore.close()
    }

    companion object {
        private const val NDS_DATA_DIR = "onboard"
        private const val RELATIVE_NDS_STORE_PATH = "map"
        private const val RELATIVE_UPDATE_STORAGE_PATH = "updates"
        private const val RELATIVE_KEYSTORE_PATH = "keystore.sqlite"
        private const val RELATIVE_MAP_UPDATE_PERSISTENCE_PATH = "mapUpdatePersistence"
        private const val AUTOMATIC_UPDATES_RELEVANT_REGIONS_UPDATE = true
        private val AUTOMATIC_UPDATES_RELEVANT_REGIONS_RADIUS = Distance.kilometers(20.0)
        private val AUTOMATIC_UPDATES_RELEVANT_REGIONS_UPDATE_INTERVAL = 60.minutes
        private const val UPDATE_SERVER_URL = "https://api.tomtom.com/nds-test/updates/1/fetch"
        private const val SPEED_METERS_PER_SECOND = 30.0
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }
}
