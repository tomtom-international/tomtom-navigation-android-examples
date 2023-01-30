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
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.sdk.examples.maps.MapExamplesActivity
import com.tomtom.sdk.examples.navigation.NavigationExamplesActivity
import com.tomtom.sdk.examples.offline.OfflineExamplesActivity
import com.tomtom.sdk.examples.routing.RoutingExamplesActivity
import com.tomtom.sdk.examples.search.SearchExamplesActivity
import com.tomtom.sdk.examples.usecase.BasicNavigationActivity


class MainMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
    }

    fun launchBasicDriving(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, BasicNavigationActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchMapDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, MapExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchNavigationDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, NavigationExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchOfflineMapsDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, OfflineExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchRoutingDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, RoutingExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchSearchDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, SearchExamplesActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }
}