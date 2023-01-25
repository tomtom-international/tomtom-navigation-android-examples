package com.tomtom.demo.navdemoapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


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