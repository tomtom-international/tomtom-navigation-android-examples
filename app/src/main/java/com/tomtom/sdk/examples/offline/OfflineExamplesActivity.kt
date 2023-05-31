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
package com.tomtom.sdk.examples.offline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.sdk.examples.databinding.ActivityOfflineExamplesBinding

class OfflineExamplesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOfflineExamplesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOfflineExamplesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}