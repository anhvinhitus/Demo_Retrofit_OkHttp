package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.VNDCurrencyTextWatcher;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 8/29/16.
 * *
 */
public class SetAmountFragment extends BaseFragment {

    public static SetAmountFragment newInstance() {

        Bundle args = new Bundle();

        SetAmountFragment fragment = new SetAmountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public long mAmount;
    private long mMinAmount;
    private long mMaxAmount;
    private String mValidMinAmount;
    private String mValidMaxAmount;

    @Override
    protected void setupFragmentComponent() {
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_set_amount;
    }

    @BindView(R.id.textInputAmount)
    TextInputLayout textInputAmountView;

    @BindView(R.id.textInputMessage)
    TextInputLayout textInputMessageView;

    @BindView(R.id.btnUpdate)
    View mBtnContinueView;

    @OnTextChanged(R.id.edtAmount)
    public void onAmountChanged() {

    }

    @OnTextChanged(R.id.edtAmount)
    public void onTextChanged(CharSequence s) {
        mBtnContinueView.setEnabled(s.length() > 0);
    }

    @OnClick(R.id.btnUpdate)
    public void onClickUpdate() {
        if (!isValidAmount()) {
            textInputAmountView.requestFocus();
            return;
        } else {
            hideAmountError();
        }

        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putLong("amount", mAmount);
        EditText editText = textInputMessageView.getEditText();
        if (editText != null) {
            bundle.putString("message", editText.getText().toString());
        }
        data.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText editText = textInputAmountView.getEditText();
        if (editText != null) {
            editText.addTextChangedListener(new VNDCurrencyTextWatcher(editText) {
                @Override
                public void onValueUpdate(long value) {
                    Timber.d("onValueUpdate value [%s]", value);
                    mAmount = value;
                    if (isValidMaxAmount()) {
                        hideAmountError();
                    }

                }
            });
        }
        mBtnContinueView.setEnabled(false);
        initLimitAmount();
    }

    private void initLimitAmount() {
        try {
            mMinAmount = CShareData.getInstance().getMinTranferValue();
            mMaxAmount = CShareData.getInstance().getMaxTranferValue();
        } catch (Exception e) {
            Timber.w(e, "Get min/max deposit from paymentSDK exception: [%s]", e.getMessage());
        }
        if (mMinAmount <= 0) {
            mMinAmount = Constants.MIN_TRANSFER_MONEY;
        }
        if (mMaxAmount <= 0) {
            mMaxAmount = Constants.MAX_TRANSFER_MONEY;
        }
        mValidMinAmount = String.format(getContext().getString(R.string.min_money),
                CurrencyUtil.formatCurrency(mMinAmount, true));
        mValidMaxAmount = String.format(getContext().getString(R.string.max_money),
                CurrencyUtil.formatCurrency(mMaxAmount, true));
    }

    private boolean isValidMinAmount() {
        if (mAmount < mMinAmount) {
            showAmountError(mValidMinAmount);
            return false;
        }
        return true;
    }

    private boolean isValidMaxAmount() {
        if (mAmount > mMaxAmount) {
            showAmountError(mValidMaxAmount);
            return false;
        }
        return true;
    }

    private void showAmountError(String error) {
        if (textInputAmountView != null) {
            textInputAmountView.requestFocus();
            textInputAmountView.setError(error);
        }
    }

    private void hideAmountError() {
        if (textInputAmountView != null) {
            textInputAmountView.setError(null);
        }
    }

    private boolean isValidAmount() {
        return isValidMinAmount() && isValidMaxAmount();
    }

    @Override
    public void onDestroy() {
        CShareData.dispose();
        super.onDestroy();
    }
}
