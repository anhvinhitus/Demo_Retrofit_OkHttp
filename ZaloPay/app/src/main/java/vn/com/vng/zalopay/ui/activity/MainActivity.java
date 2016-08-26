package vn.com.vng.zalopay.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;

import com.zalopay.apploader.internal.ModuleName;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.menu.utils.MenuItemUtil;
import vn.com.vng.zalopay.notification.ZPNotificationService;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.ui.callback.MenuClickListener;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.LeftMenuFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;
import vn.com.vng.zalopay.ui.presenter.MainPresenter;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.data.GlobalData;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 5/24/16.
 * Main Application activity
 */
public class MainActivity extends BaseToolBarActivity implements MenuClickListener, IHomeView {


    //private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected void setupActivityComponent() {
        getUserComponent().inject(this);
    }

    public MainActivity() {
    }

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @Inject
    MainPresenter presenter;

    @Inject
    GlobalEventHandlingService globalEventHandlingService;

    private int mCurrentMenuId;
    private long back_pressed;

    private ActionBarDrawerToggle toggle;
    private SweetAlertDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.setView(this);
        presenter.initialize();

        if (getIntent() != null) {
            this.mCurrentMenuId = getIntent().getIntExtra("menuId", MenuItemUtil.HOME_ID);
        } else {
            this.mCurrentMenuId = MenuItemUtil.HOME_ID;
        }

        if (savedInstanceState == null) {
            FragmentTransaction add = getSupportFragmentManager().beginTransaction().add(R.id.menu, LeftMenuFragment.newInstance(), "MenuFragment");
            switch (this.mCurrentMenuId) {
                case MenuItemUtil.HOME_ID:
                    add.add(R.id.container, ZaloPayFragment.newInstance(), "ZaloPayFragment");
                    break;
                case MenuItemUtil.ACCOUNT_ID:
                    break;
            }
            add.commit();
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toggle = new ActionBarDrawerToggle(
                this, drawer, getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        startZaloPayService();
        presenter.getZaloFriend();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.d("onNewIntent");
        if (intent != null) {
            int menuId = intent.getIntExtra("menuId", -1);
            if (menuId >= 0) {
                //Todo:
            }
        }
    }

    @Override
    protected void onDestroy() {
        Timber.d("destroy main activity");

        drawer.removeDrawerListener(toggle);
        presenter.destroyView();
        GlobalData.initApplication(null);
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.toggle.onConfigurationChanged(newConfig);
    }

    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putInt("menuId", this.mCurrentMenuId);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onMenuItemClick(int id) {
        replaceFragmentDelay(id);
    }

    @Override
    public void onClickProfile() {
//        navigator.startUpdateProfileLevel2Activity(this, false);
        navigator.startProfileInfoActivity(this);
    }

    public void replaceFragmentDelay(final int id) {
        this.drawer.closeDrawer(Gravity.LEFT);
        new Handler().postDelayed(new OpenMenuRunnable(this, id), 300);
    }

    public void replaceFragmentImmediate(final int id) {

        Timber.d(TAG, "replaceFragmentImmediate  ", id);

        switch (id) {
            case MenuItemUtil.ACCOUNT_ID:
                if (id != mCurrentMenuId) {
                    mCurrentMenuId = id;
                    // getSupportFragmentManager().beginTransaction().replace(R.id.container, new uy(), "AccountFragment").commit();
                }
                break;
            case MenuItemUtil.APPLICATION_INFO_ID:
                navigator.startMiniAppActivity(this, ModuleName.ABOUT);
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUABOUT);
                break;
            case MenuItemUtil.CONTACT_SUPPORT_ID:
                navigator.startMiniAppActivity(this, ModuleName.HELP);
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUHELP);
                break;
            case MenuItemUtil.DEPOSIT_ID:
                navigator.startDepositActivity(this);
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUADDCASH);
                break;
            case MenuItemUtil.FAQ_ID:
                navigator.startMiniAppActivity(this, ModuleName.FAQ);
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUFAQ);
                break;
            case MenuItemUtil.HOME_ID:
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUHOME);
                break;
            case MenuItemUtil.NOTIFICATION_ID:
                navigator.startMiniAppActivity(this, ModuleName.NOTIFICATIONS);
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUNOTIFICATION);
                break;
            case MenuItemUtil.SCAN_QR_ID:
                // startQRCodeActivity();
                navigator.startScanToPayActivity(getActivity());
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUSCANQR);
                break;
            case MenuItemUtil.SIGN_OUT_ID:
                showConfirmSignOut();
                break;
            case MenuItemUtil.TRANSACTION_HISTORY_ID:
                navigator.startMiniAppActivity(this, ModuleName.TRANSACTION_LOGS);
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUTRANSACTIONLOGS);
                break;
            case MenuItemUtil.TRANSFER_ID:
                navigator.startTransferMoneyActivity(this);
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUTRANSFERMONEY);
                break;
            case MenuItemUtil.SAVE_CARD_ID:
                navigator.startLinkCardActivity(this);
                ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENUADDCARD);
                break;

            case MenuItemUtil.RECEIVE_MONEY:
                navigator.startMyQrCode(this);
                break;

        }
    }

    private void showConfirmSignOut() {
        new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog)
                .setContentText(getString(R.string.txt_confirm_sigout))
                .setCancelText(getString(R.string.cancel))
                .setConfirmText(getString(R.string.accept))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        presenter.logout();
                        ZPAnalytics.trackEvent(ZPEvents.TAPLEFTMENULOGOUT);
                    }
                })
                .show();
    }

    private void startQRCodeActivity() {
        if (checkAndRequestPermission(Manifest.permission.CAMERA, 100)) {
            navigator.startQrCodeActivity(this);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    private final class OpenMenuRunnable implements Runnable {
        final int id;
        final WeakReference<MainActivity> act;

        OpenMenuRunnable(MainActivity mainMenuActivity, int id) {
            this.act = new WeakReference<>(mainMenuActivity);
            this.id = id;
        }

        public final void run() {
            MainActivity mainActivity = act.get();
            if (mainActivity != null) {
                mainActivity.replaceFragmentImmediate(id);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (back_pressed + 2000 > System.currentTimeMillis()) {
            finish();
            System.exit(0);
        } else {
            showToast(R.string.pressed_back_to_exit);
            back_pressed = System.currentTimeMillis();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navigator.startQrCodeActivity(this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startZaloPayService() {
/*
        checkPlayServices();*/
        Intent intent = new Intent(this, ZPNotificationService.class);
        startService(intent);
    }

 /*   */

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     *//*
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Timber.d("This device is not supported.");
            }
            return false;
        }
        return true;
    }
*/
    @Override
    public void onPause() {
        Timber.i("MainActivity is pausing");
        presenter.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        Timber.i("MainActivity is resuming");
        super.onResume();
        presenter.resume();
        GlobalEventHandlingService.Message message = globalEventHandlingService.popMessage();
        if (message == null) {
            return;
        }

        SweetAlertDialog alertDialog = new SweetAlertDialog(getContext(), message.messageType, R.style.alert_dialog);
        alertDialog.setConfirmText(message.title);
        alertDialog.setContentText(message.content);
        alertDialog.show();
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPayWithTransToken(final PaymentDataEvent event) {
        if (presenter != null) {
            presenter.pay(event.appId, event.zptranstoken);
        }
        eventBus.removeStickyEvent(PaymentDataEvent.class);
    }

}
