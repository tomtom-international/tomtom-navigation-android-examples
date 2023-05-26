package com.tomtom.sdk.examples.maps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tomtom.sdk.examples.BuildConfig
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.MapView

/**
 * This activity is responsible for displaying the TomTom Vector Map, both with location and without.
 *
 */

class ConfigurableMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mapFragment: MapFragment
    private lateinit var tomTomMap: TomTomMap
    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private lateinit var goBackIV: ImageView

    /**
     * Navigation SDK is only available upon request.
     * Use the API key provided by TomTom to start using the SDK.
     */
    private val apiKey = BuildConfig.TOMTOM_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMapViewPage()

        /**
         * Triggered on button go back click to return to the main view with the map styles
         */
        goBackIV.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView = mapFragment.view as MapView
        mapView.contentDescription = "MAP NOT READY"
    }


    /**
     * All functions needed to load the activity_map_view layout page
     */
    private fun loadMapViewPage() {
        setContentView(R.layout.activity_map_view)
        displayMap()
        initViews()
        setTagsToViewsWithDrawable()
    }

    /**
     * Initialize UI elements
     */
    private fun initViews() {
        goBackIV = findViewById(R.id.go_back_iv)
    }

    /**
     * Set tags to UI elements with drawable icons (needed for testing)
     */
    private fun setTagsToViewsWithDrawable() {
        goBackIV.tag = R.drawable.ic_tomtom_arrow_left
    }

    private fun displayMap() {
        initMap()
    }

    /**
     * Displaying a map
     *
     * MapOptions is required to initialize the map.
     * Use MapFragment to display a map.
     * Optional configuration: You can further configure the map by setting various properties of the MapOptions object. You can learn more in the Map Configuration guide.
     * The last step is adding the MapFragment to the previously created container.
     */
    private fun initMap() {
        val mapOptions = MapOptions(mapKey = apiKey)
        mapFragment = MapFragment.newInstance(mapOptions)
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()
        mapFragment.getMapAsync { map ->
            tomTomMap = map
            mapView.contentDescription = "MAP READY"
            enableUserLocation()
        }
    }

    /**
     * The SDK provides a LocationProvider interface that is used between different modules to get location updates.
     * This examples uses the AndroidLocationProvider.
     * Under the hood, the engine uses Android’s system location services.
     */
    private fun initLocationProvider() {
        locationProvider = AndroidLocationProvider(context = this)
    }

    /**
     * In order to show the user’s location, the application must use the device’s location services, which requires the appropriate permissions.
     */
    private fun enableUserLocation() {
        if (areLocationPermissionsGranted()) {
            showUserLocation()
        } else {
            requestLocationPermission()
        }
    }

    /**
     * The LocationProvider itself only reports location changes. It does not interact internally with the map or navigation.
     * Therefore, to show the user’s location on the map you have to set the LocationProvider to the TomTomMap.
     * You also have to manually enable the location indicator.
     * It can be configured using the LocationMarkerOptions class.
     *
     * Read more about user location on the map in the Showing User Location guide.
     */
    private fun showUserLocation() {
        initLocationProvider()
        locationProvider.enable()
        // zoom to current location at city level
        onLocationUpdateListener = OnLocationUpdateListener { location ->
            tomTomMap.moveCamera(CameraOptions(location.position, zoom = 8.0))
            locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
        }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap.enableLocationMarker(locationMarker)
        mapView.contentDescription = "MAP WITH LOCATION"
    }

    private fun showDefaultLocation() {
        val amsterdam = GeoPoint(52.379189, 4.899431)
        val cameraOptions = CameraOptions(
            position = amsterdam,
            zoom = 8.0
        )
        tomTomMap.moveCamera(cameraOptions)
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            showUserLocation()
        } else {
            Toast.makeText(
                this,
                getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
            showDefaultLocation()
        }
    }

    private fun areLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}