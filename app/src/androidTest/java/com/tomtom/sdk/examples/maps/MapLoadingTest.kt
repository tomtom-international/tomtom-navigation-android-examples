package com.tomtom.sdk.examples.maps

import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapLoadingTest {

    private lateinit var uiDevice: UiDevice

    companion object {
        const val SDK_VERSION_23 = 23
        const val SDK_VERSION_24 = 24
        const val SDK_VERSION_28 = 28
        const val SDK_VERSION_29 = 29
        const val ALLOW_TEXT_SDK_23 = "Allow"
        const val ALLOW_TEXT_SDK_28 = "ALLOW"
        const val ALLOW_TEXT_SDK_29 = "Allow only while using the app"
        const val ALLOW_TEXT_DEFAULT = "While using the app"
        const val DENY_TEXT_DEFAULT = "Deny"
        const val DENY_TEXT_SDK_24_TO_28 = "DENY"
        const val TIMEOUT_10 = 10000L
        const val TIMEOUT_5 = 5000L
        const val MAP_READY_DESC_TEXT = "MAP READY"
        const val MAP_WITH_LOCATION_DESC_TEXT = "MAP WITH LOCATION"
        const val MAP_DEFAULT_LOCATION_DESC_TEXT = "MAP DEFAULT LOCATION"
    }

    @get: Rule
    val activityRule : ActivityScenarioRule<ConfigurableMapActivity> = ActivityScenarioRule(ConfigurableMapActivity::class.java)

    @Before
    fun setUp() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testMapShowing() {
        val mapReadySelector = By.descContains(MAP_READY_DESC_TEXT) //specify criteria for matching UI elements
        val mapReadyObject = uiDevice.wait(Until.hasObject(mapReadySelector), TIMEOUT_10)
        // Assert if object with the description "MAP READY" is found on the screen
        assertTrue(mapReadyObject == true)
    }

    @Test
    fun testMapDefaultLocation_onPermissionDeny() {
        val denyPermissions = uiDevice.wait(Until.findObject(By.text(
            when (Build.VERSION.SDK_INT) {
                in SDK_VERSION_24..SDK_VERSION_28 -> DENY_TEXT_SDK_24_TO_28
                else -> DENY_TEXT_DEFAULT
            }
        )), TIMEOUT_10)
        try {
            denyPermissions.click()
            val mapLocationSelector = By.descContains(MAP_DEFAULT_LOCATION_DESC_TEXT)
            val mapLocationObject = uiDevice.wait(Until.hasObject(mapLocationSelector), TIMEOUT_5)

            assertTrue(mapLocationObject == true)
        } catch (e: UiObjectNotFoundException) {
            println("$e There is no permissions dialog to interact with ")
        }
    }

    @Test
    fun testMapUserLocation_onPermissionAllow() {
        val allowPermissions = uiDevice.wait(Until.findObject(By.text(
            when {
                Build.VERSION.SDK_INT == SDK_VERSION_23 -> ALLOW_TEXT_SDK_23
                Build.VERSION.SDK_INT <= SDK_VERSION_28 -> ALLOW_TEXT_SDK_28
                Build.VERSION.SDK_INT == SDK_VERSION_29 -> ALLOW_TEXT_SDK_29
                else -> ALLOW_TEXT_DEFAULT
            }
        )), TIMEOUT_10)
        try {
            allowPermissions.click()
            val mapLocationSelector = By.descContains(MAP_WITH_LOCATION_DESC_TEXT)
            val mapLocationObject = uiDevice.wait(Until.hasObject(mapLocationSelector), TIMEOUT_5)

            assertTrue(mapLocationObject == true)
        } catch (e: UiObjectNotFoundException) {
            println("$e There is no permissions dialog to interact with ")
        }
    }

}