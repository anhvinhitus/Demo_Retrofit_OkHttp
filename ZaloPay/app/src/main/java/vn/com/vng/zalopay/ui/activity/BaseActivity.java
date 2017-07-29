package vn.com.vng.zalopay.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.activities.ProfileActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel3Activity;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.bank.ui.BankActivity;
import vn.com.vng.zalopay.bank.ui.BankSupportSelectionActivity;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.protect.ui.ProtectAccountActivity;
import vn.com.vng.zalopay.transfer.ui.ReceiveMoneyActivity;
import vn.com.vng.zalopay.transfer.ui.SetAmountActivity;
import vn.com.vng.zalopay.transfer.ui.TransferHomeActivity;
import vn.com.vng.zalopay.transfer.ui.TransferViaZaloPayNameActivity;
import vn.com.vng.zalopay.transfer.ui.ZaloContactActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.vng.zalopay.webapp.WebAppPromotionActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;


/**
 * Created by AnhHieu on 3/24/16.
 * *
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupActivityComponent(ApplicationComponent applicationComponent) {
    }

    @Nullable
    protected abstract BaseFragment getFragmentToHost();

    @NonNull
    protected abstract String getTrackingScreenName();

    protected final String TAG = getClass().getSimpleName();
    protected final EventBus mEventBus = getAppComponent().eventBus();
    protected final Navigator navigator = getAppComponent().navigator();

    private Unbinder unbinder;
    private boolean mResumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityComponent(getAppComponent());
        setContentView(getResLayoutId());

        if (savedInstanceState == null) {
            hostFragment(getFragmentToHost());
        }

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.logActionLaunch();
    }

    protected int getResLayoutId() {
        return R.layout.activity_common;
    }

    protected void hostFragment(BaseFragment fragment, int id) {
        if (fragment != null && getFragmentManager().findFragmentByTag(fragment.getTag()) == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(id, fragment, fragment.TAG);
            ft.commit();
        }
    }

    protected void hostFragment(BaseFragment fragment) {
        hostFragment(fragment, R.id.fragment_container);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        unbinder = ButterKnife.bind(this);
    }


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mResumed = true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        String screenName = getTrackingScreenName();
        if (!TextUtils.isEmpty(screenName)) {
            ZPAnalytics.trackScreen(screenName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (!mResumed) {
            return;
        }

        Fragment activeFragment = getActiveFragment();
        if (activeFragment instanceof BaseFragment) {
            if (((BaseFragment) activeFragment).onBackPressed()) {
                return;
            }
        }

        this.logActionNavigationBack();
        super.onBackPressed();
    }

    protected Fragment getActiveFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    public void showToast(String message) {
        ToastUtil.showToast(this, message);
    }

    public void showToast(int message) {
        ToastUtil.showToast(this, message);
    }

    protected ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    private void logActionLaunch() {

        if (TAG.equals(BankActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.LINKBANK_LAUNCH);
        } else if (TAG.equals(BalanceTopupActivity.class.getSimpleName())) {
          //  ZPAnalytics.trackEvent(ZPEvents.ADDCASH_LAUNCH);
        } else if (TAG.equals(TransferHomeActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_LAUNCH);
        } else if (TAG.equals(ReceiveMoneyActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_LAUNCH);
        } else if (TAG.equals(BankSupportSelectionActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.LINKBANK_ADD_LAUNCH);
        }
    }

    private void logActionNavigationBack() {
        if (TAG.equals(BankActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.LINKBANK_TOUCH_BACK);
        } else if (TAG.equals(BalanceTopupActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_TOUCH_BACK);
        } else if (TAG.equals(TransferHomeActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_TOUCH_BACK);
        } else if (TAG.equals(ReceiveMoneyActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_TOUCH_BACK);
        } else if (TAG.equals(TransferViaZaloPayNameActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_ZPID_TOUCH_BACK);
        } else if (TAG.equals(ZaloContactActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_ZFRIEND_TOUCH_BACK);
        } else if (TAG.equals(SetAmountActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_SETAMOUNT_BACK);
        }else if (TAG.equals(WebAppPromotionActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.PROMOTION_DETAIL_TOUCH_BACK);
        }else if (TAG.equals(ProtectAccountActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.ME_SECURITY_TOUCH_BACK);
        }else if (TAG.equals(ProfileActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.ME_PROFILE_ZPID_TOUCH_BACK);
        }else if (TAG.equals(UpdateProfileLevel3Activity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.ME_PROFILE_IDENTITY_BACK);
        }


    }

    public void showNetworkErrorDialog() {
        showNetworkErrorDialog(null);
    }

    public void showNetworkErrorDialog(ZPWOnSweetDialogListener listener) {
        DialogHelper.showNetworkErrorDialog(this, listener);
    }

    public void showCustomDialog(String message,
                                 String cancelBtnText,
                                 int dialogType,
                                 final ZPWOnEventDialogListener listener) {
        DialogHelper.showCustomDialog(this,
                message,
                cancelBtnText,
                dialogType,
                listener);
    }

    public void showNotificationDialog(String message) {
        DialogHelper.showNotificationDialog(this,
                message);
    }

    public void showWarningDialog(String message,
                                  final ZPWOnEventDialogListener cancelListener) {
        DialogHelper.showWarningDialog(this,
                message,
                cancelListener);
    }

    public void showErrorDialog(String message) {
        DialogHelper.showNotificationDialog(this, message, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Fragment activeFragment = getActiveFragment();
        if (activeFragment instanceof BaseFragment) {
            ((BaseFragment) activeFragment).onNewIntent(intent);
        }
    }
}
