package com.tomtom.sdk.examples.usecase

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView
import com.tomtom.sdk.examples.R


class MapModeSelectionActivity : AppCompatActivity() {
    private var onlineButton: MaterialCardView? = null
    private var offlineButton: MaterialCardView? = null
    private var hybridButton: MaterialCardView? = null
    private var mapMode = MapMode.ONLINE

    companion object {
        private const val STROKE_WIDTH = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_mode_selection)
        val toolbar = findViewById<View>(R.id.toolbar) as? Toolbar
        setSupportActionBar(toolbar);

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        initRadioButtons()

        findViewById<Button>(R.id.bt_proceed).setOnClickListener {
            proceed()
        }
    }

    private fun initRadioButtons(){
        onlineButton = findViewById(R.id.online_button)
        hybridButton = findViewById(R.id.hybrid_button)
        offlineButton = findViewById(R.id.offline_button)

        onlineButton?.isSelected = true

        onlineButton?.setOnClickListener {
            selectButton(onlineButton)
            mapMode = MapMode.ONLINE
        }

        /*
        hybridButton?.setOnClickListener {
            selectButton(hybridButtonId)
            mapMode = MapMode.HYBRID

            // In order to activate, please remove the comment lines,
            // and remove the darker gray tint of ImageView in the xml file
        }

        offlineButton?.setOnClickListener {
            selectButton(offlineButtonId)
            mapMode = MapMode.OFFLINE

            // In order to activate, please remove the comment lines,
            // and remove the darker gray tint of ImageView in the xml file
        }
        */
    }

    private fun proceed() {
        when (mapMode) {
            MapMode.ONLINE -> {
                val myIntent = Intent(this, BasicNavigationActivity::class.java)
                this.startActivity(myIntent)
            }
            MapMode.HYBRID -> {
                TODO()
            }
            MapMode.OFFLINE -> {
                TODO()
            }
        }
    }

    private fun selectButton(selectedButton: MaterialCardView?) {
        onlineButton?.strokeWidth = 0
        hybridButton?.strokeWidth = 0
        offlineButton?.strokeWidth = 0

        val density = resources.displayMetrics.density
        val strokeWidthPixels = (STROKE_WIDTH * density).toInt()

        selectedButton?.strokeWidth = strokeWidthPixels
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.info_button, menu);

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_info -> showInfoDialog()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInfoDialog(): Boolean{
        val dialogView = layoutInflater.inflate(R.layout.dialog_info_map_mode, null)

        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        val dialogButton = dialogView.findViewById<ImageButton>(R.id.bt_close)

        dialogButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        return true
    }

    enum class MapMode {
        ONLINE,
        HYBRID,
        OFFLINE
    }
}