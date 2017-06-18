package vn.com.vng.zalopay.passport.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.zalopay.ui.widget.password.view.PassCodeRoundView;

/**
 * Created by hieuvm on 6/16/17.
 * *
 */

public class CustomPassCodeRoundView extends PassCodeRoundView implements NumberEditable {

    public CustomPassCodeRoundView(Context context) {
        this(context, null);
    }

    public CustomPassCodeRoundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPassCodeRoundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(attrs, defStyleAttr);
    }

    private int mPinLength = 6;
    private InputEnteredListener mEnteredListener;

    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null && !isInEditMode()) {
            /*final TypedArray attributes = getContext().obtainStyledAttributes(attrs, ,
                    defStyleAttr, 0);
            mPinLength = attributes.getInt(R.styleable.);
            attributes.recycle();*/
            setPinLength(mPinLength);
        }


    }

    private String mString;

    @Override
    public void append(int number) {
        if (mString == null) {
            mString = "";
        }

        if (mString.length() >= mPinLength) {
            return;
        }

        mString += number;
        setInternalText(mString);
    }

    @Override
    public void delete() {
        if (TextUtils.isEmpty(mString)) {
            return;
        }

        mString = mString.substring(0, mString.length() - 1);
        setInternalText(mString);
    }

    @Override
    public void setInputText(String text) {
        if (text == null) {
            text = "";
        }

        if (text.length() >= mPinLength) {
            text = text.substring(0, mPinLength);
        }

        mString = text;
        setInternalText(mString);
    }

    private void setInternalText(String text) {
        int length = text == null ? 0 : text.length();
        refresh(length);

        if (length < mPinLength) {
            return;
        }

        if (mEnteredListener != null) {
            mEnteredListener.onPinEntered(text == null ? "" : text);
        }
    }

    @Override
    public String getInputText() {
        return mString == null ? "" : mString;
    }

    public void setPinEnteredListener(InputEnteredListener enteredListener) {
        mEnteredListener = enteredListener;
    }
}
