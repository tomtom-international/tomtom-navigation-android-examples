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
import com.tomtom.sdk.examples.maps.MapDemoActivity
import com.tomtom.sdk.examples.navigation.NavigationDemoActivity
import com.tomtom.sdk.examples.offline.OfflineMapsDemoActivity
import com.tomtom.sdk.examples.routing.RoutingDemoActivity
import com.tomtom.sdk.examples.search.SearchDemoActivity
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
        val myIntent = Intent(this@MainMenu, MapDemoActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchNavigationDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, NavigationDemoActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchOfflineMapsDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, OfflineMapsDemoActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchRoutingDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, RoutingDemoActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }

    fun launchSearchDemo(button : android.view.View) {
        val myIntent = Intent(this@MainMenu, SearchDemoActivity::class.java)
        this@MainMenu.startActivity(myIntent)
    }
}