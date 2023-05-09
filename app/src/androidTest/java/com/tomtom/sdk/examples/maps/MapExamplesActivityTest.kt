package com.tomtom.sdk.examples.maps

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.utils.matchers.ButtonViewHasDrawableMatcher.hasDrawable
import com.tomtom.sdk.examples.utils.matchers.ImageViewHasDrawableMatcher.hasDrawableSrc
import com.tomtom.sdk.examples.utils.matchers.TextViewHasCompoundDrawableMatcher.hasCompoundDrawable
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule

@RunWith(JUnit4::class)
class MapExamplesActivityTest {

    @get: Rule
    val activityRule : ActivityScenarioRule<MapExamplesActivity> = ActivityScenarioRule(MapExamplesActivity::class.java)

    @Test
    fun test_onButtonTryClick_isNavigatedToConfigMapView() {
        onView(withId(R.id.button_try)).perform(click())
        onView(withId(R.id.configurable_map_view)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onGoBackClick_isNavigatedToMapExamples() {
        onView(withId(R.id.button_try)).perform(click())
        onView(withId(R.id.configurable_map_view)).check(matches(isDisplayed()))
//        onView(withId(R.id.go_back_iv)).perform(click()) //method 1
        pressBack() //method 2
        onView(withId(R.id.map_examples))
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_isActivityInView() {
//        val activityScenario = ActivityScenario.launch(MapExamplesActivity::class.java)
        onView(withId(R.id.map_examples)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onMapContainer_isDisplayed() { //check if it is displayed on screen
        onView(withId(R.id.map_container)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onImageView_isDisplayed() {
        onView(withId(R.id.map_box_imageView)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onImageView_hasDrawable() {
        onView(withId(R.id.map_box_imageView)).check(matches(hasDrawableSrc(R.drawable.rounded_map_box)))
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
        onView(withId(R.id.dropdown)).check(matches(hasCompoundDrawable(R.drawable.ic_tomtom_arrow_up, 2)))
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
    fun test_onButtonTry_isDisplayed() {
        onView(withId(R.id.button_try)).check(matches(isDisplayed()))
    }

    @Test
    fun test_onButtonTry_isVisible() {
        onView(withId(R.id.button_try)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun test_onButtonTry_isBackgroundDrawableDisplayed() {
        onView(allOf(withId(R.id.button_try),
            hasBackground(R.drawable.button_primary), isDisplayed()));
    }

    @Test
    fun test_onButtonTry_isTextDisplayed() {
        onView(withId(R.id.button_try)).check(matches(withText(R.string.try_it_mapdemo)))
    }

    @Test
    fun test_onButtonTry_isPlayDrawableDisplayedLeft() {
        onView(withId(R.id.button_try)).check(matches(hasDrawable(R.drawable.ic_tomtom_play, 0)))
    }

//    onView(withId(R.id.dropdown_layout)).perform(click())
}