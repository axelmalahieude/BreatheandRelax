package com.axel.breatheandrelax.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
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
import com.axel.breatheandrelax.view.DualChronometer;

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
        mChronometer = findViewById(R.id.chronometer);
        mIntroTextView = findViewById(R.id.tv_tap_to_start);
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

        // Set appropriate times if the activity is returning from being destroyed
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

        mChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEnableStopwatch) // if timer mode
                    launchTimePicker();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(CURRENT_MEDITATION_TIME, mMeditationTime);
        outState.putLong(START_MEDITATION_TIME, mMeditationStartTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMeditation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

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
     * if the chronometer is counting up.
     */
    @Override
    public void onChronometerFinished() {
        // meditation is over
        stopMeditation();
        Intent i = new Intent(this, MainMenuActivity.class);
        i.putExtra(CURRENT_MEDITATION_TIME, mMeditationStartTime);
        startActivity(i);
    }

    @Override
    public void setStopWatchTime(int minutes, int seconds) {
        mMeditationTime = mMeditationStartTime = ((minutes * 60) + seconds) * 1000;
        startMeditation();
    }

    @Override
    public void onDialogCancelled() {
        stopMeditation();
        mAskForTime = true;
    }

    /**
     * Creates the animation to be displayed based on the selection in Preferences
     * @param sharedPreferences the preferences to read from
     */
    public void createAnimation(SharedPreferences sharedPreferences) {
        // Fetch time breakdown from preferences
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
        mAnimation.setInhaleColor(colorStringToInt(colorInhale));
        String colorExhale = sharedPreferences.getString(getResources().getString(R.string.pref_colors_exhale_key), getResources().getString(R.string.pref_colors_exhale_default));
        mAnimation.setExhaleColor(colorStringToInt(colorExhale));
    }

    public void launchHelpDialog() {
        stopMeditation();
        DialogFragment help = new SingleMessageDialogFragment();

        Bundle args = new Bundle();
        args.putString(SingleMessageDialogFragment.MESSAGE_KEY, getResources().getString(R.string.help_text));
        args.putString(SingleMessageDialogFragment.BUTTON_LABEL_KEY, getResources().getString(R.string.acknowledgement_got_it));
        args.putString(SingleMessageDialogFragment.TITLE_KEY, getResources().getString(R.string.title_help_text));
        help.setArguments(args);
        help.show(getSupportFragmentManager(), "help");
    }

    public void launchTimePicker() {
        stopMeditation();
        DialogFragment dialog = new TimePickerDialogFragment();
        dialog.show(getSupportFragmentManager(), "timepicker");
        mAskForTime = false;
    }

    /**
     * Stops the breathing animation completely with no possibility to resume.
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // If any preferences were changed, SettingsActivity was launched, in which case we
        // want to reset the timer
        stopMeditation();
        mMeditationTime = mMeditationStartTime;

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
            mAnimation.setInhaleColor(colorStringToInt(colorString));
        } else if (key.equals(getResources().getString(R.string.pref_colors_exhale_key))) {
            String colorString = sharedPreferences.getString(key, getResources().getString(R.string.pref_colors_exhale_default));
            mAnimation.setExhaleColor(colorStringToInt(colorString));
        } else if (key.equals(getResources().getString(R.string.pref_animation_style_key))){
            createAnimation(sharedPreferences);
        }
    }

    /**
     * Converts a resource String to the associated color
     * @param colorString the String to convert
     * @return an integer representing colorString's associated color
     */
    public int colorStringToInt(String colorString) {
        if (colorString.equals(getResources().getString(R.string.pref_colors_red_value)))
            return getResources().getColor(R.color.breathe_red);
        if (colorString.equals(getResources().getString(R.string.pref_colors_green_value)))
            return getResources().getColor(R.color.breathe_green);
        if (colorString.equals(getResources().getString(R.string.pref_colors_blue_value)))
            return getResources().getColor(R.color.breathe_blue);
        if (colorString.equals(getResources().getString(R.string.pref_colors_yellow_value)))
            return getResources().getColor(R.color.breathe_yellow);
        else return -1;
    }

    @Override
    public void onDialogDismissed() {
        startMeditation();
    }
}
