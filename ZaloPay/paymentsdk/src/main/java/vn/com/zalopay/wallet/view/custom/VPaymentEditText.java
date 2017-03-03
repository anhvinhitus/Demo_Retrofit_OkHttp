package vn.com.zalopay.wallet.view.custom;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.view.interfaces.IBaseDoActionEdittext;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicEditText;
import vn.com.zalopay.wallet.utils.Log;

public class VPaymentEditText extends TextInputEditText {
    public static final int OFFSET = (int) GlobalData.getAppContext().getResources().getDimension(R.dimen.zpw_offset_drawable_right);
    protected DDynamicEditText mEditTextConfig = null;
    protected char SPACE_SEPERATOR;

    protected AdapterBase mAdapter = null;

    protected boolean mIsTextGroup = true;
    protected boolean mIsPattern = false;
    protected String mPattern = null;
    protected Rect bounds;
    protected int actionX, actionY;
    protected IBaseDoActionEdittext mBaseDoActionEdittextListner;
    protected String mText = "";
    protected TextWatcher mTextFormater = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkEnableDrawableRight();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mText = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            formatText(false);
        }
    };

    public VPaymentEditText(Context context) {
        super(context);
    }

    public VPaymentEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VPaymentEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void setDoActionListner(IBaseDoActionEdittext pListener) {
        this.mBaseDoActionEdittextListner = pListener;
    }

    /***
     * base validate
     *
     * @return
     */
    public boolean isValid() {
        return true;
    }

    /***
     * validate input by pattern in config.xml
     *
     * @return
     */
    public boolean isValidPattern() {
        return true;
    }

    /***
     * validate input
     *
     * @return
     */
    public boolean isValidInput() {
        return true;
    }

    public void init(DDynamicEditText pEditText, AdapterBase pAdapter) {
        mIsPattern = pEditText.pattern;
        mEditTextConfig = pEditText;

        mAdapter = pAdapter;

        this.addTextChangedListener(mTextFormater);
    }

    public String getPatternErrorMessage() {
        return null;
    }

    @Nullable
    public TextInputLayout getTextInputLayout() {
        View currentView = this;

        for (int i = 0; i < 2; i++) {
            ViewParent parentView = currentView.getParent();

            if (parentView instanceof TextInputLayout) {
                return (TextInputLayout) parentView;
            } else {
                currentView = (View) parentView;
            }
        }

        return null;
    }

    /***
     * load bitmap from bundle and convert to drawable
     *
     * @param pIconName
     * @return
     */
    protected Drawable getDrawable(String pIconName) {
        //load bitmap
        try {
            return new BitmapDrawable(getResources(), ResourceManager.getImage(pIconName));

        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return null;
    }

    public String getString() {
        if (mIsTextGroup) {
            return getText().toString().replace(String.valueOf(SPACE_SEPERATOR), "").trim();
        } else {
            return getText().toString().trim();
        }
    }

    public void setGroupText(boolean pIsEnabled) {
        mIsTextGroup = pIsEnabled;
    }

    protected void checkEnableDrawableRight() {

    }

    protected void formatText(boolean pIsFullText) {
    }

    public boolean isInputMaxLength() {
        if (getText().toString().length() == getMaxLength())
            return true;
        return false;
    }

    protected int getMaxLength() {
        return -1;
    }

    public int getLength() {
        return getText().toString().length();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            actionX = (int) event.getX();
            actionY = (int) event.getY();
            int xTouch, yTouch;
            int extraTapArea = 13;
            /**
             * IF USER CLICKS JUST OUT SIDE THE RECTANGLE OF THE DRAWABLE
             * THAN ADD X AND SUBTRACT THE Y WITH SOME VALUE SO THAT AFTER
             * CALCULATING X AND Y CO-ORDINATE LIES INTO THE DRAWBABLE
             * BOUND. - this process help to increase the tappable area of
             * the rectangle.
             */
            xTouch = (int) (actionX + extraTapArea);
            yTouch = (int) (actionY - extraTapArea);

            /**Since this is right drawable subtract the value of x from the width
             * of view. so that width - tappedarea will result in x co-ordinate in drawable bound.
             */
            xTouch = getWidth() - xTouch;

            /**x can be negative if user taps at x co-ordinate just near the width.
             * e.g views width = 300 and user taps 290. Then as per previous calculation
             * 290 + 13 = 303. So subtract X from getWidth() will result in negative value.
             * So to avoid this add the value previous added when x goes negative.
             */

            if (xTouch <= 0) {
                xTouch += extraTapArea;
            }

            /** If result after calculating for extra tappable area is negative.
             * assign the original value so that after subtracting
             * extratapping area value doesn't go into negative value.
             */

            if (yTouch <= 0)
                yTouch = actionY;

            /**If drawble bounds contains the x and y points then move ahead.*/
            if (bounds != null && bounds.contains(xTouch, yTouch)) {
                if (mBaseDoActionEdittextListner != null)
                    mBaseDoActionEdittextListner.doAction();
            }
        }

        return super.onTouchEvent(event);
    }

    /***
     * prevent user hide softkeyboard when inputting the card info
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && GlobalData.isChannelHasInputCard() && mAdapter != null && mAdapter.isInputStep()) {
            Log.d(this, "can not back,you have to input card info");
            return true;
        }
        return false;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        // Do your thing:
        boolean consumed = super.onTextContextMenuItem(id);
        // React:
        switch (id) {
            case android.R.id.paste:
                onTextPaste();
                break;
        }
        return consumed;
    }

    public void onTextPaste() {
        this.setText(mText);
    }
}
