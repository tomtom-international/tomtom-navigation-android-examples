package com.tomtom.sdk.examples.utils.matchers

import android.view.View
import android.widget.ImageView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

object ImageViewHasDrawableMatcher {
    fun hasDrawableSrc(expectedDrawableId: Int): BoundedMatcher<View, ImageView>{
        return object: BoundedMatcher<View, ImageView>(ImageView::class.java) {
            override fun describeTo(description: Description?) { //identifier for the test
                description?.appendText("has drawable")
            }

            override fun matchesSafely(item: ImageView?): Boolean {
                return (item?.drawable != null &&
                        item.tag == expectedDrawableId)
            }

        }
    }
}