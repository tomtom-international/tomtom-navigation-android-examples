//package com.tomtom.sdk.examples.maps
//
//import android.animation.ObjectAnimator
//import android.graphics.drawable.Drawable
//import android.graphics.drawable.LayerDrawable
//import android.graphics.drawable.RotateDrawable
//import android.widget.TextView
//import junit.framework.TestCase.assertEquals
//import junit.framework.TestCase.assertTrue
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mockito.*
//import org.mockito.junit.MockitoJUnitRunner
//
//@RunWith(MockitoJUnitRunner::class)
//class MapExamplesActivityTest {
//    private lateinit var dropdown: TextView
//    private lateinit var rotateDrawable: RotateDrawable
//    private lateinit var layerDrawable: LayerDrawable
//
//    @Before
//    fun setUp() {
//        dropdown = mock(TextView::class.java)
//        rotateDrawable = mock(RotateDrawable::class.java)
//        layerDrawable = mock(LayerDrawable::class.java)
//        dropdown.setCompoundDrawablesWithIntrinsicBounds(null, null, layerDrawable, null)
//        rotateDrawable.drawable = mock(Drawable::class.java)
//    }
//
//    @Test
//    fun `test animateRotation`() {
//        // Set up initial values
//        rotateDrawable.fromDegrees = 0f
//        rotateDrawable.toDegrees = 0f
//        val animator = mock(ObjectAnimator::class.java)
//
//        // Set the desired scenario
//        val isArrowUp = true
//        `when`(layerDrawable.getDrawable(0)).thenReturn(rotateDrawable)
//        `when`(animator.duration).thenReturn(500)
//
//        val activity = MapExamplesActivity()
//        activity.animateRotation()
//
//        // Verify the updated values
//        assertEquals(0f, rotateDrawable.fromDegrees)
//        assertEquals(180f, rotateDrawable.toDegrees)
//        assertTrue(activity.isArrowUp != isArrowUp) // Verify that the arrow direction is toggled
//
//        // Verify the animator
//        verify(animator).setIntValues(0, 10000)
//        verify(animator).duration = 500
//        verify(animator).start()
//    }
//}