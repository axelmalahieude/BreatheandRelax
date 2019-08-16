package com.axel.breatheandrelax

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Path
import android.graphics.PathMeasure
import android.support.constraint.ConstraintLayout
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView

/**
 * One of the breathing animations. Extends from Movable, which takes care of most
 * generic animation capabilities. Notice that this class closely resembles SizeAnimation.
 * This is a side effect that arises from making Movable generic to work with all types
 * of animations, not just those that have
 */

class BallAnimation
/**
 * Constructor creates new ImageView with the specified item.
 * @param context is the context within which to generate the new image.
 * @param inhaleTime time to inhale for
 * @param exhaleTime time to exhale for
 * @param holdTime time to pause after inhaling
 * @param pauseTime time to pause after exhaling
 */
(context: Context, inhaleTime: Int, exhaleTime: Int,
 holdTime: Int, pauseTime: Int) : Movable(context, inhaleTime, exhaleTime, holdTime, pauseTime) {

    // Animation-specific data members
    private val mBall: ImageView // object to animate
    private var mBallHeight: Int = 0 // dimensions of the animated object
    private var mColorAnimation: ValueAnimator? = null // animation for color changes

    // UI data members
    private val mScreen: ConstraintLayout

    init {

        mScreen = (context as Activity).findViewById(R.id.cl_main_layout)

        // Create the animation
        mAnimation = ValueAnimator.ofFloat(0f, 1f)

        // Create the object to animate
        mBall = ImageView(context)
        mBall.setImageDrawable(context.getResources().getDrawable(R.drawable.ball))
    }


    /**
     * Override for Movable.startBreathing
     * Fetches layout dimensions before starting any animations
     * Refreshes UI
     */
    override fun startBreathing() {
        // Add animated object to the screen
        val params = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Refresh UI
        mBall.drawable.setTint(inhaleColor) // reset color
        if (mBall.parent != null)
            (mBall.parent as ViewGroup).removeView(mBall)
        mScreen.addView(mBall, params)

        // Get the height of the ImageView that depicts the ball as soon as it is drawn
        val ball = mBall
        if (ball.height != 0)
        // getHeight() may fail if the ImageView hasn't been drawn yet
        {
            super.startBreathing()
            return
        }
        // If getHeight() fails, wait for the ImageView to be drawn before continuing, since
        // we will need getHeight() as soon as startBreathing() returns
        val viewTreeObserver = ball.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mBallHeight = ball.height / 2
                ball.viewTreeObserver.removeOnGlobalLayoutListener(this)
                super@BallAnimation.startBreathing()
            }
        })
    }

    /**
     * Stops the breathing animation, deallocating any resources
     */
    override fun stopBreathing() {
        super.stopBreathing()
        if (mColorAnimation != null)
            mColorAnimation!!.cancel()
        if (mAnimation != null) {
            mScreen.removeView(mBall)
        }
    }

    /**
     * Removes all listeners for animations.
     */
    override fun removeListeners() {
        if (mAnimation != null) {
            mAnimation!!.removeAllUpdateListeners()
            mAnimation!!.removeAllListeners()
        }
        if (mColorAnimation != null) {
            mColorAnimation!!.removeAllListeners()
            mColorAnimation!!.removeAllUpdateListeners()
        }
    }


    /**
     * Controls inhaling and exhaling animations.
     */
    override fun breathe() {
        // Animate the item we just created
        if (justInhaled())
            move(exhaleTime, -359f) // counterclockwise
        else
            move(inhaleTime, 359f) // clockwise

        // Mark the switch in state
        toggleInhale()

        // Wait for animation to finish before holding the breath
        mAnimation!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mAnimation!!.removeAllListeners()
                holdBreath()
            }
        })
    }

    /**
     * Controls holding the breath and changing animation colors.
     */
    override fun holdBreath() {
        // Create color animation
        if (justInhaled()) {
            mColorAnimation = ValueAnimator.ofArgb(inhaleColor, exhaleColor)
            mColorAnimation!!.duration = holdTime.toLong()
        } else {
            mColorAnimation = ValueAnimator.ofArgb(exhaleColor, inhaleColor)
            mColorAnimation!!.duration = pauseTime.toLong()
        }

        mColorAnimation!!.addUpdateListener { animation -> mBall.drawable.setTint(animation.animatedValue as Int) }

        // Wait for animation to finish before breathing again
        mColorAnimation!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mColorAnimation!!.removeAllListeners()
                breathe()
            }
        })

        mColorAnimation!!.start()
    }

    /**
     * Move the current object in a circle
     * @param milliseconds duration of animation
     * @param angle through which to move the animation through
     */
    private fun move(milliseconds: Int, angle: Float) {
        var angle = angle
        mAnimation!!.duration = milliseconds.toLong()

        // Generate path through which animation travels
        val path = Path()
        val arcRadius = screenCenterX / 2f

        // Determine the starting angle based on screen orientation
        var startAngle = 270f
        if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            startAngle = 180f
            angle *= -1f // landscape switches animation direction
        }

        // Generate the path through which to arc the object
        path.arcTo(screenCenterX - arcRadius,
                screenCenterY - arcRadius,
                screenCenterX + arcRadius,
                screenCenterY + arcRadius,
                startAngle, angle, true
        )

        // Update the animation periodically across the path
        mAnimation!!.addUpdateListener { va ->
            val value = va.animatedFraction
            val pathMeasure = PathMeasure(path, true)

            // Get new position
            val point = FloatArray(2)
            pathMeasure.getPosTan(pathMeasure.length * value, point, null)

            // Set the new location of the ball, making sure to center it
            val xcor: Float
            val ycor: Float
            if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                xcor = point[0] - mBallHeight
                ycor = point[1] - mBallHeight
            } else {
                xcor = point[1] - mBallHeight
                ycor = point[0] - mBallHeight
            }

            mBall.x = xcor
            mBall.y = ycor
        }

        mAnimation!!.start()
    }
}
