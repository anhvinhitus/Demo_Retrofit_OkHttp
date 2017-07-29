package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.ConfigLoader;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.MoneyEditText;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.withdraw.ui.adapter.WithdrawAdapter;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawView;
import vn.com.zalopay.utility.CurrencyUtil;


public class WithdrawFragment extends BaseFragment implements IWithdrawView, WithdrawAdapter.OnClickDenominationListener {

    private final static int SPAN_COUNT_APPLICATION = 2;
    private View mView;
    private MoneyEditText mEdtAmount;
    private Button btnContinue;
    private TextView mDescription;
    @BindView(R.id.listview)
    RecyclerView listview;

    @BindView(R.id.tvMoney)
    TextView mMoneyView;
    @Inject
    WithdrawPresenter mPresenter;
    WithdrawAdapter mAdapter;

    @BindView(R.id.tvEnoughMoney)
    View mEnoughView;

    @BindView(R.id.stub)
    ViewStub mViewStub;

    @BindView(R.id.background)
    SimpleDraweeView mBackgroundView;

    public static WithdrawFragment newInstance() {

        Bundle args = new Bundle();

        WithdrawFragment fragment = new WithdrawFragment();
        fragment.setArguments(args);
        return fragment;
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
        mAdapter = new WithdrawAdapter(this);
        mAdapter.setSpanCount(SPAN_COUNT_APPLICATION);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mViewStub.setEnabled(false);
        listview.setHasFixedSize(true);
        listview.addItemDecoration(new VerticalGridCardSpacingDecoration());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT_APPLICATION);
        gridLayoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        listview.setLayoutManager(gridLayoutManager);
        listview.setAdapter(mAdapter);
        mBackgroundView
                .getHierarchy()
                .setPlaceholderImageFocusPoint(new PointF(0.5f, 0f));
    }

    @Override
    public void onDestroyView() {
        if (mEdtAmount != null) {
            mEdtAmount.clearValidators();
        }
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.loadView();
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
    public void showInputError(String message) {
        if (mEdtAmount == null) {
            return;
        }
        mEdtAmount.setError(message);
        showKeyboard();
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void setBalance(long balance) {
        mMoneyView.setText(CurrencyUtil.formatCurrency(balance, false));
        mAdapter.setBalance(balance);
    }

    @Override
    public void setMinAmount(long minAmount) {
        mAdapter.setBalance(minAmount);
    }

    @Override
    public void addDenominationMoney(List<Long> val) {
        mAdapter.insertItems(val);
    }

    @Override
    public void finish(int result) {
        getActivity().setResult(result);
        getActivity().finish();
    }

    @Override
    public void onClickDenomination(long money) {
        mPresenter.withdraw(money);
    }

    @Override
    public void showEnoughView(boolean isShow) {
        if (mEnoughView != null) {
            mEnoughView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showVisibleStubView() {
        listview.setVisibility(View.INVISIBLE);
        if (mViewStub == null) {
            return;
        }
        if (mViewStub.getParent() != null) {
            mView = mViewStub.inflate();
        } else {
            mViewStub.setVisibility(View.VISIBLE);
        }
        mViewStub.setEnabled(true);
        initStubView();
    }

    @Override
    public void initStubView() {
        if (mView == null) {
            return;
        }
        mEdtAmount = (MoneyEditText) mView.findViewById(R.id.edtAmount);
        mDescription = (TextView) mView.findViewById(R.id.description);
        initLimitAmount();
        btnContinue = (Button) mView.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            if (mEdtAmount != null && mEdtAmount.validate()) {
                long mMoney = mEdtAmount.getAmount();
                mPresenter.withdraw(mMoney);
            }
        });

        mEdtAmount.addValidator(new ZPEditTextValidate(getString(R.string.valid_money)) {
            @Override
            public boolean isValid(@NonNull CharSequence s) {
                return mEdtAmount.getAmount() % 10000 == 0;
            }
        });
        btnContinue.setEnabled(mEdtAmount.isValid());
        mEdtAmount.addTextChangedListener(textWatcher);
        showKeyboard();
    }

    @Override
    public boolean onBackPressed() {
        hideKeyboard();
        if (mViewStub != null && mViewStub.isEnabled()) {
            mViewStub.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
            mViewStub.setEnabled(false);
            return true;
        }
        return super.onBackPressed();
    }

    private void initLimitAmount() {
        long minWithdrawAmount = mPresenter.getMinWithdrawAmount();
        long maxWithdrawAmount = mPresenter.getMaxWithdrawAmount();
        long multipleMoneyWithdraw = ConfigLoader.getMultipleMoneyWithdraw();
        String description = String.format(getString(R.string.withdraw_inputmoney_hint),
                CurrencyUtil.formatCurrency(minWithdrawAmount),
                CurrencyUtil.formatCurrency(multipleMoneyWithdraw));
        mDescription.setText(description);
        mEdtAmount.setMinMaxMoney(minWithdrawAmount, maxWithdrawAmount);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (btnContinue != null && mEdtAmount != null) {
                btnContinue.setEnabled(mEdtAmount.isValid());
            }
        }
    };

    public void showKeyboard() {
        AndroidUtils.runOnUIThread(mKeyboardRunnable, 250);
    }

    private Runnable mKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            if (mEdtAmount != null) {
                mEdtAmount.requestFocus();
                AndroidUtils.showKeyboard(mEdtAmount);
            }

        }
    };
}
