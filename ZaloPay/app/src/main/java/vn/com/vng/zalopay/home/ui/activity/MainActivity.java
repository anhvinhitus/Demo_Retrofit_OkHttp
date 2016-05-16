package vn.com.vng.zalopay.home.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.menu.listener.MenuItemClickListener;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.utils.MenuItemUtil;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;
import vn.zing.pay.zmpsdk.ZingMobilePayApplication;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.listener.ZPWGatewayInfoCallback;

public class MainActivity extends BaseToolBarActivity implements MenuItemClickListener {

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected void setupActivityComponent() {
        Timber.d(TAG, " UserComponent " + getUserComponent());

        if (getUserComponent() == null) {
            Timber.e(TAG, "*** NULL USER COMPONENT ****");
        }

        getUserComponent().inject(this);
    }

/*    private final String REPLACE_HOME_TRANSACTION = "REPLACE_HOME_TRANSACTION";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;*/

    int currentSelected = -1;

    private TextView mTvNotificationCount;

    private BaseFragment mCurrentFragment;

    private int mRetryDownloadPaySDK = 0;

    @Inject
    Navigator navigator;

    @Bind(R.id.appBarLayout)
    AppBarLayout mAppBarLayout;

    @OnClick(R.id.btn_qr_code)
    public void onBtnQrCodeClick(View view) {
        navigator.startQrCodeActivity(this);
    }

    @OnClick(R.id.btn_deposit)
    public void onBtnDepositClick(View view) {
        navigator.startDepositActivity(this);
    }

    @OnClick(R.id.btn_link_card)
    public void onBtnLinkCardClick(View view) {
        navigator.startLinkCardActivity(getActivity());
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
    private void loginPaymentSDK() {
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (user == null) {
            return;
        }

        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;

        ZingMobilePayApplication.loadGatewayInfo(this, paymentInfo, new ZPWGatewayInfoCallback() {
            @Override
            public void onFinish() {
                Timber.tag("LoginPresenter").d("loadGatewayInfo onSuccess");
                mRetryDownloadPaySDK = 0;
            }

            @Override
            public void onProcessing() {
                Timber.tag("LoginPresenter").d("loadGatewayInfo onProcessing");
            }

            @Override
            public void onError(String pMessage) {
                Timber.tag("LoginPresenter").d("loadGatewayInfo onError:%s", pMessage);
                mRetryDownloadPaySDK++;
                if (mRetryDownloadPaySDK < 5) {
                    loginPaymentSDK();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG).d("onCreate....................");

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            addFragment(ZaloPayFragment.newInstance());
        }

        selectHome(true);
        mRetryDownloadPaySDK = 0;
        loginPaymentSDK();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.tag(TAG).d("onNewIntent...........selectHome");
        setSelectedDrawerMenuItem(R.id.nav_home);
        selectHome(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentSelected", currentSelected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        android.view.MenuItem item = menu.findItem(R.id.action_notifications);
//        MenuItemCompat.setActionView(item, R.layout.notification_menu_item);
        FrameLayout notifications = (FrameLayout) MenuItemCompat.getActionView(item);
        mTvNotificationCount = (TextView) notifications.findViewById(R.id.tvNotificationCount);
        updateNotificationCount(0);
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(R.string.action_notifications);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    protected void selectMenu(int id) {
        try {
            setSelectedDrawerMenuItem(id);
        } catch (Exception ex) {
            Timber.tag(TAG).d("Cannot select id: " + id, ex);
        }

    }

    public void selectHome(boolean onStart) {
        Timber.tag(TAG).d("selectHome onStart: " + onStart);
        if (onStart) {
            currentSelected = R.id.nav_home;
        }
        setSelectedDrawerMenuItem(R.id.nav_home);
    }

    private long back_pressed;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (currentSelected != R.id.nav_home) {
            selectHome(false);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                finish();
                System.exit(0);
            } else {
                showToast(R.string.pressed_back_to_exit);
                back_pressed = System.currentTimeMillis();
            }
        }
    }


    private void addFragment(Fragment fragment) {
        if (fragment == null) return;

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment current = fragmentManager.findFragmentById(R.id.container);
        if (current != null && current.equals(fragment)) {
            return;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

    }

    protected boolean setSelectedDrawerMenuItem(int itemId) {
//        if (itemId == currentSelected) {
//            return true;
//        } else {
        int prevId = currentSelected;
        currentSelected = itemId;
        if (itemId == MenuItemUtil.HOME_ID) {
            addFragment(ZaloPayFragment.newInstance());

            if (mAppBarLayout != null) {
                mAppBarLayout.setExpanded(true, true);
            }
            return true;
        } else if (itemId == MenuItemUtil.TRANSFER_ID) {
            selectHome(false);
            return true;
        } else if (itemId == MenuItemUtil.DEPOSIT_ID) {
            navigator.startDepositActivity(this);
        } else if (itemId == MenuItemUtil.SCAN_QR_ID) {
            navigator.startQrCodeActivity(this);
        } else if (itemId == MenuItemUtil.FAQ_ID) {
            navigator.startMiniAppActivity(this, "FAQ");
        } else if (itemId == MenuItemUtil.NOTIFICATION_ID) {
            navigator.startMiniAppActivity(this, "Notifications");
        } else if (itemId == MenuItemUtil.APPLICATION_INFO_ID) {
            navigator.startMiniAppActivity(this, "About");
        } else if (itemId == MenuItemUtil.CONTACT_SUPPORT_ID) {
            navigator.startMiniAppActivity(this, "Help");
        } else if (itemId == MenuItemUtil.TRANSACTION_HISTORY_ID) {
            navigator.startMiniAppActivity(this, "TransactionLogs");
        } else if (itemId == MenuItemUtil.SIGOUT_ID) {
            ZaloSDK.Instance.unauthenticate();
            navigator.startLoginActivity(this);
        }


        return false;
    }

    @Override
    public void onMenuItemClick(MenuItem menuItem) {
        if (menuItem == null) {
            return;
        }
        int id = menuItem.getId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        setSelectedDrawerMenuItem(id);
    }

    @Override
    public void onMenuHeaderClick(MenuItem menuItem) {
    }

    public void updateNotificationCount(final int count) {
        if (count <= 0) {
            mTvNotificationCount.setText("");
            mTvNotificationCount.setVisibility(View.GONE);
        } else {
            mTvNotificationCount.setText("" + count);
            mTvNotificationCount.setVisibility(View.VISIBLE);
        }
    }
}
