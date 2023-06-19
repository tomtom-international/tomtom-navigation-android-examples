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

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.sdk.examples.databinding.ActivityMapExamplesBinding

/**
 * This example shows the appearance of the TomTom Vector Map, lists its features and implements its display mechanism.
 *
 */

class MapExamplesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapExamplesBinding
    private lateinit var rotateDrawable: RotateDrawable
    private lateinit var layerDrawable: LayerDrawable

    companion object {
        // The index value that stands for right as drawable icon position relative to the text in TextView
        const val DRAWABLE_TO_RIGHT_SIDE_INDEX = 2

        // The value that stands for the maximum state or full visibility level of a drawable icon in ObjectAnimator
        const val FULL_DRAWABLE_VISIBILITY = 10000

        // The value that stands for invisibility level of a drawable icon in ObjectAnimator
        const val DRAWABLE_INVISIBILITY = 0

        const val DRAWABLE_PROPERTY_LEVEL = "level"

        // The value that sets the degrees of the initial rotation angle on a drawable icon
        const val INITIAL_ROTATION_ANGLE_DRAWABLE = 0f

        // The value that sets the degrees of the target rotation angle on a drawable icon
        const val TARGET_ROTATION_ANGLE_DRAWABLE = 180f

        // The value that sets the duration of the animated rotation on a drawable icon
        const val ROTATION_ANIMATION_DURATION: Long = 500
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapExamplesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        loadMapExamplesPage()

        binding.tryItLayoutButton.setOnClickListener {
            tryMapView()
        }

        binding.dropdownLayout.setOnClickListener {
            expand()
        }
    }


    /**
     * Enable the 'changing' transition type on the layout object, which means that any changes
     * made to the layout will trigger the transition animation specified in the layoutTransition object.
     */
    private fun enableTransitionOnMapDescription() {
        binding.dropdownLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    /**
     * Triggered on click of button "Try it" to change layout activity
     */
    private fun tryMapView() {
        val intent = Intent(this, ConfigurableMapActivity::class.java)
        startActivity(intent)
    }

    /**
     * All functions needed to load the activity_map_examples layout page
     */
    private fun loadMapExamplesPage() {
        enableTransitionOnMapDescription()
        initRotationElements()
    }

    /**
     * Toggle the visibility of the detailsText and tryItButton views by using the TransitionManager
     * and AutoTransition() to apply a visual transition animation effect when views are shown or hidden.
     */
    private fun expand() {
        TransitionManager.beginDelayedTransition(binding.dropdownLayout, AutoTransition())
        TransitionManager.beginDelayedTransition(binding.tryItLayoutButton, AutoTransition())
        binding.details.visibility = if (binding.details.visibility == View.GONE) View.VISIBLE else View.GONE
        animateRotation()
    }

    /**
     * Set up the elements for animated rotation
     */
    private fun initRotationElements() {
        val drawable = binding.dropdown.compoundDrawables[DRAWABLE_TO_RIGHT_SIDE_INDEX] // Get the right-end drawable

        rotateDrawable = RotateDrawable()
        rotateDrawable.drawable = drawable
        rotateDrawable.level = FULL_DRAWABLE_VISIBILITY
        rotateDrawable.toDegrees = INITIAL_ROTATION_ANGLE_DRAWABLE

        layerDrawable = LayerDrawable(arrayOf(rotateDrawable)) // Create a LayerDrawable and add the RotateDrawable to it
        binding.dropdown.setCompoundDrawablesWithIntrinsicBounds(null, null, layerDrawable, null)
    }
    /**
     * Rotate with animation the arrow icon by 180 degrees relative to its current rotation.
     */
    private var isArrowUp: Boolean = true
    private fun animateRotation() {
        rotateDrawable.fromDegrees = rotateDrawable.toDegrees
        rotateDrawable.toDegrees = if (isArrowUp) TARGET_ROTATION_ANGLE_DRAWABLE else INITIAL_ROTATION_ANGLE_DRAWABLE
        isArrowUp = !isArrowUp

        // Create a new animator with the updated rotation angles
        val animator = ObjectAnimator.ofInt(layerDrawable.getDrawable(0), DRAWABLE_PROPERTY_LEVEL, DRAWABLE_INVISIBILITY, FULL_DRAWABLE_VISIBILITY)
        animator.duration = ROTATION_ANIMATION_DURATION

        animator.start()
    }
}