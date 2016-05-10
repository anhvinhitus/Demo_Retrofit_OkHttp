package vn.com.vng.zalopay.home.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.menu.listener.MenuItemClickListener;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.ui.adapter.MenuItemAdapter;
import vn.com.vng.zalopay.menu.utils.MenuItemUtil;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.zing.pay.zmpsdk.ZingMobilePayApplication;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.helper.gms.RegistrationIntentService;
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
        Log.d("SetupComponent", " AndroidApplication.instance().getUserComponent()" + AndroidApplication.instance().getUserComponent());
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    private final String REPLACE_HOME_TRANSACTION = "REPLACE_HOME_TRANSACTION";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    int currentSelected = -1;

    private TextView mTvNotificationCount;

    ZaloPayFragment homeFragment;

    private int mRetryDownloadPaySDK = 0;

    @Inject
    Navigator navigator;

    @Bind(R.id.appBarLayout)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.btn_qr_code)
    View mBtnQrCode;

    @Bind(R.id.btn_deposit)
    View mBtnDeposit;

    @Bind(R.id.btn_link_card)
    View mBtnLinkCard;

    @OnClick(R.id.btn_qr_code)
    public void onBtnQrCodeClick(View view) {
        navigator.startQrCodeActivity(this);
    }

    @OnClick(R.id.btn_deposit)
    public void onBtnDepositClick(View view) {
        gotoDepositActivity();
    }

    @OnClick(R.id.btn_link_card)
    public void onBtnLinkCardClick(View view) {

    }

    private void gotoDepositActivity() {
        Intent intent = new Intent(this, BalanceTopupActivity.class);
        startActivity(intent);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
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
    }

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
            public void onFinish()
            {
                Timber.tag("LoginPresenter").d("loadGatewayInfo onSuccess");
                mRetryDownloadPaySDK = 0;
            }

            @Override
            public void onProcessing()
            {
                Timber.tag("LoginPresenter").d("loadGatewayInfo onProcessing");
            }

            @Override
            public void onError(String pMessage)
            {
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

        if (savedInstanceState != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.root);
            if (fragment instanceof ZaloPayFragment) {
                homeFragment = (ZaloPayFragment) fragment;
            }
//            mBalanceFragment = (BalanceFragment)getSupportFragmentManager().findFragmentById(R.id.toolbar_fragment);
        } else {
            homeFragment = ZaloPayFragment.newInstance();
//            mBalanceFragment = BalanceFragment.newInstance();
//            getSupportFragmentManager().beginTransaction().add(R.id.root, homeFragment).add(R.id.toolbar_fragment, mBalanceFragment).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.root, homeFragment).commit();
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
        updateNotificationCount(currentNotificationCount);
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(R.string.action_notifications);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
//        int itemId = item.getItemId();
//        if (itemId == R.id.action_notifications) {
//            ToastUtil.showToast(this, "Thông báo ------------");
//        }
        return super.onOptionsItemSelected(item);
    }

    protected void selectMenu(int id) {
        try {
            //navigationView.getMenu().findItem(id).setChecked(true);
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
        //navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
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

    protected boolean setSelectedDrawerMenuItem(int itemId) {
//        if (itemId == currentSelected) {
//            return true;
//        } else {
        int prevId = currentSelected;
        currentSelected = itemId;
        if (itemId == MenuItemUtil.HOME_ID) {
            if (!getSupportFragmentManager().popBackStackImmediate(REPLACE_HOME_TRANSACTION, FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
                homeFragment = ZaloPayFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.root, homeFragment).commit();
            }
//                Fragment fragment = HomeFragment.newInstance();
//                getSupportFragmentManager().beginTransaction().replace(R.id.root, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
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
        }/*  else if (itemId == R.id.nav_cards) {
            hideBalanceAllView();
            showTitle(getString(R.string.title_activity_cards));
            Fragment fragment = CardsFragment.newInstance(1);
            if (prevId != R.id.nav_home){
                getSupportFragmentManager().popBackStack(REPLACE_HOME_TRANSACTION, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.root, fragment).addToBackStack(REPLACE_HOME_TRANSACTION).commit();
            if (mAppBarLayout != null) {
                mAppBarLayout.setExpanded(true, false);
            }
            return true;
        }*/
//        }
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
    public void onMenuHeaderClick(vn.com.vng.zalopay.menu.model.MenuItem menuItem) {

    }

//    private ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Timber.tag(TAG).d("onServiceConnected..............name:" + name);
//            Timber.tag(TAG).d("onServiceConnected..............service:" + service);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Timber.tag(TAG).d("onServiceDisconnected..............name:" + name);
//        }
//    };

//    private void startBroadcastReceiver() {
//        Timber.tag(TAG).d("startBroadcastReceiver..................");
//        // Bind to the service
//        //startService(new Intent(this, MonitorService.class));//, mServiceConnection, Context.BIND_AUTO_CREATE);
//        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        Intent i = new Intent(this, ScheduleReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
//        Calendar cal = Calendar.getInstance();
//        // Start 30 seconds after boot completed
//        cal.add(Calendar.SECOND, 1);
//        //
//        // Fetch every 30 seconds
//        // InexactRepeating allows Android to optimize the energy consumption
//        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pendingIntent);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000 * 30, pendingIntent);
//    }

    private void setExpanded(boolean expand, boolean animate) {
        if (mAppBarLayout != null) {
            mAppBarLayout.setExpanded(expand, animate);
        }
    }

    public void setExpanded(boolean expanded) {
        if (mAppBarLayout != null) {
            mAppBarLayout.setExpanded(expanded);
        }
    }

    protected int currentNotificationCount = 2;

    public synchronized void updateNotificationCount(final int count) {
        currentNotificationCount = count;
        if (mTvNotificationCount == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (count <= 0) {
                    mTvNotificationCount.setText("");
                    mTvNotificationCount.setVisibility(View.GONE);
                } else {
                    mTvNotificationCount.setText("" + count);
                    mTvNotificationCount.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
