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
import com.tomtom.sdk.examples.databinding.ActivityMapViewBinding
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
 */
class ConfigurableMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapViewBinding
    private lateinit var mapView: MapView
    private lateinit var mapFragment: MapFragment
    private lateinit var tomTomMap: TomTomMap
    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private lateinit var goBackImageButtonView: ImageView

    companion object {
        const val AMSTERDAM_GEO_POINT_LATITUDE = 52.379189
        const val AMSTERDAM_GEO_POINT_LONGITUDE = 4.899431
        const val CAMERA_ZOOM_VALUE = 8.0
    }

    /**
     * Navigation SDK is only available upon request.
     * Use the API key provided by TomTom to start using the SDK.
     */
    private val apiKey = BuildConfig.TOMTOM_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        loadMapViewPage()

        /**
         * Triggered on button go back click to return to the main view with the map styles
         */
        goBackImageButtonView.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView = mapFragment.view as MapView
        mapView.contentDescription = applicationContext.resources.getString(R.string.map_not_ready)
    }

    /**
     * All functions needed to load the activity_map_view layout page
     */
    private fun loadMapViewPage() {
        initMap()
        initializeUIElements()
        setTagsToViewsWithDrawable()
    }

    private fun initializeUIElements() {
        goBackImageButtonView = binding.goBackImageButton
    }

    /**
     * Set tags to UI elements with drawable icons (needed for testing)
     */
    private fun setTagsToViewsWithDrawable() {
        goBackImageButtonView.tag = R.drawable.ic_tomtom_arrow_left
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
            mapView.contentDescription = applicationContext.resources.getString(R.string.map_ready)
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
            tomTomMap.moveCamera(CameraOptions(location.position, zoom = CAMERA_ZOOM_VALUE))
            locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
        }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap.enableLocationMarker(locationMarker)
        mapView.contentDescription = applicationContext.resources.getString(R.string.map_with_location)
    }

    private fun showDefaultLocation() {
        val amsterdam = GeoPoint(AMSTERDAM_GEO_POINT_LATITUDE, AMSTERDAM_GEO_POINT_LONGITUDE)
        val cameraOptions = CameraOptions(
            position = amsterdam,
            zoom = CAMERA_ZOOM_VALUE
        )
        tomTomMap.moveCamera(cameraOptions)
        mapView.contentDescription = applicationContext.resources.getString(R.string.map_default_location)
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