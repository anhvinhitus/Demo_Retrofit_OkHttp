package vn.com.zalopay.wallet.view.custom;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.view.interfaces.IDoActionDateEdittext;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class VPaymentValidDateEditText extends VPaymentEditText implements IDoActionDateEdittext {
    public static final int FIELD_MONTH = 1;
    public static final int FIELD_YEAR = 2;
    private static final char VERTICAL_SEPERATOR = '/';
    private boolean mIsCheckNow = false;
    private boolean mIsCheckPass = false;

    private Drawable drawableRight;

    private int mPreLength;

    public VPaymentValidDateEditText(Context context) {
        super(context, null);
        init(null, 0);
    }

    public VPaymentValidDateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public VPaymentValidDateEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @Override
    protected void finalize() throws Throwable {
        drawableRight = null;
        super.finalize();
    }

    public void setCheckDateNow(boolean pIsCheck) {
        mIsCheckNow = pIsCheck;
    }

    public void setCheckPass(boolean pIsCheck) {
        mIsCheckPass = pIsCheck;
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (this.getContext() instanceof PaymentChannelActivity) {
            mAdapter = ((PaymentChannelActivity) getContext()).getAdapter();
        }

        this.addTextChangedListener(mTextFormater);
        this.setClickable(true);
        this.setEnabled(true);
        this.setDoActionListner(this);

        if (drawableRight == null)
            drawableRight = getDrawable(RS.drawable.ic_delete);

        if (drawableRight != null) {
            Rect rectIcon = drawableRight.getBounds();

            //extend the bound
            bounds = new Rect();
            bounds.set(rectIcon.left - OFFSET, rectIcon.top - OFFSET, rectIcon.right + OFFSET,
                    rectIcon.bottom + OFFSET);
        }

        SPACE_SEPERATOR = VERTICAL_SEPERATOR;
    }

    public String getTextField(int pField) {
        String text = getString();

        if (TextUtils.isEmpty(text)) {
            return null;
        }
        try {
            if (pField == FIELD_MONTH) {
                return text.substring(0, 2);
            } else if (pField == FIELD_YEAR) {
                return text.substring(2);
            }
        } catch (Exception e) {
            Log.d(this, e);
        }
        return null;
    }

    @Override
    public int getMaxLength() {

        return getResources().getInteger(R.integer.maximum_character_field_visa_valid_date);
    }

    public String getContent() {
        return getText().toString();
    }

    @Override
    public boolean isValid() {
        String strMonth = getTextField(FIELD_MONTH);

        String strYear = getTextField(FIELD_YEAR);

        int month = 0, year = 0;

        try {
            if (!TextUtils.isEmpty(strMonth))
                month = Integer.parseInt(strMonth);
            if (!TextUtils.isEmpty(strYear))
                year = Integer.parseInt(strYear);
        } catch (Exception ex) {
            Log.e(this, ex);
        }

        if (month <= 0 || year <= 0 || month > 12)
            return false;

        if (mIsCheckNow) {
            try {
                if ((year < ZPWUtils.getYear())
                        || ((year == ZPWUtils.getYear()) && (month < ZPWUtils.getMonth()))) {
                    Log.d(VPaymentValidDateEditText.this, "**** " + mEditTextConfig.id + "NOW NOT MATCH ****");

                    return false;
                } else {
                    Log.d(VPaymentValidDateEditText.this, "**** " + mEditTextConfig.id + " NOW MATCH ****");
                }

            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        /***
         * ISSUE DAY MUST LESS THAN TODAY
         */

        if (mIsCheckPass) {
            try {
                if ((year > ZPWUtils.getYear())
                        || ((year == ZPWUtils.getYear()) && (month > ZPWUtils.getMonth()))) {
                    Log.d(VPaymentValidDateEditText.this, "**** " + mEditTextConfig.id + "PASS NOT MATCH ****");
                    return false;
                } else {
                    Log.d(VPaymentValidDateEditText.this, "**** " + mEditTextConfig.id + " PASS MATCH ****");
                }

            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        /**
         * MONTH < 12
         */
        try {
            if (month > 12)
                return false;
        } catch (Exception e) {
            Log.e(this, e);
        }

        /***
         * check pattern get from bundle
         */
        if (mIsPattern && mAdapter != null) {
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
                    Log.d(VPaymentValidDateEditText.this, "**** " + mEditTextConfig.id + " MATCH ****");
                    return true;
                } else {
                    Log.d(VPaymentValidDateEditText.this, "**** " + mEditTextConfig.id + " NOT MATCH ****");
                }
            }
            return false;
        }

        return super.isValid();
    }

    @Override
    public void formatText(boolean isTextFull) {
        if (mIsTextGroup) {
            Editable s = getEditableText();

            if (isTextFull) {
                for (int i = 0; i < s.length(); i++) {
                    if (i > 1 && (i % 3) == 0) {
                        char c = s.charAt(i - 1);
                        // Only if its a digit where there should be a space we
                        // insert a space
                        if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(SPACE_SEPERATOR)).length <= 2) {

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
            if (mPreLength != 0 && mPreLength < s.length() && s.length() > 0 && ((s.length() % 2) == 0 || (mPreLength % 2) == 0) && !s.toString().contains(String.valueOf(SPACE_SEPERATOR))) {
                char c = s.charAt(s.length() - 1);
                // Only if its a digit where there should be a space we
                // insert a space
                if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(SPACE_SEPERATOR)).length <= 1 && mPreLength < 2) {
                    InputFilter[] filters = s.getFilters(); // save filters
                    s.setFilters(new InputFilter[]{}); // clear filters
                    s.insert(s.length(), String.valueOf(SPACE_SEPERATOR));
                    s.setFilters(filters); // restore filters
                } else if (mPreLength == 2 && !s.toString().contains(String.valueOf(SPACE_SEPERATOR))) {
                    InputFilter[] filters = s.getFilters(); // save filters
                    s.setFilters(new InputFilter[]{}); // clear filters
                    s.insert(s.length() - 1, String.valueOf(SPACE_SEPERATOR));
                    s.setFilters(filters); // restore filters
                }
            } else if (mPreLength >= s.length() && s.length() > 2 && !s.toString().contains(String.valueOf(SPACE_SEPERATOR))) {
                s.clear();
            }

            mPreLength = s.length();
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
        if (mIsPattern) {
            return isValid();
        }

        return super.isValidPattern();
    }

    @Override
    protected void checkEnableDrawableRight() {
        //show remove icon

        String s = getString();
        setCompoundDrawablePadding(20);

        if (s.length() > 0) {
            if (drawableRight == null) {
                drawableRight = getDrawable(RS.drawable.ic_delete);

                if (drawableRight != null) {
                    Rect rectIcon = drawableRight.getBounds();

                    //extend the bound
                    bounds = new Rect();
                    bounds.set(rectIcon.left - OFFSET, rectIcon.top - OFFSET, rectIcon.right + OFFSET,
                            rectIcon.bottom + OFFSET);
                }
            }
            if (drawableRight != null) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawableRight, null);
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
    public void doAction() {
        setText(null);
    }
}
