package vn.com.vng.zalopay.ui.fragment;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.BalanceManagementPresenter;
import vn.com.vng.zalopay.ui.view.IBalanceManagementView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.utility.CurrencyUtil;
import vn.com.vng.zalopay.utils.DialogHelper;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BalanceManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceManagementFragment extends BaseFragment implements IBalanceManagementView {

    @Inject
    BalanceManagementPresenter mPresenter;
    @BindView(R.id.tv_balance)
    TextView mTvBalance;
    @BindView(R.id.tvAccountName)
    TextView mTvAccountName;
    @BindView(R.id.balance_management_rl_deposit)
    View mViewDeposit;
    @BindView(R.id.viewSeparate)
    View mViewSeparate;
    @BindView(R.id.background)
    SimpleDraweeView mBackgroundView;

    public static BalanceManagementFragment newInstance() {
        return new BalanceManagementFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_balance_management;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mBackgroundView
                .getHierarchy()
                .setPlaceholderImageFocusPoint(new PointF(0.5f, 0f));
        mPresenter.loadView();
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
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @OnClick(R.id.balance_management_rl_deposit)
    public void onClickDeposit() {
        navigator.startDepositActivity(getContext());

    }

    @OnClick(R.id.balance_management_rl_withdraw)
    public void onClickWithdraw() {
        mPresenter.startWithdrawActivity();
    }

    @OnClick(R.id.tvAccountName)
    public void onClickAccountName() {
        mPresenter.updateZaloPayID();
    }

    @Override
    public void showTopup(boolean isEnableDeposit) {
        Timber.d("showTopup: [%s]", isEnableDeposit);
        mViewDeposit.setVisibility(isEnableDeposit ? View.VISIBLE : View.GONE);
        mViewSeparate.setVisibility(isEnableDeposit ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBalance(long balance) {
        if (mTvBalance != null) {
            mTvBalance.setText(CurrencyUtil.formatCurrency(balance, false));
        }
    }

    @Override
    public void setUser(User user) {
        if (mTvAccountName == null) {
            return;
        }

        String name = TextUtils.isEmpty(user.zalopayname) ? getString(R.string.not_update) : user.zalopayname;

        String accountLabel = String.format(getString(R.string.zalopayid_not_update_format), name);

        if (TextUtils.isEmpty(user.zalopayname)) {
            mTvAccountName.setHint(accountLabel);
            mTvAccountName.setClickable(true);
            mTvAccountName.setCompoundDrawablesWithIntrinsicBounds(null, null, AndroidUtils.getTintedDrawable(getContext(), R.drawable.ic_arrow_right, R.color.blue_008fe5), null);
        } else {
            mTvAccountName.setText(accountLabel);
            mTvAccountName.setClickable(false);
            mTvAccountName.setCompoundDrawables(null, null, null, null);
        }
    }

    @Override
    public void showConfirmDialog(String message,
                                  String btnConfirm,
                                  String btnCancel,
                                  ZPWOnEventConfirmDialogListener listener) {
        DialogHelper.showNoticeDialog(getActivity(), message, btnConfirm, btnCancel, listener);
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
        super.showErrorDialog(message);
    }
}
