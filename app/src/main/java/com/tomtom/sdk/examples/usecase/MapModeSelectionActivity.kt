package com.tomtom.sdk.examples.usecase

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.tomtom.sdk.examples.R


class MapModeSelectionActivity : AppCompatActivity() {
    private var onlineButton: MaterialCardView? = null
    private var offlineButton: MaterialCardView? = null
    private var hybridButton: MaterialCardView? = null
    private var mapMode = MapMode.ONLINE

    companion object {
        private const val STROKE_WIDTH = 4
        private const val DIALOG_WIDTH = 1300
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_mode_selection)
        val toolbar = findViewById<View>(R.id.toolbar) as? Toolbar
        setSupportActionBar(toolbar);

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        val onlineButtonId = R.id.online_button
        val hybridButtonId = R.id.hybrid_button
        val offlineButtonId = R.id.offline_button

        onlineButton = findViewById(onlineButtonId)
        hybridButton = findViewById(hybridButtonId)
        offlineButton = findViewById(offlineButtonId)

        onlineButton?.isSelected = true

        onlineButton?.setOnClickListener {
            selectButton(onlineButtonId)
            mapMode = MapMode.ONLINE
        }

        /*hybridButton?.setOnClickListener {
            selectButton(hybridButtonId)
            mapMode = MapMode.HYBRID
        }

        offlineButton?.setOnClickListener {
            selectButton(offlineButtonId)
            mapMode = MapMode.OFFLINE
        }*/

        findViewById<Button>(R.id.bt_proceed).setOnClickListener {
            proceed()
        }
    }

    private fun proceed(){
        when(mapMode){
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

    private fun selectButton(selectedButtonId: Int) {
        onlineButton?.strokeWidth = 0
        hybridButton?.strokeWidth = 0
        offlineButton?.strokeWidth = 0

        val density = resources.displayMetrics.density
        val strokeWidthPixels = (STROKE_WIDTH * density).toInt()

        findViewById<MaterialCardView>(selectedButtonId).strokeWidth = strokeWidthPixels
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.info_button, menu);

        val infoMenuItem = menu?.findItem(R.id.action_info)

        val iconTint = ContextCompat.getColor(this, R.color.active_color)
        val icon = infoMenuItem?.icon
        icon?.setColorFilter(iconTint, PorterDuff.Mode.SRC_IN)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_info -> {
                val dialogView = layoutInflater.inflate(R.layout.dialog_info_map_mode, null)

                val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
                    .setView(dialogView)
                    .create()

                val dialogButton = dialogView.findViewById<ImageButton>(R.id.bt_close)

                dialogButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.setOnShowListener {
                    val dialogWindow = dialog.window
                    val layoutParams = WindowManager.LayoutParams()
                    layoutParams.copyFrom(dialogWindow?.attributes)
                    layoutParams.width = DIALOG_WIDTH
                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                    dialogWindow?.attributes = layoutParams
                }
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    enum class MapMode {
        ONLINE,
        HYBRID,
        OFFLINE
    }
}