package vn.com.zalopay.wallet.view.custom;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;

import vn.com.zalopay.wallet.business.behavior.view.interfaces.IDoActionDrawableEdittext;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.validation.CardValidation;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class VPaymentDrawableEditText extends VPaymentEditText implements IDoActionDrawableEdittext {
    public static final char VERTICAL_SEPERATOR = ' ';
    private static final String TAG = VPaymentDrawableEditText.class.getName();
    //private Drawable drawableRightScan;
    private Drawable drawableRightDelete;

    //private boolean mIsCameraScan = false;

    private OnClickListener mSelectedCardScanListener = null;
    private OnClickListener mOnClickListener = null;

    public VPaymentDrawableEditText(Context context) {
        super(context, null);
        init(null, 0);
    }

    public VPaymentDrawableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public VPaymentDrawableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setOnClickListener(OnClickListener pListener) {
        this.mOnClickListener = pListener;
    }

    @Override
    protected void finalize() throws Throwable {
        //drawableRightScan = null;
        drawableRightDelete = null;

        super.finalize();
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (this.getContext() instanceof PaymentChannelActivity) {
            mAdapter = ((PaymentChannelActivity) getContext()).getAdapter();
        }
        this.addTextChangedListener(mTextFormater);
        this.setClickable(true);
        this.setEnabled(true);
        this.setDoActionListner(this);
        if (drawableRightDelete == null) {
            drawableRightDelete = getDrawable(RS.drawable.ic_delete);
        }

        if (drawableRightDelete != null) {
            Rect rectIcon = drawableRightDelete.getBounds();
            //extend the bound
            bounds = new Rect();
            bounds.set(rectIcon.left - OFFSET, rectIcon.top - OFFSET, rectIcon.right + OFFSET,
                    rectIcon.bottom + OFFSET);
        }

        SPACE_SEPERATOR = VERTICAL_SEPERATOR;
    }

    public boolean isValidInput() {
        String input = getString();

        if (TextUtils.isEmpty(input))
            return true;

        return CardValidation.validCardName(input);
    }

    /***
     * check pattern input valid rule from bundle
     */
    @Override
    public boolean isValid() {
        if (mIsPattern && mAdapter != null) {
            try {

                mPattern = ResourceManager.getInstance(null).getPattern(mEditTextConfig.id, mAdapter.getChannelID());

                if (mPattern == null) {
                    mPattern = ResourceManager.getInstance(null).getPattern(mEditTextConfig.id, "all");
                }
                if (this.getText().length() == 0) {
                    return true;
                }

                if (mPattern != null) {
                    String text = getString();

                    if (text.matches(mPattern)) {
                        Log.d(VPaymentDrawableEditText.this, "===" + mEditTextConfig.id + "===is match");
                        return true;
                    } else {
                        Log.d(VPaymentDrawableEditText.this, "===" + mEditTextConfig.id + "===not match");
                    }
                }

            } catch (Exception e) {
                Log.e(this, e);
            }

            return false;
        }

        return super.isValid();
    }

    @Override
    public void formatText(boolean isTextFull) {
        super.formatText(isTextFull);

        Editable s = getEditableText();
        if (mIsTextGroup) {
            // Remove spacing char
            if (s.length() > 0 && (s.length() % 5) == 0) {
                final char c = s.charAt(s.length() - 1);
                if (SPACE_SEPERATOR == c) {
                    s.delete(s.length() - 1, s.length());
                }
            }
            if (isTextFull) {
                for (int i = 0; i < s.length(); i++) {
                    if (i > 1 && (i % 5) == 0) {
                        char c = s.charAt(i - 1);
                        // Only if its a digit where there should be a space we
                        // insert a space
                        if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(SPACE_SEPERATOR)).length <= 3) {

                            InputFilter[] filters = s.getFilters(); // save filters
                            s.setFilters(new InputFilter[]{}); // clear filters
                            s.insert(i - 1, String.valueOf(SPACE_SEPERATOR));
                            s.setFilters(filters); // restore filters
                        }
                    }
                }
                return;
            }
            // Insert char where needed.
            if (s.length() > 0 && (s.length() % 5) == 0 && s.length() <= 20) {
                char c = s.charAt(s.length() - 1);
                // Only if its a digit where there should be a space we
                // insert a space
                if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(SPACE_SEPERATOR)).length <= 4) {

                    InputFilter[] filters = s.getFilters(); // save filters
                    s.setFilters(new InputFilter[]{}); // clear filters
                    s.insert(s.length() - 1, String.valueOf(SPACE_SEPERATOR));
                    s.setFilters(filters); // restore filters
                }
            }

        }
    }

    /***
     * show remove icon if editext has text
     */
    @Override
    protected void checkEnableDrawableRight() {
        super.checkEnableDrawableRight();

        String s = getString();
        setCompoundDrawablePadding(10);

        if (s.toString().length() > 0) {
            if (drawableRightDelete == null) {
                drawableRightDelete = getDrawable(RS.drawable.ic_delete);

                if (drawableRightDelete != null) {
                    Rect rectIcon = drawableRightDelete.getBounds();

                    //extend the bound
                    bounds = new Rect();
                    bounds.set(rectIcon.left - OFFSET, rectIcon.top - OFFSET, rectIcon.right + OFFSET,
                            rectIcon.bottom + OFFSET);
                }
            }
            if (drawableRightDelete != null) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRightDelete, null);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawableRightDelete, null);

                }
            }
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    @Override
    public String getPatternErrorMessage() {
        if (mIsPattern && mEditTextConfig != null)
            return mEditTextConfig.errMess;

        return super.getPatternErrorMessage();
    }

    @Override
    public boolean isValidPattern() {
        if (mIsPattern)
            return isValid();

        return super.isValidPattern();
    }

    @Override
    public void doAction() {
        if (mOnClickListener != null)
            mOnClickListener.onClick(this);

        if (!TextUtils.isEmpty(getString()))
            setText(null);
        else if (mSelectedCardScanListener != null)
            mSelectedCardScanListener.onClick(this);
    }

}
