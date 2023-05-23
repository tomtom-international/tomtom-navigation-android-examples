package com.tomtom.sdk.examples.utils.matchers

import android.view.View
import androidx.cardview.widget.CardView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

object CardViewHasRadiusMatcher {

    fun hasCardCornerRadius(cornerRadius: Float): BoundedMatcher<View, CardView> {
        return object : BoundedMatcher<View, CardView>(CardView::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("has corner radius: $cornerRadius")
            }

            override fun matchesSafely(item: CardView): Boolean {
                val cornerRadiusDP = item.radius / item.resources.displayMetrics.density
                return cornerRadiusDP == cornerRadius
            }
        }
    }

}