package com.tomtom.sdk.examples.maps

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.utils.matchers.CardViewHasRadiusMatcher.hasCardCornerRadius
import com.tomtom.sdk.examples.utils.matchers.ImageViewHasDrawableMatcher.hasDrawableSrc
import com.tomtom.sdk.examples.utils.matchers.TextViewHasCompoundDrawableMatcher.hasCompoundDrawable
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MapExamplesActivityTest {

    @get: Rule
    val activityRule : ActivityScenarioRule<MapExamplesActivity> = ActivityScenarioRule(MapExamplesActivity::class.java)

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun test_onTryItLayoutButtonClick_isNavigatedToConfigMapView() {
        onView(withId(R.id.try_it_layout_button)).perform(scrollTo(), click())
        onView(withId(R.id.configurable_map_view)).check(matches(isDisplayed()))
    }

    @Test
    fun test_isActivityInView() {
        onView(withId(R.id.map_examples)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onVectorMapCardView_isDisplayedWithCornerRadius() {
        onView(withId(R.id.vector_map_card))
            .check(matches(isDisplayed()))
            .check(matches(hasCardCornerRadius(7f)))
    }

    @Test
    fun test_onVectorMapImageView_isDisplayed() {
        onView(withId(R.id.vector_map_iv)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onVectorMapImageView_hasDrawable() {
        onView(withId(R.id.vector_map_iv)).check(matches(hasDrawableSrc(R.drawable.img_tomtom_vector_map, context)))
    }

    @Test
    fun test_onDropdown_isDisplayed() {
        onView(withId(R.id.dropdown)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onDropdown_isTitleDisplayed() {
        onView(withId(R.id.dropdown)).check(matches(withText(R.string.conf_map_title)))
    }

    @Test
    fun test_onDropdown_hasCompoundDrawableOnRight() {
        onView(withId(R.id.dropdown)).check(matches(hasCompoundDrawable(R.drawable.ic_tomtom_arrow_up, 2, context)))
    }

    @Test
    fun test_onDetails_isDisplayed() {
        onView(withId(R.id.details)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onDetails_isTextDisplayed() {
        onView(withId(R.id.details)).check(matches(withText(R.string.conf_map_description)))
    }

    @Test
    fun test_onDetails_isVisible() {
        onView(withId(R.id.details)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun test_onTryItLayoutButton_isDisplayed() {
        onView(withId(R.id.try_it_layout_button))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_onTryItLayoutButton_isBackgroundDrawableDisplayed() {
        onView(allOf(withId(R.id.try_it_layout_button),
            hasBackground(R.drawable.button_primary), isDisplayed()))
    }

    @Test
    fun test_onTryItText_isTextDisplayed() {
        onView(withId(R.id.try_it_text)).check(matches(withText(R.string.try_it_mapdemo)))
    }

    @Test
    fun test_onTryItIcon_isPlayDrawableDisplayedLeft() {
        onView(withId(R.id.try_it_icon)).check(matches(hasDrawableSrc(R.drawable.ic_tomtom_play, context)))
    }
}