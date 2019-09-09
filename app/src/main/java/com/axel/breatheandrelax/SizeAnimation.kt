package com.axel.breatheandrelax

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.ViewGroup
import android.widget.ImageView

/**
 * One of the breathing animations. Extends from Movable, which takes care
 * of most generic animation capabilities.
 */

class SizeAnimation
/**
 * Constructor initializes animation and UI
 * @param context activity context
 * @param inhaleTime time to inhale for
 * @param exhaleTime time to exhale for
 * @param holdTime time to pause after inhaling
 * @param pauseTime time to pause after exhaling
 */
(context: Context, inhaleTime: Int, exhaleTime: Int,
 holdTime: Int, pauseTime: Int) : Movable(context, inhaleTime, exhaleTime, holdTime, pauseTime) {

    // Animation-specific data members
    private val mBall: ImageView // object to animate
    private var mColorAnimation: ValueAnimator? = null // animation for color changes

    // UI data members
    private var mParams: ViewGroup.LayoutParams? = null
    private var mOrientation: Int = 0 // screen orientation

    init {
        // Create the animation
        mAnimation = ValueAnimator.ofInt(0, screenCenterX.toInt())

        // Create the object to animate
        mBall = ImageView(context)
        mBall.setImageDrawable(context.resources.getDrawable(R.drawable.ball))
    }

    /**
     * Override for Movable.startBreathing
     * Refreshes the UI
     */
    override fun startBreathing() {
        // Add the animated object to the screen
        val params = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Refresh the UI (in case this animation is not starting from scratch)
        mBall.drawable.setTint(inhaleColor) // reset color
        // verify object has a parent before removing it
        if (mBall.parent != null) {
            (mBall.parent as ViewGroup).removeView(mBall)

        }
        mScreen.addView(mBall, params)
        mParams = mBall.layoutParams

        // Determine screen orientation to properly position the animated object
        mOrientation = mContext.resources.configuration.orientation

        // The animation itself; we set it to change the size of the object dynamically
        mAnimation!!.addUpdateListener { animation ->
            val size = animation.animatedValue as Int
            mParams!!.width = size
            mParams!!.height = mParams!!.width
            mBall.layoutParams = mParams

            val ballHeight = mBall.height / 2
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mBall.x = screenCenterY - ballHeight
                mBall.y = screenCenterX - ballHeight
            } else {
                mBall.x = screenCenterX - ballHeight
                mBall.y = screenCenterY - ballHeight
            }
        }
        super.startBreathing() // Movable.startBreathing does the rest of the work
    }

    /**
     * Stops the animation and removes its elements from the UI
     */
    override fun stopBreathing() {
        super.stopBreathing()
        if (mColorAnimation != null)
            mColorAnimation!!.cancel()
        if (mAnimation != null) {
            mAnimation!!.removeAllListeners()
            mScreen.removeView(mBall)
        }
    }

    /**
     * Either inhale or exhale, performing the correct animation.
     */
    override fun breathe() {
        // Animate the item we just created
        if (justInhaled()) {
            mAnimation!!.duration = exhaleTime.toLong()
            mAnimation!!.reverse()
        } else {
            mAnimation!!.duration = inhaleTime.toLong()
            mAnimation!!.start()
        }

        // Mark the switch in state
        toggleInhale()

        // Wait for the animation to finish before continuing
        mAnimation!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                holdBreath()
                mAnimation!!.removeListener(this)
            }
        })
    }

    /**
     * Change the color of the animated object to signify the user holding their breath.
     */
    override fun holdBreath() {
        // Create the appropriate color animation (e.g. blue --> red vs red --> blue)
        if (justInhaled()) {
            mColorAnimation = ValueAnimator.ofArgb(inhaleColor, exhaleColor)
            mColorAnimation!!.duration = holdTime.toLong()
        } else {
            mColorAnimation = ValueAnimator.ofArgb(exhaleColor, inhaleColor)
            mColorAnimation!!.duration = pauseTime.toLong()
        }

        mColorAnimation!!.addUpdateListener { animation -> mBall.drawable.setTint(animation.animatedValue as Int) }

        // Wait for the animation to finish before breathing again
        mColorAnimation!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mColorAnimation!!.removeAllListeners()
                breathe()
            }
        })

        mColorAnimation!!.start()
    }

    override fun removeListeners() {
        // no need to do so
    }
}
