package vn.com.vng.zalopay.ui.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.ServerMaintainEvent;
import vn.com.vng.zalopay.data.eventbus.TokenExpiredEvent;
import vn.com.vng.zalopay.data.exception.AccountSuspendedException;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.linkcard.ui.LinkCardActivity;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.ui.ReceiveMoneyActivity;
import vn.com.vng.zalopay.transfer.ui.TransferHomeActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;


/**
 * Created by AnhHieu on 3/24/16.
 * *
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupActivityComponent() {
    }

    public abstract BaseFragment getFragmentToHost();

    protected final String TAG = getClass().getSimpleName();

    private Unbinder unbinder;

    protected final EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();

    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate [%s]", TAG);
        createUserComponent();
        setupActivityComponent();
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

    private void createUserComponent() {

        Timber.d(" user component %s", getUserComponent());

        if (getUserComponent() != null)
            return;

        UserConfig userConfig = getAppComponent().userConfig();
        Timber.d(" mUserConfig %s", userConfig.isSignIn());
        if (userConfig.isSignIn()) {
            userConfig.loadConfig();
            AndroidApplication.instance().createUserComponent(userConfig.getCurrentUser());
        }
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
        eventBus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ZPAnalytics.trackScreen(TAG);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        Timber.d("onDestroy [%s]", TAG);
    }


    @Override
    public void onBackPressed() {
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

    public void showToastLonger(String message) {
        ToastUtil.showToast(this, message, Toast.LENGTH_LONG);
    }

    public void showToast(int message) {
        ToastUtil.showToast(this, message);
    }

    public ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpired(TokenExpiredEvent event) {
        Timber.i("SESSION EXPIRED in Screen %s", TAG);
        boolean result = clearUserSession(getString(R.string.exception_token_expired_message));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerMaintain(ServerMaintainEvent event) {
        Timber.i("Receive server maintain event");
        String eventMessage = TextUtils.isEmpty(event.getMessage()) ?
                getString(R.string.exception_server_maintain) : event.getMessage();

        boolean result = clearUserSession(eventMessage);
        if (!result) {
            showCustomDialog(eventMessage,
                    getString(R.string.txt_close),
                    SweetAlertDialog.NORMAL_TYPE,
                    null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountSuspended(AccountSuspendedException event) {
        Timber.i("Receive server maintain event");

        boolean result = clearUserSession(getString(R.string.exception_zpw_account_suspended));

        if (!result) {
            showWarningDialog(getString(R.string.exception_zpw_account_suspended), null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onTokenPaymentExpired(TokenPaymentExpiredEvent event) {
        Timber.i("SESSION EXPIRED in Screen %s", TAG);
        boolean result = clearUserSession(getString(R.string.exception_token_expired_message));
    }

    public boolean clearUserSession(String message) {
        if (TAG.equals(LoginZaloActivity.class.getSimpleName())) {
            return false;
        }

        //Remove all sticky event in app
        eventBus.removeAllStickyEvents();

        if (getUserComponent() != null) {
            getUserComponent().userSession().endSession();
        }

        getAppComponent().applicationSession().setMessageAtLogin(message);
        getAppComponent().applicationSession().clearUserSession();
        return true;
    }

    private void logActionLaunch() {

        if (TAG.equals(LinkCardActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_LAUNCH);
        } else if (TAG.equals(BalanceTopupActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.ADDCASH_LAUNCH);
        } else if (TAG.equals(TransferHomeActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_LAUNCH);
        } else if (TAG.equals(UpdateProfileLevel2Activity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.UPDATEPROFILE2_LAUNCH);
        } else if (TAG.equals(ReceiveMoneyActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_LAUNCH);
        }
    }

    private void logActionNavigationBack() {
        if (TAG.equals(LinkCardActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.MANAGECARD_NAVIGATEBACK);
        } else if (TAG.equals(BalanceTopupActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.ADDCASH_NAVIGATEBACK);
        } else if (TAG.equals(TransferHomeActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_NAVIGATEBACK);
        } else if (TAG.equals(UpdateProfileLevel2Activity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.UPDATEPROFILE2_NAVIGATEBACK);
        } else if (TAG.equals(ReceiveMoneyActivity.class.getSimpleName())) {
            ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_BACK);
        }
    }

    public void showNetworkErrorDialog() {
        showNetworkErrorDialog(null);
    }

    public void showNetworkErrorDialog(ZPWOnSweetDialogListener listener) {
        DialogHelper.showNetworkErrorDialog(getActivity(), listener);
    }

    public void showCustomDialog(String message,
                                 String cancelBtnText,
                                 int dialogType,
                                 final ZPWOnEventDialogListener listener) {
        DialogHelper.showCustomDialog(getActivity(),
                message,
                cancelBtnText,
                dialogType,
                listener);
    }

    public void showWarningDialog(String message,
                                  final ZPWOnEventDialogListener cancelListener) {
        DialogHelper.showWarningDialog(getActivity(),
                message,
                cancelListener);
    }

    public void showErrorDialog(String message) {
        DialogHelper.showNotificationDialog(getActivity(), message, null);
    }
}
