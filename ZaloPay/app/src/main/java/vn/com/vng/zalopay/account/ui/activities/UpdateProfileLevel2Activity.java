package vn.com.vng.zalopay.account.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.viewpager.NonSwipeableViewPager;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnPageChange;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.adapter.ProfileSlidePagerAdapter;
import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPreProfileView;
import vn.com.vng.zalopay.event.ReceiveOTPEvent;
import vn.com.vng.zalopay.event.RefreshPaymentSdkEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.widget.FragmentLifecycle;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

public class UpdateProfileLevel2Activity extends UserBaseToolBarActivity
        implements IPreProfileView,
        PinProfileFragment.OnPinProfileFragmentListener,
        OtpProfileFragment.OnOTPFragmentListener {

    private boolean mLinkAccAfterUpdateProfile2 = false;
    private SweetAlertDialog mProgressDialog;
    private String mCurrentPhone = null;
    private ProfileSlidePagerAdapter mAdapter;
    private int mCurrentPage;

    @Inject
    PreProfilePresenter presenter;

    @BindView(R.id.viewPager)
    NonSwipeableViewPager mViewPager;

    @Override
    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {
        userComponent.inject(this);
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_update_profile_level2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.attachView(this);
        initData();
    }

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();

        if (bundle == null) {
            return;
        }
        mLinkAccAfterUpdateProfile2 = bundle.getBoolean(Constants.ARG_UPDATE_PROFILE2_AND_LINK_ACC);
    }

    @Override
    public void initPagerContent(int pageIndex) {
        mAdapter = new ProfileSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        if (pageIndex >= 0 && pageIndex < mViewPager.getAdapter().getCount()) {
            mViewPager.setCurrentItem(pageIndex);
        }

    }

    @OnPageChange(value = R.id.viewPager, callback = OnPageChange.Callback.PAGE_SELECTED)
    public void onPageSelected(int newPosition) {

        Fragment fragmentToShow = mAdapter.getPage(newPosition);
        if (fragmentToShow instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToShow).onStartFragment();
        }

        Fragment fragmentToHide = mAdapter.getPage(mCurrentPage);
        if (fragmentToHide instanceof FragmentLifecycle) {
            ((FragmentLifecycle) fragmentToHide).onStopFragment();
        }

        mCurrentPage = newPosition;
    }


    public void nextPager() {
        if (mViewPager == null)
            return;
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    protected void onDestroy() {
        if (isUserSessionStarted()) {
            presenter.detachView();
            presenter.destroy();
            hideLoading();
            eventBus.removeStickyEvent(ReceiveOTPEvent.class);
        }

        super.onDestroy();
    }

    @Override
    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }


    @Override
    public void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showError(String message) {
        super.showToast(message);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onUpdatePinSuccess(String phone) {
        mCurrentPhone = phone;
        nextPager();
    }

    @Override
    public void updateCurrentPhone(String phone) {
        mCurrentPhone = phone;
    }

    @Override
    public void onConfirmOTPSuccess() {
        Timber.d("onConfirmOTPSucess");
        presenter.saveUserPhone(mCurrentPhone);
        //Reload PaymentSDK for load new payment permission
        EventBus.getDefault().post(new RefreshPaymentSdkEvent());
        if (mLinkAccAfterUpdateProfile2) {
            showDialogConfirmLinkAccToContinuePay();
        } else if (getActivity() != null && !getActivity().isFinishing()) {
            showToastUpdateProfileSuccess();
            setResult(RESULT_OK);
            getActivity().finish();
        }
    }

    private void showToastUpdateProfileSuccess() {
        showToast(getString(R.string.update_profile2_success));
    }

    private void showDialogConfirmLinkAccToContinuePay() {
        DialogHelper.showNoticeDialog(this, getString(R.string.confirm_link_card_to_continue_pay),
                getString(R.string.btn_link),
                getString(R.string.btn_cancel_transaction),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        finishActivityWithResult(Constants.RESULT_END_PAYMENT);
                    }

                    @Override
                    public void onOKevent() {
                        finishActivityWithResult(RESULT_OK);
                    }
                });
    }

    private void finishActivityWithResult(int result) {
        if (getActivity() == null) {
            return;
        }
        setResult(result);
        getActivity().finish();
    }

    @Override
    public void onBackPressed() {
        if (mViewPager == null || mAdapter == null) {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
            return;
        }

        if (mViewPager.getCurrentItem() == 0) {
            Fragment fragment = mAdapter.getPage(0);
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).onBackPressed();
            }

            setResult(RESULT_CANCELED);
            super.onBackPressed();
        } else {
            if (mViewPager.getCurrentItem() > 0
                    && mViewPager.getCurrentItem() < mAdapter.getCount()) {
                Fragment fragment = mAdapter.getPage(mViewPager.getCurrentItem());
                if (fragment instanceof BaseFragment) {
                    ((BaseFragment) fragment).onBackPressed();
                }
            }
            ZPAnalytics.trackEvent(ZPEvents.OTP_LEVEL2_INPUTNONE);
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String otp = intent.getStringExtra("otp");

        if (!TextUtils.isEmpty(otp)) {
            eventBus.postSticky(new ReceiveOTPEvent(otp));
        }

    }

    @Override
    public Activity getActivity() {
        return this;
    }
}
