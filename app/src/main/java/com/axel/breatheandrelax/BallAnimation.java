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

public class BallAnimation extends Movable {

    // Data members
    private int mBallHeight;

    // View references
    private ImageView mBall;
    private ConstraintLayout mScreen;

    private ValueAnimator mColorAnimation;

    /**
     * Constructor creates new ImageView with the specified item.
     * @param context is the context within which to generate the new image.
     */
    public BallAnimation(Context context, final int inhaleTime, final int exhaleTime,
                         final int holdTime, final int pauseTime) {
        super(context, inhaleTime, exhaleTime, holdTime, pauseTime);
        mAnimation = ValueAnimator.ofFloat(0f, 1f); // arguments don't matter

        // Create the object that will be animated
        mScreen = ((Activity) context).findViewById(R.id.cl_main_layout);

        // Add the object to the screen
        mBall = new ImageView(context);
        mBall.setImageDrawable(context.getResources().getDrawable(R.drawable.ball));
    }


    /**
     * Override for Movable.startBreathing to get the layout dimensions before starting any animations
     */
    public void startBreathing() {
        // Reset the color
        mBall.getDrawable().setTint(getInhaleColor());
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (mBall.getParent() != null)
            ((ViewGroup) mBall.getParent()).removeView(mBall);
        mScreen.addView(mBall, params);

        // Get the height of the ImageView that depicts the ball as soon as it is drawn
        final ImageView ball = mBall;
        if (ball.getHeight() != 0) {
            super.startBreathing();
            return;
        }
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

        // Once we've finished moving, hold our breath
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

        float startAngle = 270f;
        if (mContext.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {
            startAngle = 180f;
            angle *= -1; // landscape switches animation direction
        }

        path.arcTo(getScreenCenterX() - arcRadius,
                getScreenCenterY() - arcRadius,
                getScreenCenterX() + arcRadius,
                getScreenCenterY() + arcRadius,
                startAngle, angle, true
        );

        // Update the animation periodically
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
