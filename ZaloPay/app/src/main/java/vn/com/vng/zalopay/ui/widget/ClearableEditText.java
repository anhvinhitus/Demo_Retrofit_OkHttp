package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import vn.com.vng.zalopay.R;

/**
 * Created by longlv on 23/05/2016.
 * EditText with clear button at the end.
 */
public class ClearableEditText extends TextInputEditText {
    private int mPaddingDefault;
    private Drawable mDrawableRightDelete;
    protected Rect mBounds;
    protected int mActionX;
    protected int mActionY;
    private int left, right, top, bottom;
    protected TextWatcher mTextFormater = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ClearableEditText.this.checkEnableDrawableRight();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
        }
    };

    public ClearableEditText(Context context) {
        super(context);
        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.addTextChangedListener(this.mTextFormater);
        this.addTextChangedListener(this.mTextFormater);
        this.setClickable(true);
        this.setEnabled(true);
        mPaddingDefault = (int)getResources().getDimension(R.dimen.border);
        initDrawableRight();
    }

    @SuppressWarnings("deprecation")
    private void initDrawableRight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mDrawableRightDelete = getContext().getDrawable(R.drawable.ic_remove_circle);
        } else {
            this.mDrawableRightDelete = this.getResources().getDrawable(R.drawable.ic_remove_circle);
        }
    }

    public String getString() {
        if (this.getText() == null) {
            return "";
        }
        return this.getText().toString();
    }

    private void checkEnableDrawableRight() {
        String s = this.getString();
        this.setCompoundDrawablePadding(mPaddingDefault);
        if (s.length() > 0) {
            if (this.mDrawableRightDelete == null) {
                initDrawableRight();
            }

            if (this.mDrawableRightDelete != null) {
                this.mBounds = this.mDrawableRightDelete.getBounds();
                this.setCompoundDrawablesWithIntrinsicBounds(null, null, this.mDrawableRightDelete, null);
            }
        } else {
            this.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

    }

    protected int getMaxLength() {
        return -1;
    }

    public boolean isValid() {
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 0) {
            this.mActionX = (int) event.getX();
            this.mActionY = (int) event.getY();
            byte extraTapArea = (byte) getResources().getDimension(R.dimen.border);
            int xTouch = this.mActionX + extraTapArea;
            int yTouch = this.mActionY - extraTapArea;
            xTouch = this.getWidth() - xTouch;
            if (xTouch <= 0) {
                xTouch += extraTapArea;
            }

            if (yTouch <= 0) {
                yTouch = this.mActionY;
            }
            if (this.mBounds != null) {
                int mOffset = (getMeasuredHeight() - this.mBounds.height()) / 2;
                if (mOffset <= 0) {
                    mOffset = (int) getResources().getDimension(R.dimen.border);
                }
                left = this.mBounds.left - mOffset;
                top = this.mBounds.top - mOffset;
                right = this.mBounds.right + mOffset;
                bottom = this.mBounds.bottom + mOffset;
                if (contains(xTouch, yTouch)) {
                    if (!TextUtils.isEmpty(this.getString())) {
                        this.setText(null);
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }

    private boolean contains(int x, int y) {
        return left < right && top < bottom  // check for empty first
                && x >= left && x < right && y >= top && y < bottom;
    }

    @Override
    protected void finalize() throws Throwable {
        this.mDrawableRightDelete = null;
        super.finalize();
    }

    public static String optText(ClearableEditText editText) {
        if (editText == null) {
            return null;
        }

        Editable text = editText.getText();
        if (text == null) {
            return null;
        }

        return text.toString();
    }
}
