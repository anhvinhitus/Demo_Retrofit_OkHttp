package vn.com.zalopay.wallet.view.custom;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;

import timber.log.Timber;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.validation.CardValidation;
import vn.com.zalopay.wallet.view.custom.cardview.CreditCardUtils;
import vn.com.zalopay.wallet.view.interfaces.IDoActionDrawableEdittext;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;

public class VPaymentDrawableEditText extends VPaymentEditText implements IDoActionDrawableEdittext {
    public static final char VERTICAL_SEPERATOR = ' ';
//    private Drawable drawableRightDelete;
    private OnClickListener mSelectedCardScanListener = null;
    private OnClickListener mOnClickListener = null;
    private int mLastInputLength = 0;

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

    private void init(AttributeSet attrs, int defStyle) {
        this.addTextChangedListener(mTextFormater);
        this.setClickable(true);
        this.setEnabled(true);
        this.setDoActionListner(this);
        if (drawableRightDelete == null) {
            getDrawableRight();
        }
        SPACE_SEPERATOR = VERTICAL_SEPERATOR;
    }

//    public void getDrawableRight() {
//        super.loadDeleteIco()
//                .compose(SchedulerHelper.applySchedulers())
//                .subscribe(bitmap -> {
//                            drawableRightDelete = bitmap;
//                            Rect rectIcon = drawableRightDelete.getBounds();
//                            //extend the bound
//                            bounds = new Rect();
//                            bounds.set(rectIcon.left - OFFSET, rectIcon.top - OFFSET, rectIcon.right + OFFSET,
//                                    rectIcon.bottom + OFFSET);
//                        },
//                        throwable -> Timber.d(throwable, "Exception load bitmap delete ico"));
//
//    }

    public boolean isValidInput() {
        String input = getString();

        return TextUtils.isEmpty(input) || CardValidation.validCardName(input);

    }

    /***
     * check pattern input valid rule from bundle
     */
    @Override
    public boolean isValid() {
        ChannelActivity activity = BaseActivity.getChannelActivity();
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        AbstractWorkFlow adapterBase = activity.getWorkFlow();
        if (mIsPattern && adapterBase != null) {
            try {
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
                        Timber.d("id %s is match", mEditTextConfig.id);
                        return true;
                    } else {
                        Timber.d("id %s not match", mEditTextConfig.id);
                    }
                }
            } catch (Exception e) {
                Timber.w(e, "Exception valid input");
            }
            return false;
        }
        return super.isValid();
    }

    @Override
    public void formatText(boolean isTextFull) {
        super.formatText(isTextFull);
        try {
            if (!mIsTextGroup) {
                return;
            }

            Editable s = getEditableText();
            Timber.d("onTextChanged %s", s.toString());
            String text = s.toString();
            int mlength = text.length();
            if (mLastInputLength > mlength) {
                text.replace(String.valueOf(SPACE_SEPERATOR), "").trim();
                String newString = CreditCardUtils.handleCardNumber(text, CreditCardUtils.SPACE_SEPERATOR);
                mLastInputLength = newString.length();
                setText(newString);
                return;

            }
            if (text.endsWith(" ")) {
                return;
            }
            if (mlength > 0 && (mlength % 5) == 0) {
                setText(new StringBuilder(text).insert(text.length() - 1, String.valueOf(VERTICAL_SEPERATOR)).toString());
            }
            setSelection(getLength());
            mLastInputLength = getLength();
        } catch (Exception e) {
            Timber.d("onTextChanged %s", e.getMessage());
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

        if (s.length() > 0) {
            if (drawableRightDelete == null) {
                getDrawableRight();
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
