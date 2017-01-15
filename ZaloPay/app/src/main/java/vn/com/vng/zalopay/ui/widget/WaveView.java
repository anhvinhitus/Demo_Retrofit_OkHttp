package vn.com.vng.zalopay.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by AnhHieu on 6/12/16.
 */
public class WaveView extends FrameLayout {

    private static final int DEFAULT_RIPPLE_COUNT = 6;
    private static final int DEFAULT_DURATION_TIME = 3000;
    private static final float DEFAULT_SCALE = 6.0f;
    private static final int DEFAULT_FILL_TYPE = 0;

    private int rippleColor;
    private float rippleStrokeWidth;
    private float rippleRadius;
    private int rippleDurationTime;
    private int rippleAmount;
    private int rippleDelay;
    private float rippleScale;
    private int rippleType;

    private Paint paint;
    private boolean animationRunning = false;

    private AnimatorSet animatorSet;

    private ArrayList<RippleView> rippleViewList = new ArrayList<>();

    public WaveView(Context context) {
        super(context);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode())
            return;

        if (null == attrs) {
            throw new IllegalArgumentException("Attributes should be provided to this view,");
        }

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        rippleColor = typedArray.getColor(R.styleable.WaveView_rb_color, ContextCompat.getColor(getContext(), R.color.rippelColor));
        rippleStrokeWidth = typedArray.getDimension(R.styleable.WaveView_rb_strokeWidth, getResources().getDimension(R.dimen.rippleStrokeWidth));
        rippleRadius = typedArray.getDimension(R.styleable.WaveView_rb_radius, getResources().getDimension(R.dimen.rippleRadius));
        rippleDurationTime = typedArray.getInt(R.styleable.WaveView_rb_duration, DEFAULT_DURATION_TIME);
        rippleAmount = typedArray.getInt(R.styleable.WaveView_rb_rippleAmount, DEFAULT_RIPPLE_COUNT);
        rippleScale = typedArray.getFloat(R.styleable.WaveView_rb_scale, DEFAULT_SCALE);
        rippleType = typedArray.getInt(R.styleable.WaveView_rb_type, DEFAULT_FILL_TYPE);
        typedArray.recycle();

        rippleDelay = rippleDurationTime / rippleAmount;

        paint = new Paint();
        paint.setAntiAlias(true);
        if (rippleType == DEFAULT_FILL_TYPE) {
            rippleStrokeWidth = 0;
            paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setStyle(Paint.Style.STROKE);
        }
        paint.setColor(rippleColor);

        FrameLayout.LayoutParams rippleParams = new FrameLayout.LayoutParams((int) (2 * (rippleRadius + rippleStrokeWidth)), (int) (2 * (rippleRadius + rippleStrokeWidth)));
        //rippleParams.addRule(CENTER_IN_PARENT, TRUE);
        rippleParams.bottomMargin = AndroidUtils.dp(42);
        rippleParams.gravity = Gravity.CENTER;

        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        List<Animator> animatorList = new ArrayList<>();

        for (int i = 0; i < rippleAmount; i++) {
            final RippleView rippleView = new RippleView(getContext());
            addView(rippleView, rippleParams);
            rippleView.setVisibility(GONE);
            rippleViewList.add(rippleView);

            int duration = rippleDurationTime;
            int timeDelay = i * rippleDelay * 2;

            final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale);
            scaleXAnimator.setStartDelay(timeDelay);
            scaleXAnimator.setDuration(duration);
            scaleXAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    rippleView.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    rippleView.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

            animatorList.add(scaleXAnimator);

            final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale);
            scaleYAnimator.setStartDelay(timeDelay);
            scaleYAnimator.setDuration(duration);
            animatorList.add(scaleYAnimator);

            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0.0f);
            alphaAnimator.setStartDelay(timeDelay);
            alphaAnimator.setDuration(duration);
            animatorList.add(alphaAnimator);
        }

        animatorSet.playTogether(animatorList);
    }


    private class AnimatorRepeat implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
            Timber.d("onAnimationStart");
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            Timber.d("onAnimationEnd");
            animatorSet.setStartDelay(2000);
            animatorSet.start();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    private class RippleView extends View {

        public RippleView(Context context) {
            super(context);
            this.setVisibility(View.INVISIBLE);
            //  setBackgroundResource(R.drawable.circle_border_white);
        }


        @Override
        protected void onDraw(Canvas canvas) {
            int radius = (Math.min(getWidth(), getHeight())) / 2;
            canvas.drawCircle(radius, radius, radius - rippleStrokeWidth, paint);
        }
    }

    public void startRippleAnimation() {
     /*   if (!isRippleAnimationRunning()) {
          *//*  for (RippleView rippleView : rippleViewList) {
                rippleView.setVisibility(VISIBLE);
            }*//*
            animatorSet.addListener(new AnimatorRepeat());
            animatorSet.start();
            animationRunning = true;
        }*/

        Timber.d("startRippleAnimation");

        if (!animatorSet.isRunning()) {
            animatorSet.addListener(new AnimatorRepeat());
            animatorSet.start();
        }

        animationRunning = true;
    }

    public void stopRippleAnimation() {
        for (RippleView rippleView : rippleViewList) {
            rippleView.clearAnimation();
        }

        animatorSet.removeAllListeners();

        if (animatorSet.isRunning()) {
            animatorSet.end();
        }

        ArrayList<Animator> mAnimators = animatorSet.getChildAnimations();
        for (Animator mAnimator : mAnimators) {
            mAnimator.cancel();
            mAnimator.removeAllListeners();
        }

        animationRunning = false;
    }

    public boolean isRippleAnimationRunning() {
        return animationRunning;
    }
}
