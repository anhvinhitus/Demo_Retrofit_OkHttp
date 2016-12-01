package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.VNDCurrencyTextWatcher;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawView;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link WithdrawFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WithdrawFragment extends BaseFragment implements IWithdrawView {

    private long mAmount = 0;
    private long minWithdrawAmount;
    private long maxWithdrawAmount;
    private String mValidMinAmount = "";
    private String mValidMaxAmount = "";

    @Inject
    WithdrawPresenter mPresenter;

    @BindView(R.id.tvResourceMoney)
    TextView tvResourceMoney;

    @BindView(R.id.textInputAmount)
    TextInputLayout textInputAmount;

    @BindView(R.id.edtAmount)
    EditText edtAmount;

    @BindView(R.id.btnContinue)
    View btnContinue;

    @OnClick(R.id.btnContinue)
    public void setOnClickContinue(){
        if (!isValidAmount()) {
            return;
        }
        mPresenter.continueWithdraw(mAmount);
    }

    @Override
    public void showAmountError(String error) {
        if (!TextUtils.isEmpty(error)) {
            textInputAmount.setErrorEnabled(true);
            textInputAmount.setError(error);
        } else {
            hideAmountError();
        }
    }

    private void hideAmountError() {
        textInputAmount.setErrorEnabled(false);
        textInputAmount.setError(null);
    }

    public boolean isValidMinAmount() {
        if (mAmount < minWithdrawAmount) {
            showAmountError(mValidMinAmount);
            return false;
        }
        return true;
    }

    public boolean isValidMaxAmount() {
        if (mAmount > maxWithdrawAmount) {
            showAmountError(mValidMaxAmount);
            return false;
        }
        return true;
    }

    public boolean isValidAmount() {
        if (!isValidMinAmount()) {
            return false;
        }

        return isValidMaxAmount();

    }

    public WithdrawFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WithdrawFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WithdrawFragment newInstance() {
        return new WithdrawFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_withdraw;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLimitAmount();
    }

    private void initLimitAmount() {
        try {
            minWithdrawAmount = CShareData.getInstance().getMinWithDrawValue();
            maxWithdrawAmount = CShareData.getInstance().getMaxWithDrawValue();
        } catch (Exception e) {
            Timber.w(e, "Get min/max withdraw from paymentSDK exception: [%s]", e.getMessage());
        }
        if (minWithdrawAmount <= 0) {
            minWithdrawAmount = Constants.MIN_WITHDRAW_MONEY;
        }
        if (maxWithdrawAmount <= 0) {
            maxWithdrawAmount = Constants.MAX_WITHDRAW_MONEY;
        }
        mValidMinAmount = String.format(getResources().getString(R.string.min_money),
                CurrencyUtil.formatCurrency(minWithdrawAmount, true));
        mValidMaxAmount = String.format(getResources().getString(R.string.max_money),
                CurrencyUtil.formatCurrency(maxWithdrawAmount, true));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.setView(this);
        edtAmount.requestFocus();
        edtAmount.addTextChangedListener(new VNDCurrencyTextWatcher(edtAmount) {
            @Override
            public void onValueUpdate(long value) {
                mAmount = value;
            }

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                hideAmountError();
                isValidMaxAmount();
                checkShowBtnContinue();
            }
        });
        tvResourceMoney.setText(String.format(getResources().getString(R.string.title_min_money),
                CurrencyUtil.formatCurrency(minWithdrawAmount, false)));
    }

    private void checkShowBtnContinue() {
        btnContinue.setEnabled(mAmount > 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        CShareData.dispose();
        super.onDestroy();
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        showErrorDialog(message);
    }

    @Override
    public void onTokenInvalid() {

    }
}
