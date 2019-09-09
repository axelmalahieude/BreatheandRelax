package com.axel.breatheandrelax.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.axel.breatheandrelax.R

/**
 * Activity class for settings. Uses a SettingsFragment for complete implementation.
 */

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // So that back button goes back to previous activity correctly (doesn't quit app)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
