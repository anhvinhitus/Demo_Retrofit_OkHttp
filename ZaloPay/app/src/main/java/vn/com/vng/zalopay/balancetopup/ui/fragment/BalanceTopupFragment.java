package vn.com.vng.zalopay.balancetopup.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.widget.MoneyEditText;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.utility.CurrencyUtil;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BalanceTopupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceTopupFragment extends BaseFragment implements IBalanceTopupView {

    @Inject
    BalanceTopupPresenter mPresenter;
    @BindView(R.id.tvResourceMoney)
    TextView tvResourceMoney;
    @BindView(R.id.edtAmount)
    MoneyEditText mEdtAmountView;
    @BindView(R.id.btnDeposit)
    View mBtnDepositView;

    public static BalanceTopupFragment newInstance(Bundle bundle) {
        BalanceTopupFragment fragment = new BalanceTopupFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_balance_topup;
    }

    @OnClick(R.id.btnDeposit)
    public void onClickBtnDeposit() {
        if (!mEdtAmountView.validate()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_ADDCASH_INPUT);
            return;
        }

        mPresenter.deposit(mEdtAmountView.getAmount());
        ZPAnalytics.trackEvent(ZPEvents.BALANCE_ADDCASH_CONTINUE);
    }

    @OnTextChanged(value = R.id.edtAmount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAmountChanged(CharSequence s) {
        mBtnDepositView.setEnabled(mEdtAmountView.isValid());
    }

    private void initLimitAmount() {

        AppInfoStore.Interactor appInfo = SDKApplication.getApplicationComponent().appInfoInteractor();
        long minDepositAmount = appInfo.minAmountTransType(TransactionType.TOPUP);
        long maxDepositAmount = appInfo.maxAmountTransType(TransactionType.TOPUP);

        if (minDepositAmount <= 0) {
            minDepositAmount = Constants.MIN_DEPOSIT_MONEY;
        }
        if (maxDepositAmount <= 0) {
            maxDepositAmount = Constants.MAX_DEPOSIT_MONEY;
        }

        mEdtAmountView.setMinMaxMoney(minDepositAmount, maxDepositAmount);

        tvResourceMoney.setText(String.format(getResources().getString(R.string.title_min_money),
                CurrencyUtil.formatCurrency(minDepositAmount, false)));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        initLimitAmount();
        mEdtAmountView.addValidator(new ZPEditTextValidate(getString(R.string.valid_money)) {
            @Override
            public boolean isValid(@NonNull CharSequence s) {
                return mEdtAmountView.getAmount() % 10000 == 0;
            }
        });
        mEdtAmountView.setClearTextListener(this::showKeyboard);

        mBtnDepositView.setEnabled(mEdtAmountView.isValid());
    }

    @Override
    public void onDestroyView() {
        mEdtAmountView.clearValidators();
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        ZPAnalytics.trackEvent(ZPEvents.BALANCE_ADDCASH_TOUCH_BACK);
        return super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        CShareDataWrapper.dispose();
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
    public void showError(String message) {
        showErrorDialog(message);
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void showKeyboard() {
        AndroidUtils.runOnUIThread(() -> {
            if (!AndroidUtils.isKeyboardShowed(mEdtAmountView))
                AndroidUtils.showKeyboard(mEdtAmountView);
        }, 250);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
