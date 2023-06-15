package com.tomtom.sdk.examples.utils.matchers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

object ImageViewHasDrawableMatcher {
    fun hasDrawableSrc(expectedDrawableId: Int, context: Context): BoundedMatcher<View, ImageView>{
        return object: BoundedMatcher<View, ImageView>(ImageView::class.java) {
            override fun describeTo(description: Description?) { //identifier for the test
                description?.appendText("has drawable")
            }

            override fun matchesSafely(item: ImageView?): Boolean {
                val actualDrawable: Drawable? = ContextCompat.getDrawable(context, expectedDrawableId)
                val expectedDrawable: Drawable? = item?.drawable

                if (expectedDrawable is BitmapDrawable && actualDrawable is BitmapDrawable) {
                    val expectedBitmap: Bitmap = expectedDrawable.bitmap
                    val actualBitmap: Bitmap = actualDrawable.bitmap
                    return expectedBitmap.sameAs(actualBitmap)
                }
                return false
            }

        }
    }
}