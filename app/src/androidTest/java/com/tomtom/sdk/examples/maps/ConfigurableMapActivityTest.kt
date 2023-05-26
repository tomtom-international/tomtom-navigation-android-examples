package com.tomtom.sdk.examples.maps

import android.os.Build
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.Until
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.utils.matchers.ImageViewHasDrawableMatcher
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ConfigurableMapActivityTest {
    @get: Rule
    val activityRule : ActivityScenarioRule<ConfigurableMapActivity> = ActivityScenarioRule(ConfigurableMapActivity::class.java)

    @Before
    fun setUp() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowPermissions = uiDevice.wait(
            Until.findObject(
                By.text(
                    when {
                        Build.VERSION.SDK_INT == 23 -> "Allow"
                        Build.VERSION.SDK_INT <= 28 -> "ALLOW"
                        Build.VERSION.SDK_INT == 29 -> "Allow only while using the app"
                        else -> "While using the app"
                    }
                )), 10000L)
        try {
            allowPermissions.click()
        } catch (e: UiObjectNotFoundException) {
            println("$e There is no permissions dialog to interact with ")
        }
    }
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
        )
    }

    @Test
    fun test_onGoBackIV_hasDrawable() {
        Espresso.onView(ViewMatchers.withId(R.id.go_back_iv))
            .check(ViewAssertions.matches(ImageViewHasDrawableMatcher.hasDrawableSrc(R.drawable.ic_tomtom_arrow_left)))
    }
}