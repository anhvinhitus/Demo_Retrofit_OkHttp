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
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.zalopay.analytics.ZPAnalytics;


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

    protected void getTrackingEventBack() {
    }

    protected void getTrackingEventLaunch() {
    }

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
        this.getTrackingEventLaunch();
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

        this.getTrackingEventBack();
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
