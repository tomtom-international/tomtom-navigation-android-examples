/*
 * © 2023 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */
package com.tomtom.sdk.examples

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.sdk.examples.databinding.ActivityMainMenuBinding
import com.tomtom.sdk.examples.maps.MapExamplesActivity
import com.tomtom.sdk.examples.navigation.NavigationExamplesActivity
import com.tomtom.sdk.examples.offline.OfflineExamplesActivity
import com.tomtom.sdk.examples.routing.RoutingExamplesActivity
import com.tomtom.sdk.examples.search.SearchExamplesActivity
import com.tomtom.sdk.examples.usecase.MapModeSelectionActivity


class MainMenu : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun launchBasicDriving(button: View) {
        val myIntent = Intent(this@MainMenu, MapModeSelectionActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchMapDemo(button: View) {
        val myIntent = Intent(this@MainMenu, MapExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchNavigationDemo(button: View) {
        val myIntent = Intent(this@MainMenu, NavigationExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchOfflineMapsDemo(button: View) {
        val myIntent = Intent(this@MainMenu, OfflineExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchRoutingDemo(button: View) {
        val myIntent = Intent(this@MainMenu, RoutingExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchSearchDemo(button: View) {
        val myIntent = Intent(this@MainMenu, SearchExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }
}