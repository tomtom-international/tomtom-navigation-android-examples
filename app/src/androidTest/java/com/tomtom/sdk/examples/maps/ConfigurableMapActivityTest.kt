package com.tomtom.sdk.examples.maps

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.utils.matchers.ImageViewHasDrawableMatcher
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ConfigurableMapActivityTest {
    @get: Rule
    val activityRule : ActivityScenarioRule<ConfigurableMapActivity> = ActivityScenarioRule(ConfigurableMapActivity::class.java)

    @Test
    fun test_isActivityInView() {
        Espresso.onView(ViewMatchers.withId(R.id.configurable_map_view))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun test_onMapContainer_isDisplayed() { //check if it is displayed on screen
        Espresso.onView(ViewMatchers.withId(R.id.map_container))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun test_onGoBackIV_isDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.go_back_iv))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun test_onGoBackIV_isBackgroundDrawableDisplayed() {
        Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.withId(R.id.go_back_iv),
                ViewMatchers.hasBackground(R.drawable.circle), ViewMatchers.isDisplayed()
            )
        );
    }

    @Test
    fun test_onGoBackIV_hasDrawable() {
        Espresso.onView(ViewMatchers.withId(R.id.go_back_iv))
            .check(ViewAssertions.matches(ImageViewHasDrawableMatcher.hasDrawableSrc(R.drawable.ic_tomtom_arrow_left)))
    }
}