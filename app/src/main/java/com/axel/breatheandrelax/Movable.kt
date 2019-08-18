package com.axel.breatheandrelax

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.support.constraint.ConstraintLayout
import android.util.TypedValue
import android.view.Display
import android.widget.Toast

/**
 * Abstract class that provides basic framework for all breathing
 * animations in the app. Current classes that extend from Movable are
 * SizeAnimation and BallAnimation.
 */

abstract class Movable
/**
 * Constructor initializes default values common to all breathing animations.
 * @param context is the context from the activity that displays the animation.
 */
internal constructor(protected var mContext: Context, var inhaleTime: Int, var exhaleTime: Int, var holdTime: Int, var pauseTime: Int) {

    // Data members
    private val mScreenWidth: Float
    private val mScreenHeight: Float
    private var mActionBarHeight: Float = 0.toFloat()
    private var mJustInhaled: Boolean = false
    var isPlaying: Boolean = false
        private set

    var inhaleColor: Int = 0
    var exhaleColor: Int = 0

    // Protected data members controlling the animation
    protected var mAnimation: ValueAnimator? = null
    protected val mScreen: ConstraintLayout = (mContext as Activity).findViewById(R.id.cl_main_layout)

    /**
     * Fetch the screen dimensions, which are different depending on screen orientation.
     * @return the number of pixels in the specified dimension.
     */
    protected val screenCenterX: Float
        get() = if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) mScreenWidth / 2f else mScreenHeight / 2f - mActionBarHeight
    protected val screenCenterY: Float
        get() = if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) mScreenHeight / 2f - mActionBarHeight else mScreenWidth / 2f

    // Abstract methods
    protected abstract fun breathe()

    protected abstract fun holdBreath()  // hold and pause between breaths
    protected abstract fun removeListeners()  // remove all active listeners of any kind

    init {
        // Fetch screen dimensions
        val display = (mContext as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        mScreenHeight = size.y.toFloat()
        mScreenWidth = size.x.toFloat()

        // Adjust action bar
        val tv = TypedValue()
        if (mContext.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true))
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, mContext.resources.displayMetrics).toFloat()
        mJustInhaled = false // we start by inhaling
        isPlaying = false

        inhaleColor = mContext.resources.getColor(R.color.breathe_red)
        exhaleColor = mContext.resources.getColor(R.color.breathe_blue)
    }

    open fun startBreathing() {
        isPlaying = true
        mJustInhaled = false // we start by inhaling
        breathe()
        Toast.makeText(mContext, mContext.resources.getString(R.string.toast_inhale), Toast.LENGTH_SHORT).show()
    }

    open fun stopBreathing() {
        if (mAnimation != null) {
            removeListeners()
            mAnimation!!.cancel()
            isPlaying = false
        }
    }

    protected fun justInhaled(): Boolean {
        return mJustInhaled
    }

    /**
     * Toggles whether the user just inhaled to keep track of the breathing cycle.
     */
    protected fun toggleInhale() {
        mJustInhaled = !mJustInhaled
    }
}
