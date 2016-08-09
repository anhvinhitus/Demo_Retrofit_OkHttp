package com.zalopay.ui.widget;

/**
 * Created by AnhHieu on 6/10/16.
 */

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by AnhHieu on 12/30/15.
 */
public class KeyboardLinearLayout extends LinearLayout {

    private KeyboardHelper mHelper;

    public KeyboardLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHelper = new KeyboardHelper(this);
    }

    public KeyboardLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHelper = new KeyboardHelper(this);
    }

    public KeyboardLinearLayout(Context context) {
        super(context);
        mHelper = new KeyboardHelper(this);
    }

    public void setOnKeyboardStateListener(KeyboardHelper.OnKeyboardStateChangeListener listener) {
        mHelper.setOnKeyboardStateListener(listener);
    }


    public KeyboardHelper getKeyBoardHelper() {
        return mHelper;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mHelper.init();
    }

    public static class KeyboardHelper {

        public interface OnKeyboardStateChangeListener {
            void onKeyBoardShow(int height);

            void onKeyBoardHide();
        }


        private OnKeyboardStateChangeListener mListener;
        private static final int UNKNOW = -1;
        private Rect mRect = new Rect();
        private Rect mOriRect = new Rect();
        private int mKeyboardHeight = UNKNOW;
        private boolean isKeyboardShow = false;
        private View mRootView;


        public void setOnKeyboardStateListener(OnKeyboardStateChangeListener listener) {
            mListener = listener;
        }

        public KeyboardHelper(View root) {
            this.mRootView = root;
            if (this.mRootView == null) {
                throw new RuntimeException(KeyboardHelper.class.getName() + " RootView Can NOT be null");
            }
            setGlobalLayoutListener();
        }

        public void hideKeyboard() {
            hideKeyBoardForce(this.mRootView);
        }

        public void showKeyboard(EditText view) {
            showKeyBoardForce(view);
        }

        public boolean isKeyboardShow() {
            return isKeyboardShow;
        }

        private static void showKeyBoardForce(final View view) {
            if (view == null) {
                return;
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                }
            });
        }

        private static void hideKeyBoardForce(final View view) {
            if (view == null) {
                return;
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            });
        }

        /**
         * this method must call after onLayout()
         */
        public void init() {
            if (mOriRect.height() == 0) {
                this.mRootView.getWindowVisibleDisplayFrame(mOriRect);
            }
        }

        private void setGlobalLayoutListener() {

            this.mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mOriRect.height() == 0) {
                        throw new RuntimeException(KeyboardHelper.class.getName() + " You must call init() before setGlobalLayoutListener()");
                    }
                    if (mListener == null) {
                        return;
                    }

                    mRootView.getWindowVisibleDisplayFrame(mRect);

                    int height = mOriRect.height() - mRect.height();
                    if (height == mKeyboardHeight) {
                        return;
                    } else {
                        if (mKeyboardHeight != UNKNOW) {
                            if (height > 0) {
                                mListener.onKeyBoardShow(height);
                                isKeyboardShow = true;
                            } else {
                                mListener.onKeyBoardHide();
                                isKeyboardShow = false;
                            }
                        }

                        mKeyboardHeight = height;
                    }
                }
            });
        }
    }

}
