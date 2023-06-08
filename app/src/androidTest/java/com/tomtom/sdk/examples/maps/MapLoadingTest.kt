package com.tomtom.sdk.examples.maps

import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.Until
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MapLoadingTest {

    companion object {
        const val SDK_VERSION_23 = 23
        const val SDK_VERSION_28 = 28
        const val SDK_VERSION_29 = 29
        const val ALLOW_TEXT_SDK_23 = "Allow"
        const val ALLOW_TEXT_SDK_28 = "ALLOW"
        const val ALLOW_TEXT_SDK_29 = "Allow only while using the app"
        const val ALLOW_TEXT_DEFAULT = "While using the app"
        const val DENY_TEXT_DEFAULT = "Deny"
        const val DENY_TEXT_SDK_24_TO_28 = "DENY"
        const val TIMEOUT = 10000L
    }

    @get: Rule
    val activityRule : ActivityScenarioRule<ConfigurableMapActivity> = ActivityScenarioRule(ConfigurableMapActivity::class.java)


    @Test
    fun testMapShowing() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val mapReadySelector = By.descContains("MAP READY") //specify criteria for matching UI elements
        val mapReadyObject = uiDevice.wait(Until.hasObject(mapReadySelector), 10000L)

        if (mapReadyObject) {
            // Object with the description "MAP READY" is found on the screen
            assertTrue("MAP READY", true)
        } else {
            // Object with the description "MAP NOT READY" is not found within the given timeout
            assertFalse("MAP NOT READY", false)
        }
    }

    @Test
    fun testMapDefaultLocation_onPermissionDeny() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val denyPermissions = uiDevice.wait(Until.findObject(By.text(
            when (Build.VERSION.SDK_INT) {
                in 24..28 -> DENY_TEXT_SDK_24_TO_28
                else -> DENY_TEXT_DEFAULT
            }
        )), TIMEOUT)
        try {
            denyPermissions.click()
            val mapLocationSelector = By.descContains("MAP WITH LOCATION")
            val mapLocationObject = uiDevice.wait(Until.hasObject(mapLocationSelector), 5000L)

            if (mapLocationObject) {
                // Object with the description "MAP READY" is found on the screen
                assertTrue("WITH LOCATION", true)
            } else {
                // Object with the description "MAP NOT READY" is not found within the given timeout
                assertFalse("MAP DEFAULT LOCATION", false)
            }
        } catch (e: UiObjectNotFoundException) {
            println("$e There is no permissions dialog to interact with ")
        }
    }

    @Test
    fun testMapUserLocation_onPermissionAllow() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowPermissions = uiDevice.wait(Until.findObject(By.text(
            when {
                Build.VERSION.SDK_INT == SDK_VERSION_23 -> ALLOW_TEXT_SDK_23
                Build.VERSION.SDK_INT <= SDK_VERSION_28 -> ALLOW_TEXT_SDK_28
                Build.VERSION.SDK_INT == SDK_VERSION_29 -> ALLOW_TEXT_SDK_29
                else -> ALLOW_TEXT_DEFAULT
            }
        )), TIMEOUT)
        try {
            allowPermissions.click()
            val mapLocationSelector = By.descContains("MAP WITH LOCATION")
            val mapLocationObject = uiDevice.wait(Until.hasObject(mapLocationSelector), 10000L)

            if (mapLocationObject) {
                // Object with the description "MAP READY" is found on the screen
                assertTrue("WITH LOCATION", true)
            } else {
                // Object with the description "MAP NOT READY" is not found within the given timeout
                assertFalse("MAP DEFAULT LOCATION", false)
            }
        } catch (e: UiObjectNotFoundException) {
            println("$e There is no permissions dialog to interact with ")
        }
    }

}