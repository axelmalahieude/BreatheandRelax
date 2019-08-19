package com.axel.breatheandrelax.view

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.Chronometer

import java.util.Locale

/**
 * Extends the Chronometer to count both up and down according to how it is
 * programmatically set. This allows both timer and stopwatch modes without
 * using two different views.
 */

class DualChronometer : Chronometer {

    // Data members
    private var mIsCountDown: Boolean = false
    private var mCountDownStartTime: Long = 0 // time to start counting down from, in milliseconds
    private var mCountUpStartTime: Long = 0 // time to start counting up from, in milliseconds
    private var mListener: OnChronometerFinishedListener? = null

    /**
     * Fetch how long the chronometer has been running for
     * @return the number of milliseconds since the chronometer was started
     */
    val timeElapsed: Long
        get() = if (!isCountDown) SystemClock.elapsedRealtime() - base + mCountUpStartTime else SystemClock.elapsedRealtime() - base

    /**
     * Interface to signal parent activity that the timer has concluded. The parent activity
     * must implement this method.
     */
    interface OnChronometerFinishedListener {
        fun onChronometerFinished()
    }

    /**
     * Necessary constructor to override the Chronometer view
     * @param context the parent activity's context
     */
    constructor(context: Context) : super(context) {
        setDefaults(context)
    }

    /**
     * Necessary constructor to override the Chronometer view
     * @param context the parent activity's context
     * @param attrs the XML attributes of the view
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setDefaults(context)
    }

    /**
     * Necessary constructor to override the Chronometer view
     * @param context the parent activity's context
     * @param attrs the XML attributes of the view
     * @param defStyleAttr the style for the XML attributes
     */
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setDefaults(context)
    }

    /**
     * Whether the Chronometer is in countdown mode or not.
     * @return
     */
    override fun isCountDown(): Boolean {
        return mIsCountDown
    }

    /**
     * Sets default values for the chronometer. Called from each constructor.
     * @param context is the context of the chronometer's parent activity
     */
    private fun setDefaults(context: Context) {
        mIsCountDown = false // default settings is for the chronometer to act as stopwatch
        mCountDownStartTime = -1
        mCountUpStartTime = 0

        // Make sure that the parent activity implements necessary interface methods.
        // This is to signal when the Chronometer finishes after being in countdown mode.
        try {
            mListener = context as OnChronometerFinishedListener
        } catch (cce: ClassCastException) {
            throw ClassCastException("$context must implement OnChronometerFinishedListener")
        }

        // This tick listener updates the time elapsed data member according to whether the
        // Chronometer is in countdown or countup mode. Necessary to keep track of 2 different modes
        onChronometerTickListener = OnChronometerTickListener { chronometer ->
            var elapsed = SystemClock.elapsedRealtime() - chronometer.base
            if (isCountDown) {
                elapsed = mCountDownStartTime - elapsed
                if (elapsed <= 0)
                    mListener!!.onChronometerFinished()
            } else {
                elapsed += mCountUpStartTime
            }

            setText(elapsed) // sets displayed time
        }
    }

    /**
     * Sets the properly formatted time for the chronometer
     * @param time is the time in milliseconds to set the chronometer for
     */
    private fun setText(time: Long) {
        var time = time
        if (time <= 0) time = 0
        val hours = time.toInt() / 3600000
        val minutes = (if (hours > 0) time / 60000 % (60 * hours) else time / 60000).toInt()
        val seconds = (if (minutes > 0) time / 1000 % (60 * minutes) else time / 1000).toInt()
        if (minutes == 0)
        // just seconds if < 1 minute
            text = seconds.toString()
        else if (hours == 0)
            if (seconds > 9)
            // formats to MM:SS
                text = String.format(Locale.getDefault(), "%d:%d", minutes, seconds)
            else
            // formats to MM:S (under 10 seconds so we need a leading zero)
                text = String.format(Locale.getDefault(), "%d:0%d", minutes, seconds)
        else if (seconds > 9 && minutes > 9)
            text = String.format(Locale.getDefault(), "%d:%d:%d", hours, minutes, seconds)
        else if (seconds > 9)
            text = String.format(Locale.getDefault(), "%d:0%d:%d", hours, minutes, seconds)
        else if (minutes > 9)
            text = String.format(Locale.getDefault(), "%d:%d:0%d", hours, minutes, seconds)
        else
            text = String.format(Locale.getDefault(), "%d:0%d:0%d", hours, minutes, seconds)
    }

    /**
     * Sets the chronometer to count down
     * @param countDown whether to count down or not
     */
    override fun setCountDown(countDown: Boolean) {
        mIsCountDown = countDown
    }

    /**
     * Sets the time to start counting down from
     * @param startTime starting time in milliseconds
     */
    fun setCountDownStartTime(startTime: Long) {
        isCountDown = true
        mCountDownStartTime = startTime
    }

    /**
     * Sets a specific time to start counting up from
     * @param startTime time to start counting up from
     */
    fun setCountUpStartTime(startTime: Long) {
        isCountDown = false
        mCountUpStartTime = startTime
    }
}
