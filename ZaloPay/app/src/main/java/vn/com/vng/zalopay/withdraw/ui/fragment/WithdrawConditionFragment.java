package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ScrollView;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawConditionPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;


/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link WithdrawConditionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WithdrawConditionFragment extends BaseFragment implements IWithdrawConditionView {

    @Inject
    WithdrawConditionPresenter mPresenter;


    CardSupportWithdrawFragment mCardSupportFragment;

    @OnClick(R.id.btn_link_card)
    public void onClickAddAccount() {
        navigator.startBankActivityFromWithdrawCondition(getContext());
    }

    @BindView(R.id.ScrollView)
    ScrollView mScrollView;

    public WithdrawConditionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WithdrawConditionFragment.
     */
    public static WithdrawConditionFragment newInstance() {
        return new WithdrawConditionFragment();
    }

    public void setFocusDown() {
        AndroidUtils.runOnUIThread(mScrollViewRunnable, 300);
    }

    private Runnable mScrollViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (mScrollView != null) {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        }
    };

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_withdraw_condition;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mCardSupportFragment = (CardSupportWithdrawFragment)
                getChildFragmentManager().findFragmentById(R.id.cardSupportWithdrawFragment);
        showLoading();
        setFocusDown();
    }

    @Override
    public void refreshListCardSupport(List<BankConfig> list) {
        Timber.d("refresh CardSupportList[%s]", list);
        hideLoading();
        if (mCardSupportFragment != null) {
            mCardSupportFragment.refreshCardSupportList(list);
        }
    }

    @Override
    public void showConfirmDialog(String message, ZPWOnEventConfirmDialogListener listener) {
        super.showConfirmDialog(message,
                getString(R.string.txt_retry),
                getString(R.string.txt_close),
                listener);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
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
}
