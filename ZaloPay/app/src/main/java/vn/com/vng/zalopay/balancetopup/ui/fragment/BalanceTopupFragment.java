package vn.com.vng.zalopay.balancetopup.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.vng.zalopay.utils.VNDCurrencyTextWatcher;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BalanceTopupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceTopupFragment extends BaseFragment implements IBalanceTopupView {
    // TODO: Rename parameter arguments, choose names that match
    private long mAmount = 0;
    private String mValidMinAmount = "";
    private String mValidMaxAmount = "";

    @Inject
    BalanceTopupPresenter balanceTopupPresenter;

    @BindView(R.id.tvResourceMoney)
    TextView tvResourceMoney;

    @BindView(R.id.textInputAmount)
    TextInputLayout textInputAmount;

    @BindView(R.id.edtAmount)
    EditText edtAmount;

    @BindView(R.id.btnDeposit)
    View btnDeposit;

    View.OnClickListener onClickDeposit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isValidAmount()) {
                return;
            }
            showProgressDialog();
            balanceTopupPresenter.deposit(mAmount);
        }
    };

    private void showAmountError(String error) {
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
        if (mAmount < Constants.MIN_DEPOSIT_MONEY) {
            showAmountError(mValidMinAmount);
            return false;
        }
        return true;
    }

    public boolean isValidMaxAmount() {
        if (mAmount > Constants.MAX_DEPOSIT_MONEY ) {
            showAmountError(mValidMaxAmount);
            return false;
        }
        return true;
    }

    public boolean isValidAmount() {
        if (!isValidMinAmount()) {
            return false;
        }

        if (!isValidMaxAmount()) {
            return false;
        }

        if (mAmount % 10000 != 0) {
            showAmountError(getString(R.string.valid_money));
            return false;
        }
        return true;
    }

    public BalanceTopupFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BalanceTopupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BalanceTopupFragment newInstance() {
        BalanceTopupFragment fragment = new BalanceTopupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_balance_topup;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mValidMinAmount = String.format(getResources().getString(R.string.min_money),
                CurrencyUtil.formatCurrency(Constants.MIN_DEPOSIT_MONEY, true));
        mValidMaxAmount = String.format(getResources().getString(R.string.max_money),
                CurrencyUtil.formatCurrency(Constants.MAX_DEPOSIT_MONEY, true));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        balanceTopupPresenter.setView(this);
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
                CurrencyUtil.formatCurrency(Constants.MIN_DEPOSIT_MONEY, false)));
    }

    private void checkShowBtnContinue() {
        if (mAmount <= 0) {
            btnDeposit.setBackgroundResource(R.color.bg_btn_gray);
            btnDeposit.setOnClickListener(null);
        } else {
            btnDeposit.setBackgroundResource(R.drawable.bg_btn_blue);
            btnDeposit.setOnClickListener(onClickDeposit);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        balanceTopupPresenter.destroyView();
    }

    @Override
    public void onDestroy() {
        balanceTopupPresenter.destroy();
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
        ToastUtil.showToast(getActivity(), message);
    }

    @Override
    public void onTokenInvalid() {
    }
}
