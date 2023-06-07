package com.tomtom.sdk.examples.usecase

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.tomtom.sdk.examples.R
import com.tomtom.sdk.examples.databinding.ActivityMapModeSelectionBinding


class MapModeSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapModeSelectionBinding
    private var mapMode = MapMode.ONLINE

    companion object {
        private const val STROKE_WIDTH = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapModeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar);

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        initRadioButtons()

        binding.btProceed.setOnClickListener {
            proceed()
        }
    }

    private fun initRadioButtons() {
        binding.btOnline.isSelected = true

        binding.btOnline.setOnClickListener {
            selectButton(binding.btOnline)
            mapMode = MapMode.ONLINE
        }

        /*
        hybridButton?.setOnClickListener {
            selectButton(binding.btHybrid)
            mapMode = MapMode.HYBRID

            // In order to activate, please remove the comment lines,
            // and remove the darker gray tint of ImageView in the xml file
        }

        offlineButton?.setOnClickListener {
            selectButton(binding.btOffline)
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
                startActivity(myIntent)
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
        binding.btOnline.strokeWidth = 0
        binding.btHybrid.strokeWidth = 0
        binding.btOffline.strokeWidth = 0

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

    private fun showInfoDialog(): Boolean {
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