package com.tomtom.sdk.examples.utils.matchers

import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

object TextViewHasCompoundDrawableMatcher {
    fun hasCompoundDrawable(expectedDrawableId: Int, expectedDrawablePosition: Int): BoundedMatcher<View, TextView> {
        return object: BoundedMatcher<View, TextView>(TextView::class.java) {
            override fun describeTo(description: Description?) { //identifier for the test
                description?.appendText("has compound drawable")
            }

            override fun matchesSafely(item: TextView?): Boolean { //the logic that the test requires to pass

                return (item?.compoundDrawables?.get(expectedDrawablePosition) != null) &&
                        (item.tag == expectedDrawableId)
            }

        }
    }
}