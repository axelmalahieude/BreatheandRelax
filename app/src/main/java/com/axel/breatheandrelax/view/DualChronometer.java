package com.axel.breatheandrelax.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

import java.util.Locale;

public class DualChronometer extends Chronometer {

    // Data members
    private boolean mIsCountDown;
    private long mCountDownStartTime; // time to start counting down from, in milliseconds
    private long mCountUpStartTime; // time to start counting up from, in milliseconds
    private OnChronometerFinishedListener mListener;

    /**
     * Interface to signal parent activity that the timer has concluded.
     */
    public interface OnChronometerFinishedListener {
        public void onChronometerFinished();
    }

    public DualChronometer(Context context) {
        super(context);
        setDefaults(context);
    }

    public DualChronometer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaults(context);
    }

    public DualChronometer (Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDefaults(context);
    }

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
        try {
            mListener = (OnChronometerFinishedListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString() +
                    " must implement OnChronometerFinishedListener");
        }
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

                setText(elapsed);
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
        if (minutes == 0)
            setText(String.valueOf(seconds));
        else if (hours == 0)
            if (seconds > 9)
                setText(String.format(Locale.getDefault(), "%d:%d", minutes, seconds));
            else
                setText(String.format(Locale.getDefault(), "%d:0%d", minutes, seconds));
        else if (seconds > 9 && minutes > 9)
            setText(String.format(Locale.getDefault(), "%d:%d:%d", hours, minutes, seconds));
        else if (seconds > 9)
            setText(String.format(Locale.getDefault(), "%d:0%d:%d", hours, minutes, seconds));
        else if (minutes > 9)
            setText(String.format(Locale.getDefault(), "%d:%d:0%d", hours, minutes, seconds));
        else
            setText(String.format(Locale.getDefault(), "%d:0%d:0%d", hours, minutes, seconds));
    }

    @Override
    public void setCountDown(boolean countDown) {
        mIsCountDown = countDown;
    }

    public void setCountDownStartTime(long startTime) {
        setCountDown(true);
        mCountDownStartTime = startTime;
    }

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
