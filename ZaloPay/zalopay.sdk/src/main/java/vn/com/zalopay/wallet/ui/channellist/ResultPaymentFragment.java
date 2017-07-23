package vn.com.zalopay.wallet.ui.channellist;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.listener.onNetworkingDialogCloseListener;
import vn.com.zalopay.wallet.ui.BaseFragment;
import vn.com.zalopay.wallet.ui.IContract;
import vn.com.zalopay.wallet.view.custom.overscroll.OverScrollDecoratorHelper;

/*
 * Created by chucvv on 7/23/17.
 */

public class ResultPaymentFragment extends AbstractPaymentFragment<ResultPaymentPresenter> implements IContract {

    StatusResponse mResponse;

    public static BaseFragment newInstance() {
        return new ResultPaymentFragment();
    }

    public static BaseFragment newInstance(Bundle args) {
        BaseFragment fragment = newInstance();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public boolean onBackPressed() {
        return mPresenter.onBackPressed();
    }

    @Override
    public void onPaymentButtonClick() {
        mPresenter.onPaymentButtonClick();
    }

    @Override
    public void onStartFeedbackSupport() {
        try {
            mPresenter.showFeedbackDialog();
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_result;
    }

    @Override
    protected void onViewBound(View view) {
        super.onViewBound(view);
        mRootView = view;
        ScrollView mScrollViewRoot = (ScrollView) view.findViewById(R.id.zpw_scrollview_container);
        if (mScrollViewRoot != null) {
            OverScrollDecoratorHelper.setUpOverScroll(mScrollViewRoot);
        }
        findViewById(R.id.zpsdk_btn_submit).setOnClickListener(mPaymentButtonClick);
        findViewById(R.id.zpw_payment_fail_rl_support).setOnClickListener(mSupportViewClick);
    }

    @Override
    protected void onDataBound(View view) {
        super.onViewBound(view);
        mPresenter.showResultPayment(mResponse);
    }

    @Override
    protected void onArguments() {
        super.onArguments();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mResponse = bundle.getParcelable(Constants.STATUS_RESPONSE);
        }
    }

    @Override
    protected ResultPaymentPresenter initializePresenter() {
        try {
            return new ResultPaymentPresenter();
        } catch (Exception e) {
            Timber.w(e, "Exception initialize presenter");
        }
        return null;
    }

    @Override
    public void setTitle(String pTitle) {
        if (getActivity() != null) {
            ((ChannelListActivity) getActivity()).setToolbarTitle(pTitle);
        }
        Timber.d("set title %s", pTitle);
    }

    @Override
    public void showLoading(String pTitle) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String pMessage) {

    }

    @Override
    public void showInfoDialog(String pMessage) {

    }

    @Override
    public void showInfoDialog(String pMessage, ZPWOnEventDialogListener zpwOnEventDialogListener) {

    }

    @Override
    public void showRetryDialog(String pMessage, ZPWOnEventConfirmDialogListener pListener) {

    }

    @Override
    public void showOpenSettingNetwokingDialog(onNetworkingDialogCloseListener pListener) {

    }

    @Override
    public void showSnackBar(String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener) {

    }

    @Override
    public void terminate() {
        Timber.d("recycle activity");
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void callbackThenTerminate() {

    }
}
