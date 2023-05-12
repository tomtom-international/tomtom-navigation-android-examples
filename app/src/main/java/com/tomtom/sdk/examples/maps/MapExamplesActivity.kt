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
    private lateinit var vectorMapImageView: ImageView
    private lateinit var detailsText: TextView
    private lateinit var tryItIcon: ImageView
    private lateinit var tryItLayoutButton: LinearLayout
    private lateinit var dropdownLayout: LinearLayout
    private lateinit var dropdown: TextView
    private lateinit var rotateDrawable: RotateDrawable
    private lateinit var layerDrawable: LayerDrawable
//    private lateinit var animator: ObjectAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMapExamplesPage()
    }

    /**
     * Initialize UI elements
     */
    private fun initViews() {
        vectorMapImageView = findViewById(R.id.vector_map_iv)
        detailsText = findViewById(R.id.details)
        tryItIcon = findViewById(R.id.try_it_icon)
        tryItLayoutButton = findViewById(R.id.try_it_layout_button)
        dropdownLayout = findViewById(R.id.dropdown_layout)
        dropdown = findViewById(R.id.dropdown)
    }

    /**
     * Set tags to UI elements with drawable icons (needed for testing)
     */
    private fun setTagsToViewsWithDrawable() {
        vectorMapImageView.tag = R.drawable.img_tomtom_vector_map
        dropdown.tag = R.drawable.ic_tomtom_arrow_up
        tryItIcon.tag = R.drawable.ic_tomtom_play
    }


    /**
     * Enable the 'changing' transition type on the layout object, which means that any changes
     * made to the layout will trigger the transition animation specified in the layoutTransition object.
     */
    private fun enableTransitionOnMapDescription() {
        dropdownLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    /**
     * Triggered on click of button "Try it" to change layout activity
     */
    fun tryMapView(view: View) {
        val intent = Intent(this, ConfigurableMapActivity::class.java)
        startActivity(intent)
    }

    /**
     * All functions needed to load the activity_map_examples layout page
     */
    private fun loadMapExamplesPage() {
        setContentView(R.layout.activity_map_examples)
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
        TransitionManager.beginDelayedTransition(tryItLayoutButton, AutoTransition())
        detailsText.visibility = if (detailsText.visibility == View.GONE) View.VISIBLE else View.GONE
        animateRotation()
    }

    /**
     * Set up the elements for animated rotation
     */
    private fun initRotationElements() {
        val drawable = dropdown.compoundDrawables[2] // Get the end drawable

        rotateDrawable = RotateDrawable()
        rotateDrawable.drawable = drawable
        rotateDrawable.level = 10000 // Set the level to show the drawable
        rotateDrawable.toDegrees = 0f // Set the initial toDegrees value to 0 degrees

        layerDrawable = LayerDrawable(arrayOf(rotateDrawable)) // Create a LayerDrawable and add the RotateDrawable to it
        dropdown.setCompoundDrawablesWithIntrinsicBounds(null, null, layerDrawable, null)

        /**
        //Rotates from both sides (like making 360 degrees) - optimized
//        val drawable = dropdown.compoundDrawables[2] // Get the drawable
//
//        rotateDrawable = RotateDrawable().apply {
//            this.drawable = drawable
//            level = 10000 // Set the level to show the drawable (fully visible)
//            toDegrees = 0f // Set the initial toDegrees value to 0 degrees
//        }
//
//        layerDrawable = LayerDrawable(arrayOf(rotateDrawable)) // Create a LayerDrawable and add the RotateDrawable to it
//        dropdown.setCompoundDrawablesWithIntrinsicBounds(null, null, layerDrawable, null)
//
//        animator = ObjectAnimator.ofInt(layerDrawable.getDrawable(0), "level", 0, 10000).apply {
//            duration = 500
        */
//        }
    }
    /**
     * Rotate with animation the arrow icon by 180 degrees relative to its current rotation.
     */
    private var isArrowUp: Boolean = true
    private fun animateRotation() {
        // Rotate the drawable by 180 degrees from its previous direction
        rotateDrawable.fromDegrees = rotateDrawable.toDegrees
        rotateDrawable.toDegrees = if (isArrowUp) 180f else 0f
        isArrowUp = !isArrowUp // Toggle the arrow's direction

        // Create a new animator with the updated rotation angles
        val animator = ObjectAnimator.ofInt(layerDrawable.getDrawable(0), "level", 0, 10000)
        animator.duration = 500

        animator.start()

        /**
//        Rotate in 360 degrees(optimized)
        val currentRotation = rotateDrawable.toDegrees % 360 // Get the current rotation of the arrow
        rotateDrawable.fromDegrees = currentRotation
        rotateDrawable.toDegrees = currentRotation + 180f // Rotate the arrow by 180 degrees

        // Update the animator with the new target drawable and animation parameters
        animator.target = layerDrawable.getDrawable(0)
        animator.setIntValues(0, 10000)

        animator.start()
        */
    }
}