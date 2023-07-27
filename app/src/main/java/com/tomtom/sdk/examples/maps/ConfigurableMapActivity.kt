package com.tomtom.sdk.examples.maps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tomtom.sdk.examples.BuildConfig
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.databinding.ActivityMapOptionsListBinding
import com.tomtom.sdk.examples.databinding.ActivityMapViewBinding
import com.tomtom.sdk.examples.maps.mapdetails.BaseMapStyleDataAdapter
import com.tomtom.sdk.examples.maps.mapdetails.BaseMapStyleItem
import com.tomtom.sdk.examples.maps.mapdetails.DataLoader
import com.tomtom.sdk.examples.maps.mapdetails.MapPreference
import com.tomtom.sdk.examples.maps.mapdetails.MapPreferenceDataAdapter
import com.tomtom.sdk.examples.maps.mapdetails.MapPreferenceItem
import com.tomtom.sdk.examples.maps.mapdetails.OnRecyclerViewItemClickListener
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.style.LoadingStyleFailure
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.style.StyleLoadingCallback
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.MapView

/**
 * This activity is responsible for displaying the TomTom Vector Map, both with location and without.
 */
class ConfigurableMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapViewBinding
    private lateinit var mapOptionsDialogBinding: ActivityMapOptionsListBinding
    private lateinit var mapView: MapView
    private lateinit var mapFragment: MapFragment
    private lateinit var tomTomMap: TomTomMap
    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private lateinit var bottomOptionsDialog: BottomSheetDialog
    private var baseMapStyleList: MutableList<BaseMapStyleItem>? = null
    private var currentBaseMapStyle: StyleMode = StyleMode.MAIN
    private var currentMapStyleDescriptor: StyleDescriptor = StandardStyles.BROWSING
    private var isMapChanged: Boolean = false
    private var areMapPreferencesChanged = false
    private lateinit var mapBaseStyleToUpgrade: StyleMode
    private lateinit var mapStyleDescriptorToUpgrade: StyleDescriptor

    private var mapPreferencesList: MutableList<MapPreferenceItem>? = null
    private var currentMapPreferences = mutableMapOf<String, MapPreference>()
    private var mapPreferencesToUpgrade = mutableMapOf<String, MapPreference>()

    /**
     * Navigation SDK is only available upon request.
     * Use the API key provided by TomTom to start using the SDK.
     */
    private val apiKey = BuildConfig.TOMTOM_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initMap()

        /**
         * Triggered on button go back click to return to the main view with the map styles
         */
        binding.goBackImageButton.setOnClickListener {
            onBackPressed()
        }

        binding.mapOptionsImageButton.setOnClickListener {
            showBottomOptionsDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView = mapFragment.view as MapView
        mapView.contentDescription = applicationContext.resources.getString(R.string.map_not_ready)
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
        onLocationUpdateListener = OnLocationUpdateListener { location ->
            tomTomMap.moveCamera(CameraOptions(location.position, zoom = CAMERA_ZOOM_CITY_LEVEL))
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
            zoom = CAMERA_ZOOM_CITY_LEVEL
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

    private fun showBottomOptionsDialog() {
        bottomOptionsDialog = BottomSheetDialog(this)
        mapOptionsDialogBinding = ActivityMapOptionsListBinding.inflate(layoutInflater)
        bottomOptionsDialog.setContentView(mapOptionsDialogBinding.root)
        bottomOptionsDialog.show()

        mapOptionsDialogBinding.optionsTitleTextView.setOnClickListener {
            bottomOptionsDialog.dismiss()
        }

        bottomOptionsDialog.setOnDismissListener {
            if(isMapChanged) {
                upgradeMap()
            }
            if(areMapPreferencesChanged) {
                upgradeMapPreferences()
            }
            isMapChanged = false
            areMapPreferencesChanged = false
        }

        initBaseMapStyleList()
        initMapPreferencesList()
        initCurrentMapPreferences()

        val baseMapStyleRecyclerList: RecyclerView = mapOptionsDialogBinding.baseMapStylesRecyclerList
        val mapPreferenceRecyclerList: RecyclerView = mapOptionsDialogBinding.mapPreferencesRecyclerList

        setUpRecyclerListForBaseMapStyle(baseMapStyleRecyclerList)
        setUpRecyclerListForMapPreferences(mapPreferenceRecyclerList)
    }

    private val listener: OnRecyclerViewItemClickListener = object : OnRecyclerViewItemClickListener {
        override fun onBaseMapStyleItemClick(baseMapStyleItem: BaseMapStyleItem) {
            if(currentBaseMapStyle != baseMapStyleItem.styleMode || currentMapStyleDescriptor != baseMapStyleItem.styleDescriptor) {
                mapBaseStyleToUpgrade = baseMapStyleItem.styleMode
                mapStyleDescriptorToUpgrade = baseMapStyleItem.styleDescriptor
                isMapChanged = true
            } else {
                isMapChanged = false
            }
        }

        override fun onMapPreferenceItemClick(title: String, mapPreferenceItemMethod: MapPreference) {
            areMapPreferencesChanged = if(!currentMapPreferences.containsValue(mapPreferenceItemMethod)) {
                mapPreferencesToUpgrade[title] = mapPreferenceItemMethod
                true
            } else {
                if(mapPreferencesToUpgrade.containsKey(title)) {
                    mapPreferencesToUpgrade.remove(title)
                }
                false
            }
        }
    }

    private fun upgradeMap() {
        val onStyleLoadedCallback = object: StyleLoadingCallback {
            override fun onSuccess() {
                currentMapStyleDescriptor = mapStyleDescriptorToUpgrade
            }

            override fun onFailure(failure: LoadingStyleFailure) {
                Log.d("ConfigurableMapActivity.kt: Error on Upgrade Map with Style Descriptor", failure.message)
            }
        }
        tomTomMap.setStyleMode(mapBaseStyleToUpgrade)
        tomTomMap.loadStyle(mapStyleDescriptorToUpgrade, onStyleLoadedCallback)
        currentBaseMapStyle = mapBaseStyleToUpgrade
    }

    private fun upgradeMapPreferences() {
        mapPreferencesToUpgrade.forEach { (key, value) ->
            val method = tomTomMap.javaClass.getMethod(value.methodName)
            method.invoke(tomTomMap)
            currentMapPreferences[key] = value
        }

        mapPreferencesToUpgrade.clear()
    }

    private fun setUpRecyclerListForBaseMapStyle(baseMapStyleRecyclerList: RecyclerView) {
        createGridLayout(baseMapStyleRecyclerList, LAYOUT_SPAN_2_RECYCLER_VIEW)
        createAdapterForBaseMapStyleData(baseMapStyleRecyclerList)
    }

    private fun setUpRecyclerListForMapPreferences(mapPreferenceRecyclerList: RecyclerView) {
        createGridLayout(mapPreferenceRecyclerList, LAYOUT_SPAN_1_RECYCLER_VIEW)
        createAdapterForMapPreferencesData(mapPreferenceRecyclerList)
    }

    private fun createGridLayout(recyclerList: RecyclerView, layoutSpanCount: Int) {
        val gridLayoutManager = GridLayoutManager(this, layoutSpanCount)
        recyclerList.layoutManager = gridLayoutManager
    }

    private fun createAdapterForBaseMapStyleData(baseMapStyleRecyclerList: RecyclerView) {
        val baseMapStyleDataAdapter = BaseMapStyleDataAdapter(baseMapStyleRecyclerList, baseMapStyleList!!, listener, currentBaseMapStyle, currentMapStyleDescriptor)
        baseMapStyleRecyclerList.adapter = baseMapStyleDataAdapter
    }

    private fun createAdapterForMapPreferencesData(mapPreferenceRecyclerList: RecyclerView) {
        val mapPreferenceDataAdapter = MapPreferenceDataAdapter(mapPreferencesList!!, listener, currentMapPreferences)
        mapPreferenceRecyclerList.adapter = mapPreferenceDataAdapter
    }

    private fun initBaseMapStyleList() {
        if(baseMapStyleList == null) {
            baseMapStyleList = DataLoader.initBaseMapStyleList()
        }
    }

    private fun initMapPreferencesList() {
        if(mapPreferencesList == null)
            mapPreferencesList = DataLoader.initMapPreferencesList()
    }

    private fun initCurrentMapPreferences() {
        if(currentMapPreferences.isEmpty()) {
            for (item in mapPreferencesList!!) {
                currentMapPreferences[item.title] = item.methodHide
            }
        }
    }

    companion object {
        const val AMSTERDAM_GEO_POINT_LATITUDE = 52.379189
        const val AMSTERDAM_GEO_POINT_LONGITUDE = 4.899431
        const val CAMERA_ZOOM_CITY_LEVEL = 8.0
        const val LAYOUT_SPAN_1_RECYCLER_VIEW = 1
        const val LAYOUT_SPAN_2_RECYCLER_VIEW = 2
    }
}