package com.axel.breatheandrelax;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.constraint.ConstraintLayout;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * One of the breathing animations. Extends from Movable, which takes care of most
 * generic animation capabilities. Notice that this class closely resembles SizeAnimation.
 * This is a side effect that arises from making Movable generic to work with all types
 * of animations, not just those that have
 */

public class BallAnimation extends Movable {

    // Animation-specific data members
    private ImageView mBall; // object to animate
    private int mBallHeight; // dimensions of the animated object
    private ValueAnimator mColorAnimation; // animation for color changes

    // UI data members
    private ConstraintLayout mScreen;

    /**
     * Constructor creates new ImageView with the specified item.
     * @param context is the context within which to generate the new image.
     * @param inhaleTime time to inhale for
     * @param exhaleTime time to exhale for
     * @param holdTime time to pause after inhaling
     * @param pauseTime time to pause after exhaling
     */
    public BallAnimation(Context context, final int inhaleTime, final int exhaleTime,
                         final int holdTime, final int pauseTime) {
        super(context, inhaleTime, exhaleTime, holdTime, pauseTime);

        mScreen = ((Activity) context).findViewById(R.id.cl_main_layout);

        // Create the animation
        mAnimation = ValueAnimator.ofFloat(0f, 1f);

        // Create the object to animate
        mBall = new ImageView(context);
        mBall.setImageDrawable(context.getResources().getDrawable(R.drawable.ball));
    }


    /**
     * Override for Movable.startBreathing
     * Fetches layout dimensions before starting any animations
     * Refreshes UI
     */
    public void startBreathing() {
        // Add animated object to the screen
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Refresh UI
        mBall.getDrawable().setTint(getInhaleColor()); // reset color
        if (mBall.getParent() != null)
            ((ViewGroup) mBall.getParent()).removeView(mBall);
        mScreen.addView(mBall, params);

        // Get the height of the ImageView that depicts the ball as soon as it is drawn
        final ImageView ball = mBall;
        if (ball.getHeight() != 0) // getHeight() may fail if the ImageView hasn't been drawn yet
        {
            super.startBreathing();
            return;
        }
        // If getHeight() fails, wait for the ImageView to be drawn before continuing, since
        // we will need getHeight() as soon as startBreathing() returns
        final ViewTreeObserver viewTreeObserver = ball.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBallHeight = ball.getHeight() / 2;
                ball.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                BallAnimation.super.startBreathing();
            }
        });
    }

    /**
     * Stops the breathing animation, deallocating any resources
     */
    public void stopBreathing() {
        super.stopBreathing();
        if (mColorAnimation != null)
            mColorAnimation.cancel();
        if (mAnimation != null) {
            mScreen.removeView(mBall);
        }
    }

    /**
     * Removes all listeners for animations.
     */
    protected void removeListeners() {
        if (mAnimation != null) {
            mAnimation.removeAllUpdateListeners();
            mAnimation.removeAllListeners();
        }
        if (mColorAnimation != null) {
            mColorAnimation.removeAllListeners();
            mColorAnimation.removeAllUpdateListeners();
        }
    }


    /**
     * Controls inhaling and exhaling animations.
     */
    protected void breathe() {
        // Animate the item we just created
        if (justInhaled()) move(getExhaleTime(), -359f); // counterclockwise
        else move(getInhaleTime(), 359f); // clockwise

        // Mark the switch in state
        toggleInhale();

        // Wait for animation to finish before holding the breath
        mAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimation.removeAllListeners();
                holdBreath();
            }
        });
    }

    /**
     * Controls holding the breath and changing animation colors.
     */
    protected void holdBreath() {
        // Create color animation
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

        // Wait for animation to finish before breathing again
        mColorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mColorAnimation.removeAllListeners();
                breathe();
            }
        });

        mColorAnimation.start();
    }

    /**
     * Move the current object in a circle
     * @param milliseconds duration of animation
     * @param angle through which to move the animation through
     */
    private void move(int milliseconds, float angle) {
        mAnimation.setDuration(milliseconds);

        // Generate path through which animation travels
        final Path path = new Path();
        float arcRadius = getScreenCenterX() / 2f;

        // Determine the starting angle based on screen orientation
        float startAngle = 270f;
        if (mContext.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {
            startAngle = 180f;
            angle *= -1; // landscape switches animation direction
        }

        // Generate the path through which to arc the object
        path.arcTo(getScreenCenterX() - arcRadius,
                getScreenCenterY() - arcRadius,
                getScreenCenterX() + arcRadius,
                getScreenCenterY() + arcRadius,
                startAngle, angle, true
        );

        // Update the animation periodically across the path
        mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator va) {
                float value = va.getAnimatedFraction();
                PathMeasure pathMeasure = new PathMeasure(path, true);

                // Get new position
                float[] point = new float[2];
                pathMeasure.getPosTan(pathMeasure.getLength() * value, point, null);

                // Set the new location of the ball, making sure to center it
                float xcor, ycor;
                if (mContext.getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_PORTRAIT) {
                    xcor = point[0] - mBallHeight;
                    ycor = point[1] - mBallHeight;
                } else {
                    xcor = point[1] - mBallHeight;
                    ycor = point[0] - mBallHeight;
                }

                mBall.setX(xcor);
                mBall.setY(ycor);
            }
        });

        mAnimation.start();
    }
}
