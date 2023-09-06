package com.tomtom.sdk.examples.maps

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasBackground
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
    fun test_on_try_it_click_is_navigated_to_config_map_view() {
        onView(withId(R.id.try_it_layout_button)).perform(scrollTo(), click())
        onView(withId(R.id.configurable_map_view)).check(matches(isDisplayed()))
    }

    @Test
    fun test_is_activity_in_view() {
        onView(withId(R.id.map_examples)).check(matches(isDisplayed()))
    }

    @Test
    fun test_on_vector_map_card_view_is_displayed_with_corner_radius() {
        onView(withId(R.id.vector_map_card))
            .check(matches(isDisplayed()))
            .check(matches(hasCardCornerRadius(7f)))
    }

    @Test
    fun test_on_vector_map_image_view_is_displayed() {
        onView(withId(R.id.vector_map_iv)).check(matches(isDisplayed()))
    }

    @Test
    fun test_on_vector_map_image_view_has_drawable() {
        onView(withId(R.id.vector_map_iv)).check(matches(hasDrawableSrc(R.drawable.img_tomtom_vector_map, context)))
    }

    @Test
    fun test_on_dropdown_is_displayed() {
        onView(withId(R.id.dropdown)).check(matches(isDisplayed()))
    }

    @Test
    fun test_on_dropdown_is_title_displayed() {
        onView(withId(R.id.dropdown)).check(matches(withText(R.string.conf_map_title)))
    }

    @Test
    fun test_on_dropdown_has_compound_drawable_on_right() {
        onView(withId(R.id.dropdown)).check(matches(hasCompoundDrawable(R.drawable.ic_tomtom_arrow_up, 2, context)))
    }

    @Test
    fun test_on_details_is_displayed() {
        onView(withId(R.id.details)).check(matches(isDisplayed()))
    }

    @Test
    fun test_on_details_is_text_displayed() {
        onView(withId(R.id.details)).check(matches(withText(R.string.conf_map_description)))
    }

    @Test
    fun test_on_details_is_visible() {
        onView(withId(R.id.details)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun test_on_try_it_layout_button_is_displayed() {
        onView(withId(R.id.try_it_layout_button))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_on_try_it_layout_button_is_background_drawable_displayed() {
        onView(allOf(withId(R.id.try_it_layout_button),
            hasBackground(R.drawable.button_primary), isDisplayed()))
    }

    @Test
    fun test_on_try_it_text_is_text_displayed() {
        onView(withId(R.id.try_it_text)).check(matches(withText(R.string.try_it_mapdemo)))
    }

    @Test
    fun test_on_try_it_icon_is_drawable_displayed_left() {
        onView(withId(R.id.try_it_icon)).check(matches(hasDrawableSrc(R.drawable.ic_tomtom_play, context)))
    }
}