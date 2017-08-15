package vn.com.zalopay.wallet.view.custom;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.view.interfaces.IDoActionDateEdittext;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;

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

    public void setCheckDateNow(boolean pIsCheck) {
        mIsCheckNow = pIsCheck;
    }

    public void setCheckPass(boolean pIsCheck) {
        mIsCheckPass = pIsCheck;
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.addTextChangedListener(mTextFormater);
        this.setClickable(true);
        this.setEnabled(true);
        this.setDoActionListner(this);

        if (drawableRight == null) {
            getDrawableRight();
        }
        SPACE_SEPERATOR = VERTICAL_SEPERATOR;
    }

    public void getDrawableRight() {
        super.loadDeleteIco()
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(bitmap -> {
                            drawableRight = bitmap;
                            Rect rectIcon = drawableRight.getBounds();
                            //extend the bound
                            bounds = new Rect();
                            bounds.set(rectIcon.left - OFFSET, rectIcon.top - OFFSET, rectIcon.right + OFFSET,
                                    rectIcon.bottom + OFFSET);
                        },
                        throwable -> Timber.d(throwable, "Exception load bitmap delete ico"));

    }

    public String getTextField(int pField) {
        String text = getString();

        if (TextUtils.isEmpty(text) || text.length() < 2) {
            return null;
        }
        try {
            if (pField == FIELD_MONTH) {
                return text.substring(0, 2);
            } else if (pField == FIELD_YEAR) {
                return text.substring(2);
            }
        } catch (Exception e) {
            Timber.d(e);
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
            if (!TextUtils.isEmpty(strMonth)) {
                month = Integer.parseInt(strMonth);
            }
            if (!TextUtils.isEmpty(strYear)) {
                year = Integer.parseInt(strYear);
            }
        } catch (Exception e) {
            Timber.d(e, "Exception valid input");
        }

        if (month <= 0 || year <= 0 || month > 12) {
            return false;
        }
        if (mIsCheckNow) {
            try {
                if ((year < SdkUtils.getYear())
                        || ((year == SdkUtils.getYear()) && (month < SdkUtils.getMonth()))) {
                    return false;
                }
            } catch (Exception e) {
                Timber.d(e, "Exception valid input");
            }
        }
        if (mIsCheckPass) {
            try {
                if ((year > SdkUtils.getYear())
                        || ((year == SdkUtils.getYear()) && (month > SdkUtils.getMonth()))) {
                    return false;
                }
            } catch (Exception e) {
                Timber.d(e, "Exception valid input");
            }
        }
        //MONTH < 12
        if (month > 12) {
            return false;
        }

        //check pattern get from bundle
        if (mIsPattern) {
            ChannelActivity channelActivity = BaseActivity.getChannelActivity();
            if (channelActivity == null || channelActivity.isFinishing()) {
                return false;
            }
            AbstractWorkFlow adapterBase = channelActivity.getWorkFlow();
            if (adapterBase == null) {
                return false;
            }
            mPattern = ResourceManager.getInstance(null).getPattern(mEditTextConfig.id, String.valueOf(adapterBase.getChannelID()));
            if (mPattern == null) {
                mPattern = ResourceManager.getInstance(null).getPattern(mEditTextConfig.id, "all");
            }

            if (this.getText().length() == 0) {
                return true;
            }

            if (mPattern != null) {
                String text = getString();
                if (text.matches(mPattern)) {
                    return true;
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
                getDrawableRight();
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
