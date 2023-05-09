package com.tomtom.sdk.examples.utils.matchers

import android.view.View
import android.widget.Button
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

object ButtonViewHasDrawableMatcher {
    fun hasDrawable(expectedDrawableId: Int, expectedDrawablePosition: Int): BoundedMatcher<View, Button> {
        return object: BoundedMatcher<View, Button>(Button::class.java) {
            override fun describeTo(description: Description?) { //identifier for the test
                description?.appendText("has drawable")
            }

            override fun matchesSafely(item: Button?): Boolean { //the logic that the test requires to pass
                return (item?.compoundDrawables?.get(expectedDrawablePosition) != null) &&
                        (item.tag == expectedDrawableId)
            }

        }
    }
}