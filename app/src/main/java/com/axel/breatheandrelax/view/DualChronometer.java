package com.axel.breatheandrelax.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

import java.util.Locale;

/**
 * Extends the Chronometer to count both up and down according to how it is
 * programmatically set. This allows both timer and stopwatch modes without
 * using two different views.
 */

public class DualChronometer extends Chronometer {

    // Data members
    private boolean mIsCountDown;
    private long mCountDownStartTime; // time to start counting down from, in milliseconds
    private long mCountUpStartTime; // time to start counting up from, in milliseconds
    private OnChronometerFinishedListener mListener;

    /**
     * Interface to signal parent activity that the timer has concluded. The parent activity
     * must implement this method.
     */
    public interface OnChronometerFinishedListener {
        public void onChronometerFinished();
    }

    /**
     * Necessary constructor to override the Chronometer view
     * @param context the parent activity's context
     */
    public DualChronometer(Context context) {
        super(context);
        setDefaults(context);
    }

    /**
     * Necessary constructor to override the Chronometer view
     * @param context the parent activity's context
     * @param attrs the XML attributes of the view
     */
    public DualChronometer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaults(context);
    }

    /**
     * Necessary constructor to override the Chronometer view
     * @param context the parent activity's context
     * @param attrs the XML attributes of the view
     * @param defStyleAttr the style for the XML attributes
     */
    public DualChronometer (Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDefaults(context);
    }

    /**
     * Whether the Chronometer is in countdown mode or not.
     * @return
     */
    @Override
    public boolean isCountDown() {
        return mIsCountDown;
    }

    /**
     * Sets default values for the chronometer. Called from each constructor.
     * @param context is the context of the chronometer's parent activity
     */
    private void setDefaults(Context context) {
        mIsCountDown = false; // default settings is for the chronometer to act as stopwatch
        mCountDownStartTime = -1;
        mCountUpStartTime = 0;

        // Make sure that the parent activity implements necessary interface methods.
        // This is to signal when the Chronometer finishes after being in countdown mode.
        try {
            mListener = (OnChronometerFinishedListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString() +
                    " must implement OnChronometerFinishedListener");
        }

        // This tick listener updates the time elapsed data member according to whether the
        // Chronometer is in countdown or countup mode. Necessary to keep track of 2 different modes
        setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long elapsed = SystemClock.elapsedRealtime() - chronometer.getBase();
                if (isCountDown()) {
                    elapsed = mCountDownStartTime - elapsed;
                    if (elapsed <= 0)
                        mListener.onChronometerFinished();
                } else {
                    elapsed += mCountUpStartTime;
                }

                setText(elapsed); // sets displayed time
            }
        });
    }

    /**
     * Sets the properly formatted time for the chronometer
     * @param time is the time in milliseconds to set the chronometer for
     */
    public void setText(long time) {
        if (time <= 0) time = 0;
        int hours = (int) time / 3600000;
        int minutes = (int) ((hours > 0) ? (time / 60000) % (60 * hours) : time / 60000);
        int seconds = (int) ((minutes > 0) ? (time / 1000) % (60 * minutes) : time / 1000);
        if (minutes == 0) // just seconds if < 1 minute
            setText(String.valueOf(seconds));
        else if (hours == 0)
            if (seconds > 9) // formats to MM:SS
                setText(String.format(Locale.getDefault(), "%d:%d", minutes, seconds));
            else // formats to MM:S (under 10 seconds so we need a leading zero)
                setText(String.format(Locale.getDefault(), "%d:0%d", minutes, seconds));

        // Similar formatting as with minutes and seconds if we need to show hours
        // Unlikely that anyone will let the counter go up to an hour, but just in case
        else if (seconds > 9 && minutes > 9)
            setText(String.format(Locale.getDefault(), "%d:%d:%d", hours, minutes, seconds));
        else if (seconds > 9)
            setText(String.format(Locale.getDefault(), "%d:0%d:%d", hours, minutes, seconds));
        else if (minutes > 9)
            setText(String.format(Locale.getDefault(), "%d:%d:0%d", hours, minutes, seconds));
        else
            setText(String.format(Locale.getDefault(), "%d:0%d:0%d", hours, minutes, seconds));
    }

    /**
     * Sets the chronometer to count down
     * @param countDown whether to count down or not
     */
    @Override
    public void setCountDown(boolean countDown) {
        mIsCountDown = countDown;
    }

    /**
     * Sets the time to start counting down from
     * @param startTime starting time in milliseconds
     */
    public void setCountDownStartTime(long startTime) {
        setCountDown(true);
        mCountDownStartTime = startTime;
    }

    /**
     * Sets a specific time to start counting up from
     * @param startTime time to start counting up from
     */
    public void setCountUpStartTime(long startTime) {
        setCountDown(false);
        mCountUpStartTime = startTime;
    }

    /**
     * Fetch how long the chronometer has been running for
     * @return the number of milliseconds since the chronometer was started
     */
    public long getTimeElapsed() {
        if (!isCountDown())
            return SystemClock.elapsedRealtime() - getBase() + mCountUpStartTime;
        return SystemClock.elapsedRealtime() - getBase();
    }
}
