package com.tomtom.sdk.examples.utils.matchers

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description


object TextViewHasCompoundDrawableMatcher {
    fun hasCompoundDrawable(expectedDrawableId: Int, expectedDrawablePosition: Int, context: Context): BoundedMatcher<View, TextView> {
        return object: BoundedMatcher<View, TextView>(TextView::class.java) {
            override fun describeTo(description: Description?) { //identifier for the test
                description?.appendText("has compound drawable")
            }

            override fun matchesSafely(item: TextView?): Boolean {

                val actualDrawable: Drawable? = context.getDrawable(expectedDrawableId)
                var expectedDrawable: Drawable? = item?.compoundDrawables?.get(expectedDrawablePosition)

                if (expectedDrawable is LayerDrawable) {
                    expectedDrawable = layerDrawableToBitmapDrawable((expectedDrawable as LayerDrawable?)!!)
                }

                if (expectedDrawable is BitmapDrawable && actualDrawable is BitmapDrawable) {
                    val bitmap1 = expectedDrawable.bitmap
                    val bitmap2 = actualDrawable.bitmap
                    return bitmap1.sameAs(bitmap2)
                }

                return false
            }

            fun layerDrawableToBitmapDrawable(layerDrawable: LayerDrawable): BitmapDrawable {
                // Get the bounds of the LayerDrawable & Create a bitmap with the same dimensions
                val width = layerDrawable.intrinsicWidth
                val height = layerDrawable.intrinsicHeight
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                // Create a canvas and draw the LayerDrawable on the bitmap
                val canvas = Canvas(bitmap)
                layerDrawable.setBounds(0, 0, width, height)
                layerDrawable.draw(canvas)

                // Create a new BitmapDrawable from the bitmap
                return BitmapDrawable(Resources.getSystem(), bitmap)
            }
        }
    }
}