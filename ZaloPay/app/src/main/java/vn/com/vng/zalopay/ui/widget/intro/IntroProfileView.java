package vn.com.vng.zalopay.ui.widget.intro;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AnhHieu on 8/30/16.
 * *
 */

public class IntroProfileView extends RelativeLayout {

    public static int DEFAULT_MASK_COLOR = 1879048192;
    public static long DEFAULT_DELAY_MILLIS = 0L;
    public static long DEFAULT_FADE_DURATION = 700L;
    public static int DEFAULT_TARGET_PADDING = 10;

    SharedPreferences mPref;
    private int maskColor;
    private long delayMillis;
    private boolean isReady;
    private boolean isFadeAnimationEnabled;
    private long fadeAnimationDuration;
    private String mIntroId = "profileId";
    private Handler handler;
    private int width;
    private int height;

    private Paint eraser;

    private Bitmap bitmap;
    private Canvas canvas;

    private List<ShapeEraser> mErasers;

    public IntroProfileView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mErasers = new ArrayList<>();
        this.handler = new Handler();
        this.setWillNotDraw(false);
        this.setVisibility(INVISIBLE);
        this.maskColor = DEFAULT_MASK_COLOR;
        this.delayMillis = DEFAULT_DELAY_MILLIS;
        this.fadeAnimationDuration = DEFAULT_FADE_DURATION;

        this.eraser = new Paint();
        this.eraser.setColor(-1);
        this.eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.eraser.setFlags(1);
    }

    @TargetApi(16)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.width = this.getMeasuredWidth();
        this.height = this.getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                dismiss();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }


    public void addShape(ShapeEraser shapeEraser) {
        if (isReady) {
            return;
        }
        mErasers.add(shapeEraser);
    }

    public void show(Activity activity) {
        if (!isDisplayed(mIntroId)) {
            ((ViewGroup) activity.getWindow().getDecorView()).addView(this);
            this.setReady(true);
            this.handler.postDelayed(new Runnable() {
                public void run() {
                    if (IntroProfileView.this.isFadeAnimationEnabled) {
                        animateFadeIn(IntroProfileView.this, IntroProfileView.this.fadeAnimationDuration, new DefaultAnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                IntroProfileView.this.setVisibility(VISIBLE);
                            }
                        });
                    } else {
                        IntroProfileView.this.setVisibility(VISIBLE);
                    }
                }
            }, this.delayMillis);
        }
    }

    void setReady(boolean ready) {
        this.isReady = ready;
    }

    public void dismiss() {
        this.setDisplayed(mIntroId);
        animateFadeOut(this, this.fadeAnimationDuration, new DefaultAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                IntroProfileView.this.setVisibility(GONE);
                IntroProfileView.this.removeMaterialView();
            }
        });
    }

    private void removeMaterialView() {
        if (this.getParent() != null) {
            ((ViewGroup) this.getParent()).removeView(this);
        }
    }

    boolean isDisplayed(String id) {
        return mPref.contains(id);
    }

    void setDisplayed(String id) {
        mPref.edit().putBoolean(id, true).apply();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.isReady) {
            if (this.bitmap == null || canvas == null) {
                if (this.bitmap != null) {
                    this.bitmap.recycle();
                }

                this.bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
                this.canvas = new Canvas(this.bitmap);
            }

            this.canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            this.canvas.drawColor(this.maskColor);

            this.eraser.setColor(-1);
            this.eraser.setStyle(Paint.Style.FILL);

            for (ShapeEraser eraser : mErasers) {
                eraser.draw(this.canvas, this.eraser);
            }

            if (canvas != null) {
                canvas.drawBitmap(this.bitmap, 0.0F, 0.0F, null);
            }
        }
    }

    /************************************************************/

    private void animateFadeIn(View view, long duration, DefaultAnimatorListener listener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", new float[]{0.0F, 1.0F});
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(listener);
        objectAnimator.start();
    }

    private void animateFadeOut(View view, long duration, DefaultAnimatorListener listener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", new float[]{1.0F, 0.0F});
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(listener);
        objectAnimator.start();
    }


}
