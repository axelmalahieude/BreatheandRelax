package com.axel.breatheandrelax;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.widget.Toast;

/**
 * Abstract class that provides basic framework for all breathing
 * animations in the app. Current classes that extend from Movable are
 * SizeAnimation and BallAnimation.
 */

public abstract class Movable {

    // Data members
    private float mScreenWidth;
    private float mScreenHeight;
    private float mActionBarHeight;

    private int mInhaleTime;
    private int mExhaleTime;
    private int mHoldTime;
    private int mPauseTime;
    private boolean mJustInhaled;
    private boolean mIsPlaying;

    private int mInhaleColor;
    private int mExhaleColor;

    // Protected data members controlling the animation
    protected ValueAnimator mAnimation;
    protected Context mContext;

    // Abstract methods
    protected abstract void breathe();
    protected abstract void holdBreath(); // hold and pause between breaths
    protected abstract void removeListeners(); // remove all active listeners of any kind

    /**
     * Constructor initializes default values common to all breathing animations.
     * @param context is the context from the activity that displays the animation.
     */
    Movable(Context context, int inhaleTime, int exhaleTime, int holdTime, int pauseTime) {
        // Fetch screen dimensions
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        mContext = context;
        Point size = new Point();
        display.getSize(size);
        mScreenHeight = size.y;
        mScreenWidth = size.x;

        // Adjust action bar
        TypedValue tv = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());

        // Set data members
        mInhaleTime = inhaleTime;
        mExhaleTime = exhaleTime;
        mHoldTime = holdTime;
        mPauseTime = pauseTime;
        mJustInhaled = false; // we start by inhaling
        mIsPlaying = false;

        mInhaleColor = mContext.getResources().getColor(R.color.breathe_red);
        mExhaleColor = mContext.getResources().getColor(R.color.breathe_blue);
    }

    /**
     * Starts the breathing animation.
     */
    public void startBreathing() {
        mIsPlaying = true;
        mJustInhaled = false; // we start by inhaling
        breathe();
        Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_inhale), Toast.LENGTH_SHORT).show();
    }

    /**
     * Stops the breathing animation, and deallocates any listeners.
     */
    public void stopBreathing() {
        if (mAnimation != null) {
            removeListeners();
            mAnimation.cancel();
            mIsPlaying = false;
        }
    }

    /**
     * Simple accessor functions
     * @return the data member specified
     */
    public int getInhaleColor() { return mInhaleColor; }
    public int getExhaleColor() { return mExhaleColor; }
    public int getInhaleTime() { return mInhaleTime; }
    public int getExhaleTime() { return mExhaleTime; }
    public int getHoldTime() { return mHoldTime; }
    public int getPauseTime() { return mPauseTime; }
    public boolean isPlaying() { return mIsPlaying; }
    protected boolean justInhaled() { return mJustInhaled; }

    /**
     * Fetch the screen dimensions, which are different depending on screen orientation.
     * @return the number of pixels in the specified dimension.
     */
    protected float getScreenCenterX() {
        if (mContext.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT)
            return mScreenWidth / 2f;
        return mScreenHeight / 2f - mActionBarHeight;
    }
    protected float getScreenCenterY() {
        if (mContext.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT)
            return mScreenHeight / 2f - mActionBarHeight;
        return mScreenWidth / 2f;
    }

    /**
     * Setter methods for breathing times and color.
     */
    public void setInhaleColor(int color) { mInhaleColor = color; }
    public void setExhaleColor(int color) { mExhaleColor = color; }
    public void setInhaleTime(int time) { mInhaleTime = time; }
    public void setExhaleTime(int time) { mExhaleTime = time; }
    public void setHoldTime(int time) { mHoldTime = time; }
    public void setPauseTime(int time) { mPauseTime = time; }

    /**
     * Toggles whether the user just inhaled to keep track of the breathing cycle.
     */
    protected void toggleInhale() { mJustInhaled = !mJustInhaled; }
}
