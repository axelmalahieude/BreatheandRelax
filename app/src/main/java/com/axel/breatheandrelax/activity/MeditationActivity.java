package com.axel.breatheandrelax.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.axel.breatheandrelax.BallAnimation;
import com.axel.breatheandrelax.Movable;
import com.axel.breatheandrelax.R;
import com.axel.breatheandrelax.SizeAnimation;
import com.axel.breatheandrelax.fragment.SingleMessageDialogFragment;
import com.axel.breatheandrelax.fragment.TimePickerDialogFragment;
import com.axel.breatheandrelax.util.Tools;
import com.axel.breatheandrelax.view.DualChronometer;

/**
 * Activity class for the main breathing screen. Contains an animation and a timer,
 * with an action bar for navigation
 */

public class MeditationActivity extends AppCompatActivity implements
        DualChronometer.OnChronometerFinishedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        TimePickerDialogFragment.TimePickerDialogListener,
        SingleMessageDialogFragment.DialogFinishedListener {

    // Data members
    private Movable mAnimation; // animation featured in this Activity
    private long mMeditationTime; // dynamic meditation time in milliseconds
    private long mMeditationStartTime; // starting meditation time in milliseconds
    private boolean mEnableStopwatch; // whether the chronometer should count down
    private boolean mAskForTime; // flag to ask user for meditation time

    // Bundle variable codes
    public static final String CURRENT_MEDITATION_TIME = "current_time";
    public static final String START_MEDITATION_TIME = "starting_time";

    // Layout and view references
    private DualChronometer mChronometer;
    private TextView mIntroTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_meditation);

        // View references
        mChronometer = findViewById(R.id.chronometer);
        mIntroTextView = findViewById(R.id.tv_tap_to_start);

        // Adjust action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Fetch preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEnableStopwatch = sharedPreferences.getBoolean(getString(R.string.pref_enable_stopwatch_key), getResources().getBoolean(R.bool.pref_enable_stopwatch_default));

        // Register the SharedPreferences listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Create the animation
        createAnimation(sharedPreferences); // load the remainder of the preferences to make the animation

        // Set appropriate defaults if the activity is returning from being destroyed (i.e. orientation change)
        if (savedInstanceState != null) {
            mMeditationTime = savedInstanceState.getLong(CURRENT_MEDITATION_TIME);
            mMeditationStartTime = savedInstanceState.getLong(START_MEDITATION_TIME);
            mAskForTime = false;
        } else {
            mAskForTime = !mEnableStopwatch;
        }

        // Start or pause the animation after user input
        ConstraintLayout mScreen = findViewById(R.id.cl_main_layout);
        mScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnimation.isPlaying())
                    stopMeditation();
                else
                    startMeditation();
            }
        });

        // If user clicks on timer and it is in timer mode, launch TimePicker
        mChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEnableStopwatch) // if timer mode
                    launchTimePicker();
            }
        });
    }

    /**
     * Preserves current state before destroying the activity
     * @param outState the state to preserve
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(CURRENT_MEDITATION_TIME, mMeditationTime);
        outState.putLong(START_MEDITATION_TIME, mMeditationStartTime);
        super.onSaveInstanceState(outState);
    }

    /**
     * Safely dispose of SharedPreferences when destroying activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Stops the breathing animation when navigating away from the activity
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopMeditation();
    }

    /**
     * Creates the action bar menu
     * @param menu is the menu to inflate
     * @return true if the menu was successfully created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    /**
     * Manages menu item clicks
     * @param item the menu item that was clicked
     * @return true if the menu click was successfully handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.menu_help:
                launchHelpDialog();
                return true;
            case R.id.menu_restart:
                stopMeditation();
                mMeditationTime = mMeditationStartTime;
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called via interface when the chronometer has finished counting down. Will never be called
     * if the chronometer is counting up (since it can count to infinity)
     */
    @Override
    public void onChronometerFinished() {
        // meditation is over
        stopMeditation();
        Intent i = new Intent(this, MainMenuActivity.class);
        i.putExtra(CURRENT_MEDITATION_TIME, mMeditationStartTime);
        startActivity(i);
    }

    /**
     * Sets the chronometer to start at a specified time, then starts the animation
     * @param minutes number of minutes to start the chronometer at
     * @param seconds number of seconds to start the chronometer at
     */
    @Override
    public void setStopWatchTime(int minutes, int seconds) {
        mMeditationTime = mMeditationStartTime = ((minutes * 60) + seconds) * 1000;
        startMeditation();
    }

    /**
     * Creates the animation to be displayed with parameters from SharedPreferences
     * @param sharedPreferences the preferences to read from
     */
    public void createAnimation(SharedPreferences sharedPreferences) {
        // Fetch breathing times from preferences
        int inhaleTime = sharedPreferences.getInt(getString(R.string.pref_seekbar_inhale_key), getResources().getInteger(R.integer.inhale_uplifting_default));
        int exhaleTime = sharedPreferences.getInt(getString(R.string.pref_seekbar_exhale_key), getResources().getInteger(R.integer.exhale_uplifting_default));
        int holdTime = sharedPreferences.getInt(getString(R.string.pref_seekbar_hold_key), getResources().getInteger(R.integer.hold_uplifting_default));
        int pauseTime = sharedPreferences.getInt(getString(R.string.pref_seekbar_pause_key), getResources().getInteger(R.integer.pause_uplifting_default));

        // Create a new animation
        String animationStyle = sharedPreferences.getString(getResources().getString(R.string.pref_animation_style_key), getResources().getString(R.string.pref_animation_style_default));
        if (animationStyle.equals(getResources().getString(R.string.pref_animation_style_circle_value)))
            mAnimation = new BallAnimation(this, inhaleTime * 1000, exhaleTime * 1000, holdTime * 1000, pauseTime * 1000);
        else if (animationStyle.equals(getResources().getString(R.string.pref_animation_style_grow_value)))
            mAnimation = new SizeAnimation(this, inhaleTime * 1000, exhaleTime * 1000, holdTime * 1000, pauseTime * 1000);

        // Set the new animation's colors
        String colorInhale = sharedPreferences.getString(getResources().getString(R.string.pref_colors_inhale_key), getResources().getString(R.string.pref_colors_inhale_default));
        mAnimation.setInhaleColor(Tools.colorKeyToInt(getResources(), colorInhale));
        String colorExhale = sharedPreferences.getString(getResources().getString(R.string.pref_colors_exhale_key), getResources().getString(R.string.pref_colors_exhale_default));
        mAnimation.setExhaleColor(Tools.colorKeyToInt(getResources(), colorExhale));
    }

    /**
     * Launches the help dialog
     */
    public void launchHelpDialog() {
        stopMeditation();

        // Create dialog and set arguments
        DialogFragment help = new SingleMessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(SingleMessageDialogFragment.MESSAGE_KEY, getResources().getString(R.string.help_text));
        args.putString(SingleMessageDialogFragment.BUTTON_LABEL_KEY, getResources().getString(R.string.acknowledgement_got_it));
        args.putString(SingleMessageDialogFragment.TITLE_KEY, getResources().getString(R.string.title_help_text));
        help.setArguments(args);
        help.show(getSupportFragmentManager(), "help");
    }

    /**
     * Launch the TimePicker, which is necessary if the timer mode is enabled
     */
    public void launchTimePicker() {
        stopMeditation();

        // Create the dialog and launch it
        DialogFragment dialog = new TimePickerDialogFragment();
        dialog.show(getSupportFragmentManager(), "timepicker");
        mAskForTime = false;
    }

    /**
     * Stops the breathing animation.
     */
    private void stopMeditation() {
        if (mAnimation.isPlaying()) {
            mAnimation.stopBreathing();
            mChronometer.stop();
            if (!mEnableStopwatch) {
                mMeditationTime -= mChronometer.getTimeElapsed();
            } else {
                mMeditationTime = mChronometer.getTimeElapsed();
            }
        }
        mIntroTextView.setText(getResources().getString(R.string.tap_to_start));
        mIntroTextView.setVisibility(View.VISIBLE);
        mChronometer.setVisibility(View.INVISIBLE);
    }

    /**
     * Starts the breathing animation from scratch.
     */
    private void startMeditation() {
        mIntroTextView.setVisibility(View.INVISIBLE);
        if (mAskForTime) {
            launchTimePicker();
            return;
        }
        // Start the animation
        mAnimation.startBreathing();

        // Start the timer
        mChronometer.setCountDown(!mEnableStopwatch);
        if (!mEnableStopwatch)
            mChronometer.setCountDownStartTime(mMeditationTime);
        else {
            mChronometer.setCountUpStartTime(mMeditationTime);
        }
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mChronometer.setVisibility(View.VISIBLE);
    }

    /**
     * Listens for changes in Preferences and updates the activity's parameters accordingly
     * @param sharedPreferences the reference to SharedPreferences we can query from
     * @param key the preference that was modified
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // If any preferences were changed, SettingsActivity was launched, in which case we
        // want to reset the timer
        stopMeditation();
        mMeditationTime = mMeditationStartTime;

        // Determines which preference was changed and updates the animation accordingly
        if (key.equals(getString(R.string.pref_seekbar_inhale_key))) {
            mAnimation.setInhaleTime(sharedPreferences.getInt(key, getResources().getInteger(R.integer.inhale_uplifting_default)) * 1000);
        } else if (key.equals(getString(R.string.pref_seekbar_exhale_key))) {
            mAnimation.setExhaleTime(sharedPreferences.getInt(key, getResources().getInteger(R.integer.exhale_uplifting_default)) * 1000);
        } else if (key.equals(getString(R.string.pref_seekbar_hold_key))) {
            mAnimation.setHoldTime(sharedPreferences.getInt(key, getResources().getInteger(R.integer.hold_uplifting_default)) * 1000);
        } else if (key.equals(getString(R.string.pref_seekbar_pause_key))) {
            mAnimation.setPauseTime(sharedPreferences.getInt(key, getResources().getInteger(R.integer.pause_uplifting_default)) * 1000);
        } else if (key.equals(getResources().getString(R.string.pref_enable_stopwatch_key))) {
            mEnableStopwatch = sharedPreferences.getBoolean(getString(R.string.pref_enable_stopwatch_key),
                    getResources().getBoolean(R.bool.pref_enable_stopwatch_default));
            mAskForTime = !mEnableStopwatch;
            if (mEnableStopwatch)
                mMeditationStartTime = mMeditationTime = 0;
        } else if (key.equals(getResources().getString(R.string.pref_colors_inhale_key))) {
            String colorString = sharedPreferences.getString(key, getResources().getString(R.string.pref_colors_inhale_default));
            mAnimation.setInhaleColor(Tools.colorKeyToInt(getResources(), colorString));
        } else if (key.equals(getResources().getString(R.string.pref_colors_exhale_key))) {
            String colorString = sharedPreferences.getString(key, getResources().getString(R.string.pref_colors_exhale_default));
            mAnimation.setExhaleColor(Tools.colorKeyToInt(getResources(), colorString));
        } else if (key.equals(getResources().getString(R.string.pref_animation_style_key))){
            createAnimation(sharedPreferences);
        }
    }

    /**
     * Interface method called when returning from an open dialog. Sets the flag to
     * ask the user for how long to meditate for (if timer mode is enabled)
     */
    @Override
    public void onDialogCancelled() {
        stopMeditation();
        mAskForTime = true;
    }

    /**
     * Interface method called when dismissing a dialog. Automatically starts meditation.
     */
    @Override
    public void onDialogDismissed() {
        startMeditation();
    }
}
