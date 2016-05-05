package vn.com.vng.zalopay.home.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.menu.listener.MenuItemClickListener;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.ui.adapter.MenuItemAdapter;
import vn.com.vng.zalopay.menu.utils.MenuItemUtil;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.zing.pay.zmpsdk.helper.gms.RegistrationIntentService;


public class MainActivity extends BaseToolBarActivity implements MenuItemClickListener {

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    private final String REPLACE_HOME_TRANSACTION = "REPLACE_HOME_TRANSACTION";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    int currentSelected = -1;

    HeaderHolder header;
    private TextView mTvNotificationCount;

    ZaloPayFragment homeFragment;
    NavigationView navigationView;
    ListView menuItemListView;
    MenuItemAdapter menuItemAdapter;

    @Inject
    Navigator navigator;

    @Bind(R.id.im_logo)
    ImageView mImLogo;

    @Bind(R.id.tv_title)
    TextView mTvTitle;

    @Bind(R.id.appBarLayout)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.btn_qr_code)
    View mBtnQrCode;

    @Bind(R.id.btn_deposit)
    View mBtnDeposit;

    @OnClick(R.id.btn_qr_code)
    public void onBtnQrCodeClick(View view) {
        navigator.startQrCodeActivity(this);
    }

    @OnClick(R.id.btn_deposit)
    public void onBtnDepositClick(View view) {
        gotoDepositActivity();
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
        AndroidApplication.instance().getUserComponent().inject(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        hideDefaultTitle();
        showLogo();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        menuItemListView = (ListView) findViewById(R.id.list);
        menuItemAdapter = new MenuItemAdapter(this, MenuItemUtil.getMenuItems(), this);

        header = new HeaderHolder(this);
//        navigationView.addHeaderView(header.root);
        menuItemListView.addHeaderView(header.root);
        menuItemListView.setAdapter(menuItemAdapter);
        //navigationView.setNavigationItemSelectedListener(this);

        String versionName = BuildConfig.VERSION_NAME;
//        int versionCode = BuildConfig.VERSION_CODE;
        StringBuilder strVersion = new StringBuilder();
        strVersion.append("Phiên bản ");
        strVersion.append(versionName);
        header.tvVersion.setText(strVersion.toString());

        String phone = "0988888888";
        String name = "Nguyen Van A";
        String email = "vng.zalopay@gmail.com";//AppCommon.instance().getUserConfigs().getEmail();
        String avatar = "https://plus.google.com/u/0/_/focus/photos/public/AIbEiAIAAABECI7LguvYhZ7MuAEiC3ZjYXJkX3Bob3RvKig0MDE5NGQ2ODRhNjU5ODJiYTgxNjkwNWU3Njk3MWI5MDA1MGJjZmRhMAGGAaoGCMD24SAz49-T4-e-nZAtIA?sz=96";
        if (!TextUtils.isEmpty(name)){
            header.tvName.setText(name);
            header.tvName.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(phone)){
                header.tvPhone.setText(name);
                header.tvPhone.setVisibility(View.VISIBLE);
            } else {
                header.tvPhone.setVisibility(View.GONE);
            }
        } else {
            header.tvPhone.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(phone)){
                header.tvName.setText(phone);
                header.tvName.setVisibility(View.VISIBLE);
            } else {
                header.tvName.setVisibility(View.GONE);
            }
        }
        if (!TextUtils.isEmpty(email)){
            header.tvEmail.setText(email);
            header.tvEmail.setVisibility(View.VISIBLE);
        } else {
            header.tvEmail.setVisibility(View.GONE);
        }
        loadAvatarImage(header.imageAvatar, avatar);

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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if (homeFragment != null){
//            homeFragment.updateListLayout(newConfig);
//        }
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

    protected void selectMenu(int id){
        try {
            //navigationView.getMenu().findItem(id).setChecked(true);
            setSelectedDrawerMenuItem(id);
        } catch (Exception ex){
            Timber.tag(TAG).d("Cannot select id: " + id, ex);
        }

    }
    public void selectHome(boolean onStart){
        Timber.tag(TAG).d("selectHome onStart: " + onStart);
        if (onStart) {
            currentSelected = R.id.nav_home;
        }
        setSelectedDrawerMenuItem(R.id.nav_home);
        //navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
    }

    private static long back_pressed;
    private static final long TIME_BETWEEN_DOUBLE_BACK= 2000;//2s
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (currentSelected != R.id.nav_home){
//                super.onBackPressed();
            selectHome(false);
        } else {
            if (back_pressed + TIME_BETWEEN_DOUBLE_BACK > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        ToastUtil.showToast(MainActivity.this, "Bấm một lần nữa để thoát!");
                    }
                });
            }
            back_pressed = System.currentTimeMillis();
        }
    }

    private void startZMPSDKDemo() {
        Intent intent = new Intent(this, vn.zing.pay.trivialdrivesample.DemoSDKActivity.class);
        startActivity(intent);
    }

    protected boolean setSelectedDrawerMenuItem(int itemId) {
        if (itemId == currentSelected) {
            return true;
        } else {
            int prevId = currentSelected;
            currentSelected = itemId;
            if (itemId == MenuItemUtil.HOME_ID) {
                showLogo();
                if (!getSupportFragmentManager().popBackStackImmediate(REPLACE_HOME_TRANSACTION, FragmentManager.POP_BACK_STACK_INCLUSIVE)){
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
                startZMPSDKDemo();
                selectHome(false);
                return true;
            } else if (itemId == MenuItemUtil.SCAN_QR_ID) {
                navigator.startQrCodeActivity(this);
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
    public void onMenuHeaderClick(vn.com.vng.zalopay.menu.model.MenuItem menuItem) {

    }

    class HeaderHolder {
        public View root;

        @Bind(R.id.im_avatar)
        public ImageView imageAvatar;
        @Bind(R.id.tv_name)
        public TextView tvName;
        @Bind(R.id.tv_phone)
        public TextView tvPhone;
        @Bind(R.id.tv_email)
        public TextView tvEmail;
        @Bind(R.id.tvVersion)
        public TextView tvVersion;

//        @OnClick(R.id.btn_friends)
//        void onFriendsClicked(View v){
//            startFriendsActivity();
//        }

//        @OnClick(R.id.btn_popup)
//        void onOpenPopup(View v){
//            PopupMenu popupMenu = new PopupMenu(getActivity(), v, Gravity.BOTTOM);
//            popupMenu.inflate(R.menu.menu_profile_popup);
//            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    //TODO
//                    int itemId = item.getItemId();
//                    if (itemId == R.id.action_friend_list) {
//                        startFriendsActivity();
//                    } else if (itemId == R.id.action_group_list) {
//                        startGroupActivity();
//                    } else {
//                        return false;
//                    }
//
//                    return true;
//                }
//            });
//            popupMenu.show();
//        }

        public HeaderHolder(AppCompatActivity activity){
//            root = activity.findViewById(R.id.nav_header_main);
            root = getLayoutInflater().inflate(R.layout.nav_header_main,null);
            ButterKnife.bind(this,root);
        }

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

    public void hideDefaultTitle(){
        ActionBar actionbar = getSupportActionBar();
        if (actionbar!= null) {
            actionbar.setDisplayShowTitleEnabled(false);
        }
    }

    public void showTitle(CharSequence title){
        if (mImLogo != null && mTvTitle != null){
            mImLogo.setVisibility(View.GONE);
            mTvTitle.setVisibility(View.VISIBLE);
            mTvTitle.setText(title);
        }
    }

    public void showLogo(){
        if (mImLogo != null){
            mImLogo.setVisibility(View.VISIBLE);
            mTvTitle.setVisibility(View.GONE);
            mTvTitle.setText(getString(R.string.app_name));
        }
    }

//    public void showBalanceOnly(){
//        if (mBalanceFragment != null){
//            mBalanceFragment.showLayout(true, false);
//        }
//    }
//
//    public void showBalanceAndButtons(){
//        if (mBalanceFragment != null){
//            mBalanceFragment.showLayout(true, true);
//        }
//    }
//
//    public void hideBalanceAllView(){
//        if (mBalanceFragment != null){
//            mBalanceFragment.showLayout(false, false);
//        }
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

    private void loadAvatarImage(ImageView imageView, String url) {
        Glide.with(this).load(url).placeholder(R.color.background).into(imageView);
    }
}
