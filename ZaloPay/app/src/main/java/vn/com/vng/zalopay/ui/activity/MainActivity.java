package vn.com.vng.zalopay.ui.activity;

import android.Manifest;
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

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.menu.utils.MenuItemUtil;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.ui.callback.MenuClickListener;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.LeftMenuFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;
import vn.com.vng.zalopay.ui.presenter.MainPresenter;
import vn.com.vng.zalopay.ui.view.IHomeView;

/**
 * Created by AnhHieu on 5/24/16.
 * Main Application activity
 */
public class MainActivity extends BaseToolBarActivity implements MenuClickListener, IHomeView {

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

    private ActionBarDrawerToggle toggle;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    private int mCurrentMenuId;

    @Inject
    Navigator navigator;

    @Inject
    MainPresenter presenter;

    @Inject
    GlobalEventHandlingService globalEventHandlingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.setView(this);
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


        //init SDK
        presenter.loadGatewayInfoPaymentSDK();
        globalEventHandlingService.setMainActivity(this);
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
        drawer.removeDrawerListener(toggle);
        presenter.destroyView();
        globalEventHandlingService.setMainActivity(null);
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
//        navigator.startPreProfileActivity(this);
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
            case MenuItemUtil.APPLICATION_ID:
                if (id != mCurrentMenuId) {
                    // getSupportFragmentManager().beginTransaction().replace(R.id.container, new uy(), "AccountFragment").commit();
                    mCurrentMenuId = id;
                }
                break;
            case MenuItemUtil.APPLICATION_INFO_ID:
                navigator.startMiniAppActivity(this, "About");
                break;
            case MenuItemUtil.CONTACT_SUPPORT_ID:
                navigator.startMiniAppActivity(this, "Help");
                break;
            case MenuItemUtil.DEPOSIT_ID:
                navigator.startDepositActivity(this);
                break;
            case MenuItemUtil.FAQ_ID:
                navigator.startMiniAppActivity(this, "FAQ");
                break;
            case MenuItemUtil.HOME_ID:
                break;
            case MenuItemUtil.NOTIFICATION_ID:
                navigator.startMiniAppActivity(this, "Notifications");
                break;
            case MenuItemUtil.SCAN_QR_ID:
                startQRCodeActivity();
                break;
            case MenuItemUtil.SIGOUT_ID:
                getAppComponent().userConfig().sigoutAndCleanData(this);
                break;
            case MenuItemUtil.TRANSACTION_HISTORY_ID:
                navigator.startMiniAppActivity(this, "TransactionLogs");
                break;
            case MenuItemUtil.TRANSACTION_ID:
                break;
            case MenuItemUtil.TRANSFER_ID:
                navigator.startTrasferHomeActivity(this);
                break;

        }
    }

    private void startQRCodeActivity() {
        if (checkAndRequestPermission(Manifest.permission.CAMERA, 100)) {
            navigator.startQrCodeActivity(this);
        }
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

    private long back_pressed;

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

      /*  */

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     *//*
    private boolean checkPlayServices() {
        Timber.tag(TAG).d("checkPlayServices.........");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Timber.tag(TAG).d("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void startRegistrationReceiver() {
        Timber.tag(TAG).d("startRegistrationReceiver......");
        if (checkPlayServices()) {
            Timber.tag(TAG).d("Start IntentService to register this application with GCM");
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }*/
}
