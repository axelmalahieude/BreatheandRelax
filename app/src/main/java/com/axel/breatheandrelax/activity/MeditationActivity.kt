package com.axel.breatheandrelax.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView

import com.axel.breatheandrelax.BallAnimation
import com.axel.breatheandrelax.Movable
import com.axel.breatheandrelax.R
import com.axel.breatheandrelax.SizeAnimation
import com.axel.breatheandrelax.fragment.SingleMessageDialogFragment
import com.axel.breatheandrelax.fragment.TimePickerDialogFragment
import com.axel.breatheandrelax.view.DualChronometer

/**
 * Activity class for the main breathing screen. Contains an animation and a timer,
 * with an action bar for navigation
 */

class MeditationActivity : AppCompatActivity(), DualChronometer.OnChronometerFinishedListener, SharedPreferences.OnSharedPreferenceChangeListener, TimePickerDialogFragment.TimePickerDialogListener, SingleMessageDialogFragment.DialogFinishedListener {

    // Data members
    private var mAnimation: Movable? = null // animation featured in this Activity
    private var mMeditationTime: Long = 0 // dynamic meditation time in milliseconds
    private var mMeditationStartTime: Long = 0 // starting meditation time in milliseconds
    private var mEnableStopwatch: Boolean = false // whether the chronometer should count down
    private var mAskForTime: Boolean = false // flag to ask user for meditation time

    // Layout and view references
    private var mChronometer: DualChronometer? = null
    private var mIntroTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_meditation)

        // View references
        mChronometer = findViewById(R.id.chronometer)
        mIntroTextView = findViewById(R.id.tv_tap_to_start)

        // Adjust action bar
        if (supportActionBar != null) {
            supportActionBar!!.elevation = 0f
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        // Fetch preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mEnableStopwatch = sharedPreferences.getBoolean(getString(R.string.pref_enable_stopwatch_key), resources.getBoolean(R.bool.pref_enable_stopwatch_default))

        // Register the SharedPreferences listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        // Create the animation
        createAnimation(sharedPreferences) // load the remainder of the preferences to make the animation

        // Set appropriate defaults if the activity is returning from being destroyed (i.e. orientation change)
        if (savedInstanceState != null) {
            mMeditationTime = savedInstanceState.getLong(CURRENT_MEDITATION_TIME)
            mMeditationStartTime = savedInstanceState.getLong(START_MEDITATION_TIME)
            mAskForTime = false
        } else {
            mAskForTime = !mEnableStopwatch
        }

        // Start or pause the animation after user input
        val mScreen = findViewById<ConstraintLayout>(R.id.cl_main_layout)
        mScreen.setOnClickListener {
            if (mAnimation!!.isPlaying)
                stopMeditation()
            else
                startMeditation()
        }

        // If user clicks on timer and it is in timer mode, launch TimePicker
        mChronometer!!.setOnClickListener {
            if (!mEnableStopwatch)
            // if timer mode
                launchTimePicker()
        }
    }

    /**
     * Preserves current state before destroying the activity
     * @param outState the state to preserve
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(CURRENT_MEDITATION_TIME, mMeditationTime)
        outState.putLong(START_MEDITATION_TIME, mMeditationStartTime)
        super.onSaveInstanceState(outState)
    }

    /**
     * Safely dispose of SharedPreferences when destroying activity
     */
    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Stops the breathing animation when navigating away from the activity
     */
    override fun onPause() {
        super.onPause()
        stopMeditation()
    }

    /**
     * Creates the action bar menu
     * @param menu is the menu to inflate
     * @return true if the menu was successfully created
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.settings, menu)
        return true
    }

    /**
     * Manages menu item clicks
     * @param item the menu item that was clicked
     * @return true if the menu click was successfully handled
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                val i = Intent(this, SettingsActivity::class.java)
                startActivity(i)
                return true
            }
            R.id.menu_help -> {
                launchHelpDialog()
                return true
            }
            R.id.menu_restart -> {
                stopMeditation()
                mMeditationTime = mMeditationStartTime
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Called via interface when the chronometer has finished counting down. Will never be called
     * if the chronometer is counting up (since it can count to infinity)
     */
    override fun onChronometerFinished() {
        // meditation is over
        stopMeditation()
        val i = Intent(this, MainMenuActivity::class.java)
        i.putExtra(CURRENT_MEDITATION_TIME, mMeditationStartTime)
        startActivity(i)
    }

    /**
     * Sets the chronometer to start at a specified time, then starts the animation
     * @param minutes number of minutes to start the chronometer at
     * @param seconds number of seconds to start the chronometer at
     */
    override fun setStopWatchTime(minutes: Int, seconds: Int) {
        mMeditationStartTime = ((minutes * 60 + seconds) * 1000).toLong()
        mMeditationTime = mMeditationStartTime
        startMeditation()
    }

    /**
     * Creates the animation to be displayed with parameters from SharedPreferences
     * @param sharedPreferences the preferences to read from
     */
    fun createAnimation(sharedPreferences: SharedPreferences) {
        // Fetch breathing times from preferences
        val inhaleTime = sharedPreferences.getInt(getString(R.string.pref_seekbar_inhale_key), resources.getInteger(R.integer.inhale_uplifting_default))
        val exhaleTime = sharedPreferences.getInt(getString(R.string.pref_seekbar_exhale_key), resources.getInteger(R.integer.exhale_uplifting_default))
        val holdTime = sharedPreferences.getInt(getString(R.string.pref_seekbar_hold_key), resources.getInteger(R.integer.hold_uplifting_default))
        val pauseTime = sharedPreferences.getInt(getString(R.string.pref_seekbar_pause_key), resources.getInteger(R.integer.pause_uplifting_default))

        // Create a new animation
        val animationStyle = sharedPreferences.getString(resources.getString(R.string.pref_animation_style_key), resources.getString(R.string.pref_animation_style_default))
        if (animationStyle == resources.getString(R.string.pref_animation_style_circle_value))
            mAnimation = BallAnimation(this, inhaleTime * 1000, exhaleTime * 1000, holdTime * 1000, pauseTime * 1000)
        else if (animationStyle == resources.getString(R.string.pref_animation_style_grow_value))
            mAnimation = SizeAnimation(this, inhaleTime * 1000, exhaleTime * 1000, holdTime * 1000, pauseTime * 1000)

        // Set the new animation's colors
        val colorInhale = sharedPreferences.getString(resources.getString(R.string.pref_colors_inhale_key), resources.getString(R.string.pref_colors_inhale_default))
        mAnimation!!.inhaleColor = colorStringToInt(colorInhale)
        val colorExhale = sharedPreferences.getString(resources.getString(R.string.pref_colors_exhale_key), resources.getString(R.string.pref_colors_exhale_default))
        mAnimation!!.exhaleColor = colorStringToInt(colorExhale)
    }

    /**
     * Launches the help dialog
     */
    fun launchHelpDialog() {
        stopMeditation()

        // Create dialog and set arguments
        val help = SingleMessageDialogFragment()
        val args = Bundle()
        args.putString(SingleMessageDialogFragment.MESSAGE_KEY, resources.getString(R.string.help_text))
        args.putString(SingleMessageDialogFragment.BUTTON_LABEL_KEY, resources.getString(R.string.acknowledgement_got_it))
        args.putString(SingleMessageDialogFragment.TITLE_KEY, resources.getString(R.string.title_help_text))
        help.arguments = args
        help.show(supportFragmentManager, "help")
    }

    /**
     * Launch the TimePicker, which is necessary if the timer mode is enabled
     */
    fun launchTimePicker() {
        stopMeditation()

        // Create the dialog and launch it
        val dialog = TimePickerDialogFragment()
        dialog.show(supportFragmentManager, "timepicker")
        mAskForTime = false
    }

    /**
     * Stops the breathing animation.
     */
    private fun stopMeditation() {
        if (mAnimation!!.isPlaying) {
            mAnimation!!.stopBreathing()
            mChronometer!!.stop()
            if (!mEnableStopwatch) {
                mMeditationTime -= mChronometer!!.timeElapsed
            } else {
                mMeditationTime = mChronometer!!.timeElapsed
            }
        }
        mIntroTextView!!.text = resources.getString(R.string.tap_to_start)
        mIntroTextView!!.visibility = View.VISIBLE
        mChronometer!!.visibility = View.INVISIBLE
    }

    /**
     * Starts the breathing animation from scratch.
     */
    private fun startMeditation() {
        mIntroTextView!!.visibility = View.INVISIBLE
        if (mAskForTime) {
            launchTimePicker()
            return
        }
        // Start the animation
        mAnimation!!.startBreathing()

        // Start the timer
        mChronometer!!.isCountDown = !mEnableStopwatch
        if (!mEnableStopwatch)
            mChronometer!!.setCountDownStartTime(mMeditationTime)
        else {
            mChronometer!!.setCountUpStartTime(mMeditationTime)
        }
        mChronometer!!.base = SystemClock.elapsedRealtime()
        mChronometer!!.start()
        mChronometer!!.visibility = View.VISIBLE
    }

    /**
     * Listens for changes in Preferences and updates the activity's parameters accordingly
     * @param sharedPreferences the reference to SharedPreferences we can query from
     * @param key the preference that was modified
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // If any preferences were changed, SettingsActivity was launched, in which case we
        // want to reset the timer
        stopMeditation()
        mMeditationTime = mMeditationStartTime

        // Determines which preference was changed and updates the animation accordingly
        if (key == getString(R.string.pref_seekbar_inhale_key)) {
            mAnimation!!.inhaleTime = sharedPreferences.getInt(key, resources.getInteger(R.integer.inhale_uplifting_default)) * 1000
        } else if (key == getString(R.string.pref_seekbar_exhale_key)) {
            mAnimation!!.exhaleTime = sharedPreferences.getInt(key, resources.getInteger(R.integer.exhale_uplifting_default)) * 1000
        } else if (key == getString(R.string.pref_seekbar_hold_key)) {
            mAnimation!!.holdTime = sharedPreferences.getInt(key, resources.getInteger(R.integer.hold_uplifting_default)) * 1000
        } else if (key == getString(R.string.pref_seekbar_pause_key)) {
            mAnimation!!.pauseTime = sharedPreferences.getInt(key, resources.getInteger(R.integer.pause_uplifting_default)) * 1000
        } else if (key == resources.getString(R.string.pref_enable_stopwatch_key)) {
            mEnableStopwatch = sharedPreferences.getBoolean(getString(R.string.pref_enable_stopwatch_key),
                    resources.getBoolean(R.bool.pref_enable_stopwatch_default))
            mAskForTime = !mEnableStopwatch
            if (mEnableStopwatch) {
                mMeditationTime = 0
                mMeditationStartTime = mMeditationTime
            }
        } else if (key == resources.getString(R.string.pref_colors_inhale_key)) {
            val colorString = sharedPreferences.getString(key, resources.getString(R.string.pref_colors_inhale_default))
            mAnimation!!.inhaleColor = colorStringToInt(colorString)
        } else if (key == resources.getString(R.string.pref_colors_exhale_key)) {
            val colorString = sharedPreferences.getString(key, resources.getString(R.string.pref_colors_exhale_default))
            mAnimation!!.exhaleColor = colorStringToInt(colorString)
        } else if (key == resources.getString(R.string.pref_animation_style_key)) {
            createAnimation(sharedPreferences)
        }
    }

    /**
     * Converts a resource String to the associated color
     * @param colorString the String to convert (found in strings.xml)
     * @return an integer representing colorString's associated color in hexadecimal
     */
    fun colorStringToInt(colorString: String): Int {
        if (colorString == resources.getString(R.string.pref_colors_red_value))
            return resources.getColor(R.color.breathe_red)
        if (colorString == resources.getString(R.string.pref_colors_green_value))
            return resources.getColor(R.color.breathe_green)
        if (colorString == resources.getString(R.string.pref_colors_blue_value))
            return resources.getColor(R.color.breathe_blue)
        return if (colorString == resources.getString(R.string.pref_colors_yellow_value))
            resources.getColor(R.color.breathe_yellow)
        else
            -1
    }

    /**
     * Interface method called when returning from an open dialog. Sets the flag to
     * ask the user for how long to meditate for (if timer mode is enabled)
     */
    override fun onDialogCancelled() {
        stopMeditation()
        mAskForTime = true
    }

    /**
     * Interface method called when dismissing a dialog. Automatically starts meditation.
     */
    override fun onDialogDismissed() {
        startMeditation()
    }

    companion object {

        // Bundle variable codes
        val CURRENT_MEDITATION_TIME = "current_time"
        val START_MEDITATION_TIME = "starting_time"
    }
}
