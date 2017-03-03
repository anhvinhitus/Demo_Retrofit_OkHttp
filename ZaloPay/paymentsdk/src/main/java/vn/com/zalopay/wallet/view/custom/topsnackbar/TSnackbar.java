package vn.com.zalopay.wallet.view.custom.topsnackbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.listener.ZPWOnCloseCardPopupListener;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.view.adapter.CardSupportAdapter;
import vn.com.zalopay.wallet.view.adapter.LinkCardBankGridViewAdapter;


public final class TSnackbar {
    /**
     * Show the TSnackbar indefinitely. This means that the TSnackbar will be displayed from the time
     * that is {@link #show() shown} until either it is dismissed, or another TSnackbar is shown.
     *
     * @see #setDuration
     */
    public static final int LENGTH_INDEFINITE = -2;
    /**
     * Show the TSnackbar for a short period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_SHORT = -1;
    /**
     * Show the TSnackbar for a long period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_LONG = 0;
    private static final int ANIMATION_DURATION = 250;
    private static final int ANIMATION_FADE_DURATION = 180;
    private static final Handler sHandler;
    private static final int MSG_SHOW = 0;
    private static final int MSG_DISMISS = 1;

    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((TSnackbar) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((TSnackbar) message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    private final ViewGroup mParent;
    private final Context mContext;
    private final SnackbarLayout mView;
    private final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, TSnackbar.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, TSnackbar.this));
        }
    };
    private int mDuration;
    private Callback mCallback;
    private onCloseSnackBar mOnCloseSnackBar;
    private ZPWOnCloseCardPopupListener mCloseCardPopupListener;
    private TSnackbar(ViewGroup parent) {
        mParent = parent;
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mView = (SnackbarLayout) inflater.inflate(R.layout.tsnackbar_layout, mParent, false);
    }

    /**
     * Make a TSnackbar to display a message
     * <p/>
     * <p>TSnackbar will try and find a parent view to hold TSnackbar's view from the value given
     * to {@code view}. TSnackbar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p/>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows TSnackbar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static TSnackbar make(@NonNull View view, @NonNull CharSequence text, @Duration int duration, ZPWOnCloseCardPopupListener pListener) {
        TSnackbar snackbar = new TSnackbar(findSuitableParent(view));
        snackbar.setText(text);
        snackbar.setDuration(duration);

        snackbar.mCloseCardPopupListener = pListener;
        snackbar.setClosePopupListener();
        return snackbar;
    }

    @NonNull
    public static TSnackbar makeMessageBar(@NonNull View view, @NonNull CharSequence title, @NonNull CharSequence text, CharSequence actionText, @Duration int duration, onCloseSnackBar pListener) {
        TSnackbar snackbar = new TSnackbar(findSuitableParent(view));
        snackbar.setTitle(title);
        snackbar.setMessageText(text);
        snackbar.setActionMessageText(actionText);
        snackbar.makeBankSupportSnackBarGone();
        snackbar.setDuration(duration);
        snackbar.setCloseListener(pListener);

        return snackbar;
    }

    /**
     * Make a TSnackbar to display a message.
     * <p/>
     * <p>TSnackbar will try and find a parent view to hold TSnackbar's view from the value given
     * to {@code view}. TSnackbar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p/>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows TSnackbar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param resId    The resource id of the string resource to use. Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static TSnackbar make(@NonNull View view, @StringRes int resId, @Duration int duration, ZPWOnCloseCardPopupListener pListener) {
        return make(view, view.getResources().getText(resId), duration, pListener);
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
    }

    public boolean isCardSupportPopup() {
        GridView gridView = mView.getGridViewBankList();

        if (gridView != null && gridView.getVisibility() == View.VISIBLE)
            return true;

        return false;

    }

    public void setBankListAdapter(final CardSupportAdapter pBankListAdapter) {
        final GridView gridView = mView.getGridViewBankList();

        if (gridView != null) {
            if (pBankListAdapter instanceof LinkCardBankGridViewAdapter)
                setText(Html.fromHtml(GlobalData.getStringResource(RS.string.zpw_string_title_select_card)));

            if (pBankListAdapter.getCount() <= 3)
                gridView.setNumColumns(pBankListAdapter.getCount());

            gridView.setAdapter(pBankListAdapter);

            mView.getGridViewBankList().setVisibility(View.VISIBLE);
        }
    }

    /**
     * size should be around 200....
     */
    public TSnackbar addIcon(int resource_id, int size) {
        final TextView tv = mView.getMessageView();

        tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(Bitmap.createScaledBitmap(((BitmapDrawable) (mContext.getResources().getDrawable(resource_id))).getBitmap(), size, size, true)), null, null, null);

        return this;
    }

    /**
     * Update the text in this {@link TSnackbar}.
     *
     * @param message The new text for the Toast.
     */
    @NonNull
    public TSnackbar setMessageText(@NonNull CharSequence message) {
        final TextView tv = mView.getErrorMessageView();

        if (tv != null) {
            RelativeLayout viewWrapper = mView.getErrorMessageWrapper();

            if (viewWrapper != null)
                viewWrapper.setVisibility(View.VISIBLE);

            tv.setText(message);
        }

        return this;
    }

    /**
     * Update the text in this {@link TSnackbar}.
     *
     * @param title The new text for the Toast.
     */
    @NonNull
    public TSnackbar setTitle(CharSequence title) {
        final TextView tv = mView.getTitleView();
        if (tv != null) {
            if (title == null) {
                tv.setVisibility(View.GONE);
                return this;
            }

            tv.setVisibility(View.VISIBLE);
            RelativeLayout viewWrapper = mView.getErrorMessageWrapper();

            if (viewWrapper != null) {
                viewWrapper.setVisibility(View.VISIBLE);
                viewWrapper.setGravity(Gravity.LEFT);
            }

            tv.setText(title);
        }

        return this;
    }

    public TSnackbar setActionMessageText(@NonNull CharSequence message) {
        final TextView tv = mView.getActionMessageView();

        if (tv != null) {
            if (!TextUtils.isEmpty(message))
                tv.setText(message);
            else
                tv.setVisibility(View.GONE);
        }

        return this;
    }

    public void makeBankSupportSnackBarGone() {
        mView.getMessageView().setVisibility(View.GONE);
        mView.getGridViewBankList().setVisibility(View.GONE);
        mView.getViewLine().setVisibility(View.GONE);
    }

    public void setClosePopupListener() {
        View rootView = mView.getSnackBarRootView();

        if (rootView != null)
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    dismiss();

                    if (mCloseCardPopupListener != null)
                        mCloseCardPopupListener.onCloseCardPopup();

                    return false;
                }
            });

        GridView gridView = mView.getGridViewBankList();

        if (gridView != null && gridView.getVisibility() == View.VISIBLE)
            gridView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    dismiss();

                    if (mCloseCardPopupListener != null)
                        mCloseCardPopupListener.onCloseCardPopup();

                    return false;
                }
            });
    }

    public void setCloseListener(onCloseSnackBar pListener) {
        /*
        View rootView = mView.getSnackBarRootView();

        if(rootView != null)
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    dismiss();

                    if(mOnCloseSnackBar != null)
                        mOnCloseSnackBar.onClose();

                    return false;
                }
            });
            */

        mOnCloseSnackBar = pListener;

        TextView textViewAction = mView.getActionMessageView();

        if (textViewAction != null) {
            textViewAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();

                    if (mOnCloseSnackBar != null)
                        mOnCloseSnackBar.onClose();
                }
            });
        }
    }

    /**
     * Update the text in this {@link TSnackbar}.
     *
     * @param message The new text for the Toast.
     */
    @NonNull
    public TSnackbar setText(@NonNull CharSequence message) {
        final TextView tv = mView.getMessageView();
        tv.setText(message);
        return this;
    }

    /**
     * Update the text in this {@link TSnackbar}.
     *
     * @param resId The new text for the Toast.
     */
    @NonNull
    public TSnackbar setText(@StringRes int resId) {
        return setText(mContext.getText(resId));
    }

    /**
     * Return the duration.
     *
     * @see #setDuration
     */
    @Duration
    public int getDuration() {
        return mDuration;
    }

    /**
     * Set how long to show the view for.
     *
     * @param duration either be one of the predefined lengths:
     *                 {@link #LENGTH_SHORT}, {@link #LENGTH_LONG}, or a custom duration
     *                 in milliseconds.
     */
    @NonNull
    public TSnackbar setDuration(@Duration int duration) {
        mDuration = duration;
        return this;
    }

    /**
     * Returns the {@link TSnackbar}'s view.
     */
    @NonNull
    public View getView() {
        return mView;
    }

    /**
     * Show the {@link TSnackbar}.
     */
    public void show() {
        SnackbarManager.getInstance().show(mDuration, mManagerCallback);
    }

    /**
     * Dismiss the {@link TSnackbar}.
     */
    public void dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
    }

    private void dispatchDismiss(@Callback.DismissEvent int event) {
        SnackbarManager.getInstance().dismiss(mManagerCallback, event);
    }

    /**
     * Set a callback to be called when this the visibility of this {@link TSnackbar} changes.
     */
    @NonNull
    public TSnackbar setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    /**
     * Return whether this {@link TSnackbar} is currently being shown.
     */
    public boolean isShown() {
        return SnackbarManager.getInstance().isCurrent(mManagerCallback);
    }

    /**
     * Returns whether this {@link TSnackbar} is currently being shown, or is queued to be
     * shown next.
     */
    public boolean isShownOrQueued() {
        return SnackbarManager.getInstance().isCurrentOrNext(mManagerCallback);
    }

    final void showView() {
        if (mView.getParent() == null) {
            final ViewGroup.LayoutParams lp = mView.getLayoutParams();

            if (lp instanceof CoordinatorLayout.LayoutParams) {
                // If our LayoutParams are from a CoordinatorLayout, we'll setup our Behavior

                final Behavior behavior = new Behavior();
                behavior.setStartAlphaSwipeDistance(0.1f);
                behavior.setEndAlphaSwipeDistance(0.6f);
                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
                behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        dispatchDismiss(Callback.DISMISS_EVENT_SWIPE);
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                        switch (state) {
                            case SwipeDismissBehavior.STATE_DRAGGING:
                            case SwipeDismissBehavior.STATE_SETTLING:
                                // If the view is being dragged or settling, cancel the timeout
                                SnackbarManager.getInstance().cancelTimeout(mManagerCallback);
                                break;
                            case SwipeDismissBehavior.STATE_IDLE:
                                // If the view has been released and is idle, restore the timeout
                                SnackbarManager.getInstance().restoreTimeout(mManagerCallback);
                                break;
                        }
                    }
                });
                ((CoordinatorLayout.LayoutParams) lp).setBehavior(behavior);
            }
            mParent.addView(mView);
        }

        mView.setOnAttachStateChangeListener(new SnackbarLayout.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (isShownOrQueued()) {
                    // If we haven't already been dismissed then this event is coming from a
                    // non-user initiated action. Hence we need to make sure that we callback
                    // and keep our state up to date. We need to post the call since removeView()
                    // will call through to onDetachedFromWindow and thus overflow.
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onViewHidden(Callback.DISMISS_EVENT_MANUAL);
                        }
                    });
                }
            }
        });

        if (ViewCompat.isLaidOut(mView)) {
            // If the view is already laid out, animate it now
            animateViewIn();
        } else {
            // Otherwise, add one of our layout change listeners and animate it in when laid out
            mView.setOnLayoutChangeListener(new SnackbarLayout.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    animateViewIn();
                    mView.setOnLayoutChangeListener(null);
                }
            });
        }
    }

    private void animateViewIn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewCompat.setTranslationY(mView, -mView.getHeight());
            ViewCompat.animate(mView)
                    .translationY(0f)
                    .setInterpolator(vn.com.zalopay.wallet.view.custom.topsnackbar.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            mView.animateChildrenIn(ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                                    ANIMATION_FADE_DURATION);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            if (mCallback != null) {
                                mCallback.onShown(TSnackbar.this);
                            }
                            SnackbarManager.getInstance().onShown(mManagerCallback);
                        }
                    }).start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(mView.getContext(),
                    R.anim.top_in);
            anim.setInterpolator(vn.com.zalopay.wallet.view.custom.topsnackbar.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mCallback != null) {
                        mCallback.onShown(TSnackbar.this);
                    }
                    SnackbarManager.getInstance().onShown(mManagerCallback);
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mView.startAnimation(anim);
        }
    }

    private void animateViewOut(final int event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewCompat.animate(mView)
                    .translationY(-mView.getHeight())
                    .setInterpolator(vn.com.zalopay.wallet.view.custom.topsnackbar.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            mView.animateChildrenOut(0, ANIMATION_FADE_DURATION);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            onViewHidden(event);
                        }
                    }).start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.top_out);
            anim.setInterpolator(vn.com.zalopay.wallet.view.custom.topsnackbar.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    onViewHidden(event);
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mView.startAnimation(anim);
        }
    }

    final void hideView(int event) {
        if (mView.getVisibility() != View.VISIBLE || isBeingDragged()) {
            onViewHidden(event);
        } else {
            animateViewOut(event);
        }
    }

    private void onViewHidden(int event) {
        // First tell the SnackbarManager that it has been dismissed
        SnackbarManager.getInstance().onDismissed(mManagerCallback);
        // Now call the dismiss listener (if available)
        if (mCallback != null) {
            mCallback.onDismissed(this, event);
        }
        // Lastly, remove the view from the parent (if attached)
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
    }

    /**
     * @return if the view is being being dragged or settled by {@link SwipeDismissBehavior}.
     */
    private boolean isBeingDragged() {
        final ViewGroup.LayoutParams lp = mView.getLayoutParams();

        if (lp instanceof CoordinatorLayout.LayoutParams) {
            final CoordinatorLayout.LayoutParams cllp = (CoordinatorLayout.LayoutParams) lp;
            final CoordinatorLayout.Behavior behavior = cllp.getBehavior();

            if (behavior instanceof SwipeDismissBehavior) {
                return ((SwipeDismissBehavior) behavior).getDragState()
                        != SwipeDismissBehavior.STATE_IDLE;
            }
        }
        return false;
    }

    /**
     * @hide
     */
    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    /**
     * Callback class for {@link TSnackbar} instances.
     *
     * @see TSnackbar#setCallback(Callback)
     */
    public static abstract class Callback {
        /**
         * Indicates that the TSnackbar was dismissed via a swipe.
         */
        public static final int DISMISS_EVENT_SWIPE = 0;
        /**
         * Indicates that the TSnackbar was dismissed via an action click.
         */
        public static final int DISMISS_EVENT_ACTION = 1;
        /**
         * Indicates that the TSnackbar was dismissed via a timeout.
         */
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        /**
         * Indicates that the TSnackbar was dismissed via a call to {@link #dismiss()}.
         */
        public static final int DISMISS_EVENT_MANUAL = 3;
        /**
         * Indicates that the TSnackbar was dismissed from a new TSnackbar being shown.
         */
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        /**
         * Called when the given {@link TSnackbar} has been dismissed, either through a time-out,
         * having been manually dismissed, or an action being clicked.
         *
         * @param snackbar The snackbar which has been dismissed.
         * @param event    The event which caused the dismissal. One of either:
         *                 {@link #DISMISS_EVENT_SWIPE}, {@link #DISMISS_EVENT_ACTION},
         *                 {@link #DISMISS_EVENT_TIMEOUT}, {@link #DISMISS_EVENT_MANUAL} or
         *                 {@link #DISMISS_EVENT_CONSECUTIVE}.
         * @see TSnackbar#dismiss()
         */
        public void onDismissed(TSnackbar snackbar, @DismissEvent int event) {
            // empty
        }

        /**
         * Called when the given {@link TSnackbar} is visible.
         *
         * @param snackbar The snackbar which is now visible.
         * @see TSnackbar#show()
         */
        public void onShown(TSnackbar snackbar) {
            // empty
        }

        /**
         * @hide
         */
        @IntDef({DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent {
        }
    }

    /**
     * @hide
     */
    public static class SnackbarLayout extends LinearLayout {
        private LinearLayout mRootView;

        private TextView mMessageView;

        private View mViewLine;

        private RelativeLayout mErrorMessageWrapper;
        private TextView mTitleView;
        private TextView mErrorMessageView;
        private TextView mActionMessaageView;

        private GridView mGridBank;

        private int mMaxWidth;
        private OnLayoutChangeListener mOnLayoutChangeListener;
        private OnAttachStateChangeListener mOnAttachStateChangeListener;

        public SnackbarLayout(Context context) {
            this(context, null);
        }
        public SnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);

            mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);

            if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
                ViewCompat.setElevation(this, a.getDimensionPixelSize(
                        R.styleable.SnackbarLayout_elevation, 0));
            }
            a.recycle();

            setClickable(true);

            // Now inflate our content. We need to do this manually rather than using an <include>
            // in the layout since older versions of the Android do not inflate includes with
            // the correct Context.
            LayoutInflater.from(context).inflate(R.layout.tsnackbar_layout_include, this);

            ViewCompat.setAccessibilityLiveRegion(this, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
        }

        private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
            if (ViewCompat.isPaddingRelative(view)) {
                ViewCompat.setPaddingRelative(view,
                        ViewCompat.getPaddingStart(view), topPadding,
                        ViewCompat.getPaddingEnd(view), bottomPadding);
            } else {
                view.setPadding(view.getPaddingLeft(), topPadding,
                        view.getPaddingRight(), bottomPadding);
            }
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            mRootView = (LinearLayout) findViewById(R.id.snackbar_rootview);

            mMessageView = (TextView) findViewById(R.id.snackbar_text);
            mViewLine = findViewById(R.id.snackbar_line);
            mErrorMessageWrapper = (RelativeLayout) findViewById(R.id.snackbar_error_wrapper);
            mTitleView = (TextView) findViewById(R.id.snackbar_text_title);
            mErrorMessageView = (TextView) findViewById(R.id.snackbar_text_error);
            mActionMessaageView = (TextView) findViewById(R.id.snackbar_text_action);
            mGridBank = (GridView) findViewById(R.id.gridBank);
        }

        GridView getGridViewBankList() {
            return mGridBank;
        }

        View getViewLine() {
            return mViewLine;
        }

        TextView getMessageView() {
            return mMessageView;
        }

        TextView getErrorMessageView() {
            return mErrorMessageView;
        }

        TextView getTitleView() {
            return mTitleView;
        }

        TextView getActionMessageView() {
            return mActionMessaageView;
        }

        RelativeLayout getErrorMessageWrapper() {
            return mErrorMessageWrapper;
        }

        LinearLayout getSnackBarRootView() {
            return mRootView;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (mMaxWidth > 0 && getMeasuredWidth() > mMaxWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
            /*
            final int multiLineVPadding = getResources().getDimensionPixelSize(
                    R.dimen.design_snackbar_padding_vertical_2lines);
            final int singleLineVPadding = getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical);
            final boolean isMultiLine = mMessageView.getLayout().getLineCount() > 1;

            boolean remeasure = false;
            if (isMultiLine && mMaxInlineActionWidth > 0
                    && mActionView.getMeasuredWidth() > mMaxInlineActionWidth) {
                if (updateViewsWithinLayout(VERTICAL, multiLineVPadding,
                        multiLineVPadding - singleLineVPadding)) {
                    remeasure = true;
                }
            } else {
                final int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
                if (updateViewsWithinLayout(HORIZONTAL, messagePadding, messagePadding)) {
                    remeasure = true;
                }
            }


            if (remeasure) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
            */

        }

        void animateChildrenIn(int delay, int duration) {
            if (mMessageView.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mMessageView, 0f);
                ViewCompat.animate(mMessageView).alpha(1f).setDuration(duration).setStartDelay(delay).start();
            }

            if (mErrorMessageWrapper.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mErrorMessageView, 0f);
                ViewCompat.animate(mErrorMessageView).alpha(1f).setDuration(duration).setStartDelay(delay).start();

                ViewCompat.setAlpha(mActionMessaageView, 0f);
                ViewCompat.animate(mActionMessaageView).alpha(1f).setDuration(duration).setStartDelay(delay).start();

            }

            if (mGridBank.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mGridBank, 0f);
                ViewCompat.animate(mGridBank).alpha(1f).setDuration(duration)
                        .setStartDelay(delay).start();
            }

        }

        void animateChildrenOut(int delay, int duration) {
            if (mMessageView.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mMessageView, 1f);
                ViewCompat.animate(mMessageView).alpha(0f).setDuration(duration).setStartDelay(delay).start();
            }

            if (mErrorMessageWrapper.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mErrorMessageView, 1f);
                ViewCompat.animate(mErrorMessageView).alpha(0f).setDuration(duration).setStartDelay(delay).start();

                ViewCompat.setAlpha(mActionMessaageView, 1f);
                ViewCompat.animate(mActionMessaageView).alpha(0f).setDuration(duration).setStartDelay(delay).start();
            }

            if (mGridBank.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mGridBank, 1f);
                ViewCompat.animate(mGridBank).alpha(0f).setDuration(duration)
                        .setStartDelay(delay).start();
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (changed && mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
            mOnAttachStateChangeListener = listener;
        }

        private boolean updateViewsWithinLayout(final int orientation, final int messagePadTop, final int messagePadBottom) {
            boolean changed = false;
            if (orientation != getOrientation()) {
                setOrientation(orientation);
                changed = true;
            }
            if (mMessageView.getPaddingTop() != messagePadTop
                    || mMessageView.getPaddingBottom() != messagePadBottom) {
                updateTopBottomPadding(mMessageView, messagePadTop, messagePadBottom);
                changed = true;
            }
            return changed;
        }

        interface OnLayoutChangeListener {
            void onLayoutChange(View view, int left, int top, int right, int bottom);
        }

        interface OnAttachStateChangeListener {
            void onViewAttachedToWindow(View v);

            void onViewDetachedFromWindow(View v);
        }
    }

    final class Behavior extends SwipeDismissBehavior<SnackbarLayout> {
        @Override
        public boolean canSwipeDismissView(View child) {
            return child instanceof SnackbarLayout;
        }

        @Override
        public boolean onInterceptTouchEvent(CoordinatorLayout parent, SnackbarLayout child,
                                             MotionEvent event) {
            // We want to make sure that we disable any TSnackbar timeouts if the user is
            // currently touching the TSnackbar. We restore the timeout when complete
            if (parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY())) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        SnackbarManager.getInstance().cancelTimeout(mManagerCallback);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        SnackbarManager.getInstance().restoreTimeout(mManagerCallback);
                        break;
                }
            }

            return super.onInterceptTouchEvent(parent, child, event);
        }
    }
}