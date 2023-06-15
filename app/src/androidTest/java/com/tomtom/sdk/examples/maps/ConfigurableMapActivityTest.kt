package com.tomtom.sdk.examples.maps

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
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

    companion object {
        const val SDK_VERSION_23 = 23
        const val SDK_VERSION_28 = 28
        const val SDK_VERSION_29 = 29
        const val ALLOW_TEXT_SDK_23 = "Allow"
        const val ALLOW_TEXT_SDK_28 = "ALLOW"
        const val ALLOW_TEXT_SDK_29 = "Allow only while using the app"
        const val ALLOW_TEXT_DEFAULT = "While using the app"
        const val TIMEOUT = 10000L
    }

    @get: Rule
    val activityRule : ActivityScenarioRule<ConfigurableMapActivity> = ActivityScenarioRule(ConfigurableMapActivity::class.java)

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowPermissions = uiDevice.wait(
            Until.findObject(
                By.text(
                    when {
                        Build.VERSION.SDK_INT == SDK_VERSION_23 -> ALLOW_TEXT_SDK_23
                        Build.VERSION.SDK_INT <= SDK_VERSION_28 -> ALLOW_TEXT_SDK_28
                        Build.VERSION.SDK_INT == SDK_VERSION_29 -> ALLOW_TEXT_SDK_29
                        else -> ALLOW_TEXT_DEFAULT
                    }
                )), TIMEOUT)
        try {
            allowPermissions.click()
        } catch (e: UiObjectNotFoundException) {
            println("$e There is no permissions dialog to interact with ")
        }
    }

    @Test
    fun test_isActivityInView() {
        Espresso.onView(withId(R.id.configurable_map_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_onMapContainer_isDisplayed() { //check if it is displayed on screen
        Espresso.onView(withId(R.id.map_container))
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_onGoBackImageButton_isDisplayed() {
        Espresso.onView(withId(R.id.go_back_image_button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_onGoBackImageButton_isBackgroundDrawableDisplayed() {
        Espresso.onView(
            CoreMatchers.allOf(withId(R.id.go_back_image_button),
                ViewMatchers.hasBackground(R.drawable.circle), isDisplayed()
            )
        )
    }

    @Test
    fun test_onGoBackImageButton_hasDrawable() {
        Espresso.onView(withId(R.id.go_back_image_button))
            .check(matches(ImageViewHasDrawableMatcher.hasDrawableSrc(R.drawable.ic_tomtom_arrow_left, context)))
    }
}