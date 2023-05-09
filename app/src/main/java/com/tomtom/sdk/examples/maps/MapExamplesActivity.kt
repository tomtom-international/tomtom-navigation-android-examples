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
package com.tomtom.sdk.examples.maps

import android.Manifest
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tomtom.sdk.examples.BuildConfig
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment


/**
 * This example shows the appearance of the TomTom Vector Map, lists its features and implements its display mechanism.
 *
 */

class MapExamplesActivity : AppCompatActivity() {
    private lateinit var mapFragment: MapFragment
    private lateinit var tomTomMap: TomTomMap
    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private lateinit var imageView: ImageView
    private lateinit var detailsText: TextView
    private lateinit var tryItButton: Button
    private lateinit var tryItLayout: LinearLayout
    private lateinit var dropdownLayout: LinearLayout
    private lateinit var dropdown: TextView
    private lateinit var rotateDrawable: RotateDrawable
    private lateinit var layerDrawable: LayerDrawable
    private lateinit var animator: ObjectAnimator


    /**
     * Navigation SDK is only available upon request.
     * Use the API key provided by TomTom to start using the SDK.
     */
    private val apiKey = BuildConfig.TOMTOM_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMapExamplesPage()
    }

    /**
     * Initialize UI elements
     */
    private fun initViews() {
        imageView = findViewById(R.id.map_box_imageView)
        detailsText = findViewById(R.id.details)
        tryItButton = findViewById(R.id.button_try)
        tryItLayout = findViewById(R.id.tryItBtn_layout)
        dropdownLayout = findViewById(R.id.dropdown_layout)
        dropdown = findViewById(R.id.dropdown)
    }

    /**
     * Set tags to UI elements with drawable icons (needed for testing)
     */
    private fun setTagsToViewsWithDrawable() {
        imageView.tag = R.drawable.rounded_map_box
        dropdown.tag = R.drawable.ic_tomtom_arrow_up
        tryItButton.tag = R.drawable.ic_tomtom_play
    }


    /**
     * Enable the 'changing' transition type on the layout object, which means that any changes
     * made to the layout will trigger the transition animation specified in the layoutTransition object.
     */
    private fun enableTransitionOnMapDescription() {
        dropdownLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun displayMapWithLocation() {
        initMap()
        initLocationProvider()
    }

    /**
     * Triggered on click of button "Try it" to change layout activity
     */
    fun tryMapView(view: View) {
        val intent = Intent(this, ConfigurableMapActivity::class.java)
        startActivity(intent)
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
        }
    }

    private fun areLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        locationProvider.close()
        super.onDestroy()
    }

    /**
     * All functions needed to load the activity_map_examples layout page
     */
    private fun loadMapExamplesPage() {
        setContentView(R.layout.activity_map_examples)
        displayMapWithLocation()
        initViews()
        setTagsToViewsWithDrawable()
        enableTransitionOnMapDescription()
        initRotationElements()
    }

    /**
     * Toggle the visibility of the detailsText and tryItButton views by using the TransitionManager
     * and AutoTransition() to apply a visual transition animation effect when views are shown or hidden.
     */
    fun expand(view: View) {
        TransitionManager.beginDelayedTransition(dropdownLayout, AutoTransition())
        TransitionManager.beginDelayedTransition(tryItLayout, AutoTransition())
        detailsText.visibility = if (detailsText.visibility == View.GONE) View.VISIBLE else View.GONE
//        tryItButton.visibility = if (tryItButton.visibility == View.GONE) View.VISIBLE else View.GONE
        animateRotation()
    }

    /**
     * Set up the elements for animated rotation
     */
    private fun initRotationElements() {
        //Rotates from both sides (like making 360 degrees) - optimized
        val drawable = dropdown.compoundDrawables[2] // Get the drawable

        rotateDrawable = RotateDrawable().apply {
            this.drawable = drawable
            level = 10000 // Set the level to show the drawable (fully visible)
            toDegrees = 0f // Set the initial toDegrees value to 0 degrees
        }

        layerDrawable = LayerDrawable(arrayOf(rotateDrawable)) // Create a LayerDrawable and add the RotateDrawable to it
        dropdown.setCompoundDrawablesWithIntrinsicBounds(null, null, layerDrawable, null)

        animator = ObjectAnimator.ofInt(layerDrawable.getDrawable(0), "level", 0, 10000).apply {
            duration = 500
        }


        /** Rotates only from one side
        val drawable = dropdown.compoundDrawables[2] // Get the end drawable

        rotateDrawable = RotateDrawable()
        rotateDrawable.drawable = drawable
        rotateDrawable.level = 10000 // Set the level to show the drawable
        rotateDrawable.toDegrees = 0f // Set the initial toDegrees value to 0 degrees

        layerDrawable = LayerDrawable(arrayOf(rotateDrawable)) // Create a LayerDrawable and add the RotateDrawable to it
        dropdown.setCompoundDrawablesWithIntrinsicBounds(null, null, layerDrawable, null)

        */
        /**Rotates from both sides (like making 360 degrees)
        val drawable = dropdown.compoundDrawables[2] // Get the end drawable

        // Create a RotateDrawable instance and set the initial rotation angles to 0 degrees
        rotateDrawable = RotateDrawable()
        rotateDrawable.fromDegrees = 0f
        rotateDrawable.toDegrees = 0f

        rotateDrawable.drawable = drawable
        rotateDrawable.level = 10000 // Set the level to show the drawable

        layerDrawable = LayerDrawable(arrayOf(rotateDrawable)) // Create a LayerDrawable and add the RotateDrawable to it

        dropdown.setCompoundDrawablesWithIntrinsicBounds(null, null, layerDrawable, null)
        */
    }

    /**
     * Rotate with animation the arrow icon by 180 degrees relative to its current rotation.
     */
    private fun animateRotation() {
        val currentRotation = rotateDrawable.toDegrees % 360 // Get the current rotation of the arrow
        rotateDrawable.fromDegrees = currentRotation
        rotateDrawable.toDegrees = currentRotation + 180f // Rotate the arrow by 180 degrees

        // Update the animator with the new target drawable and animation parameters
        animator.target = layerDrawable.getDrawable(0)
        animator.setIntValues(0, 10000)

        animator.start()

        /**Rotates only from one side
        // Rotate the drawable by 180 degrees from its previous direction
        rotateDrawable.fromDegrees = rotateDrawable.toDegrees
        rotateDrawable.toDegrees = if (isArrowUp) 180f else 0f
        isArrowUp = !isArrowUp // Toggle the arrow's direction

        // Create a new animator with the updated rotation angles
        val animator = ObjectAnimator.ofInt(layerDrawable.getDrawable(0), "level", 0, 10000)
        animator.duration = 500

        animator.start()

        */
        /**Rotates from both sides (like making 360 degrees)
        rotateDrawable.fromDegrees = rotateDrawable.toDegrees
        rotateDrawable.toDegrees = rotateDrawable.fromDegrees + 180f

        // Create a new animator with the updated rotation angles
        val animator = ObjectAnimator.ofInt(layerDrawable.getDrawable(0), "level", 0, 10000)
        animator.duration = 500

        animator.start()
        */
    }
}