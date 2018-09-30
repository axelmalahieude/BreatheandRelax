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

public class SizeAnimation extends Movable {

    private ImageView mBall;
    private ConstraintLayout mScreen;
    private ViewGroup.LayoutParams mParams;
    private ValueAnimator mColorAnimation;
    private int mOrientation;

    public SizeAnimation(Context context, final int inhaleTime, final int exhaleTime,
                         final int holdTime, final int pauseTime) {
        super(context, inhaleTime, exhaleTime, holdTime, pauseTime);
        mAnimation = ValueAnimator.ofInt(0, (int) getScreenCenterX());

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

        // Add the animated object to the screen
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (mBall.getParent() != null) // verify no parent is already assigned
            ((ViewGroup) mBall.getParent()).removeView(mBall);
        mScreen.addView(mBall, params);
        mParams = mBall.getLayoutParams();

        // Determine screen orientation to properly position the animated object
        mOrientation = mContext.getResources().getConfiguration().orientation;

        // The animation itself; an update listener that changes the size of the view accordingly
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
        super.startBreathing();
    }

    public void stopBreathing() {
        super.stopBreathing();
        if (mColorAnimation != null)
            mColorAnimation.cancel();
        if (mAnimation != null) {
            mAnimation.removeAllListeners();
            mScreen.removeView(mBall);
        }
    }

    @Override
    protected void breathe() {
        // Animate the item we just created
        if (justInhaled()) shrinkAnimation(getExhaleTime());
        else growAnimation(getInhaleTime());

        // Mark the switch in state
        toggleInhale();

        mAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holdBreath();
                mAnimation.removeListener(this);
            }
        });
    }

    @Override
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

    @Override
    protected void removeListeners() {
        // no need to do so
    }

    private void growAnimation(long time) {
        mAnimation.setDuration(time);
        mAnimation.start();
    }

    private void shrinkAnimation(long time) {
        mAnimation.setDuration(time);
        mAnimation.reverse();
    }
}
