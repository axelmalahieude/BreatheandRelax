package com.axel.breatheandrelax;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.constraint.ConstraintLayout;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * One of the breathing animations. Extends from Movable, which takes care
 * of most generic animation capabilities.
 */

public class SizeAnimation extends Movable {

    // Animation-specific data members
    private ImageView mBall; // object to animate
    private ValueAnimator mColorAnimation; // animation for color changes

    // UI data members
    private ConstraintLayout mScreen;
    private ViewGroup.LayoutParams mParams;
    private int mOrientation; // screen orientation

    /**
     * Constructor initializes animation and UI
     * @param context activity context
     * @param inhaleTime time to inhale for
     * @param exhaleTime time to exhale for
     * @param holdTime time to pause after inhaling
     * @param pauseTime time to pause after exhaling
     */
    public SizeAnimation(Context context, final int inhaleTime, final int exhaleTime,
                         final int holdTime, final int pauseTime) {
        super(context, inhaleTime, exhaleTime, holdTime, pauseTime);

        mScreen = ((Activity) context).findViewById(R.id.cl_main_layout);

        // Create the animation
        mAnimation = ValueAnimator.ofInt(0, (int) getScreenCenterX());

        // Create the object to animate
        mBall = new ImageView(context);
        mBall.setImageDrawable(context.getResources().getDrawable(R.drawable.ball));
    }

    /**
     * Override for Movable.startBreathing
     * Refreshes the UI
     */
    public void startBreathing() {
        // Add the animated object to the screen
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Refresh the UI (in case this animation is not starting from scratch)
        mBall.getDrawable().setTint(getInhaleColor()); // reset color
        if (mBall.getParent() != null) // verify object has a parent before removing it
            ((ViewGroup) mBall.getParent()).removeView(mBall);
        mScreen.addView(mBall, params);
        mParams = mBall.getLayoutParams();

        // Determine screen orientation to properly position the animated object
        mOrientation = mContext.getResources().getConfiguration().orientation;

        // The animation itself; we set it to change the size of the object dynamically
        mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int size = (int) animation.getAnimatedValue();
                mParams.height = mParams.width = size;
                mBall.setLayoutParams(mParams);

                int ballHeight = mBall.getHeight() / 2;
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mBall.setX(getScreenCenterY() - ballHeight);
                    mBall.setY(getScreenCenterX() - ballHeight);
                } else {
                    mBall.setX(getScreenCenterX() - ballHeight);
                    mBall.setY(getScreenCenterY() - ballHeight);
                }
            }
        });
        super.startBreathing(); // Movable.startBreathing does the rest of the work
    }

    /**
     * Stops the animation and removes its elements from the UI
     */
    public void stopBreathing() {
        super.stopBreathing();
        if (mColorAnimation != null)
            mColorAnimation.cancel();
        if (mAnimation != null) {
            mAnimation.removeAllListeners();
            mScreen.removeView(mBall);
        }
    }

    /**
     * Either inhale or exhale, performing the correct animation.
     */
    @Override
    protected void breathe() {
        // Animate the item we just created
        if (justInhaled()) {
            mAnimation.setDuration(getExhaleTime());
            mAnimation.reverse();
        } else {
            mAnimation.setDuration(getInhaleTime());
            mAnimation.start();
        }

        // Mark the switch in state
        toggleInhale();

        // Wait for the animation to finish before continuing
        mAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holdBreath();
                mAnimation.removeListener(this);
            }
        });
    }

    /**
     * Change the color of the animated object to signify the user holding their breath.
     */
    @Override
    protected void holdBreath() {
        // Create the appropriate color animation (e.g. blue --> red vs red --> blue)
        if (justInhaled()) {
            mColorAnimation = ValueAnimator.ofArgb(getInhaleColor(), getExhaleColor());
            mColorAnimation.setDuration(getHoldTime());
        } else {
            mColorAnimation = ValueAnimator.ofArgb(getExhaleColor(), getInhaleColor());
            mColorAnimation.setDuration(getPauseTime());
        }

        mColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBall.getDrawable().setTint((int) animation.getAnimatedValue());
            }
        });

        // Wait for the animation to finish before breathing again
        mColorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mColorAnimation.removeAllListeners();
                breathe();
            }
        });

        mColorAnimation.start();
    }

    @Override
    protected void removeListeners() {
        // no need to do so
    }
}
