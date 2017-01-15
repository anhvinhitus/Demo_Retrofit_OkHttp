package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.zalopay.ui.widget.edittext.ZPEditText;
import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.CurrencyUtil;

/**
 * Created by hieuvm on 11/12/16.
 */

public class MoneyEditText extends ZPEditText {

    private long mAmount;
    private long mMaxAmount = Constants.MAX_TRANSFER_MONEY;
    private long mMinAmount = Constants.MIN_TRANSFER_MONEY;

    public MoneyEditText(Context context) {
        super(context);
        init(context, null);
    }

    public MoneyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MoneyEditText(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        initAmountWatcher();
    }

    public void setMinMaxMoney(final long minMoney, final long maxMoney) {
        clearValidators();
        mMinAmount = minMoney > 0 ? minMoney : Constants.MIN_TRANSFER_MONEY;
        mMaxAmount = maxMoney > 0 ? maxMoney : Constants.MAX_TRANSFER_MONEY;

        addValidator(new ZPEditTextValidate(String.format(getContext().getString(R.string.min_money),
                CurrencyUtil.formatCurrency(mMinAmount, true))) {
            @Override
            public boolean isValid(@NonNull CharSequence s) {
                return mAmount >= mMinAmount;
            }
        });

        addValidator(new ZPEditTextValidate(String.format(getContext().getString(R.string.max_money),
                CurrencyUtil.formatCurrency(mMaxAmount, true))) {
            @Override
            public boolean isValid(@NonNull CharSequence s) {
                return mAmount <= mMaxAmount;
            }
        });
    }

    public long getAmount() {
        return mAmount;
    }

    public void setAmount(long mAmount) {
        this.mAmount = mAmount;
    }

    private void initAmountWatcher() {
        addTextChangedListener(new TextWatcher() {
            private boolean isFirstOver;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                long n = 0;
                removeTextChangedListener(this);

                try {
                    int inilen, endlen;
                    inilen = getText().length();
                    String v = s.toString().replaceAll("[^\\d]", "").trim();
                    if ("".equals(v)) {
                        n = 0;
                    } else {
                        n = Long.parseLong(v);
                    }

                    if (n >= mMaxAmount) {
                        if (isFirstOver) {
                            n = mAmount;
                        }

                        isFirstOver = true;
                    }

                    if (n < mMaxAmount) {
                        isFirstOver = false;
                    }

                    int cp = getSelectionStart();
                    if (n <= 0) {
                        setText("");
                    } else {
                        setText(CurrencyUtil.formatCurrency(n, false));
                    }

                    endlen = getText().length();
                    int sel = (cp + (endlen - inilen));
                    if (sel > 0 && sel <= getText().length()) {
                        setSelection(sel);
                    } else {
                        // place cursor at the end?
                        setSelection(getText().length() > 1 ? getText().length() - 1 : 0);
                    }

                } catch (NumberFormatException nfe) {
                    // do nothing?
                } finally {
                    addTextChangedListener(this);
                    mAmount = n;
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}