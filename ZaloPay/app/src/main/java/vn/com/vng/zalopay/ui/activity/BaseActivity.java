package vn.com.vng.zalopay.ui.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.ToastUtil;

/**
 * Created by AnhHieu on 3/24/16.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupActivityComponent() {
    }

    public abstract BaseFragment getFragmentToHost();

    protected final String TAG = getClass().getSimpleName();

    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.setStatusBarColor(getColor(R.color.colorPrimaryDark));
                return;
            }
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        createUserComponent();
        setupActivityComponent();
        setContentView(getResLayoutId());

        if (savedInstanceState == null) {
            hostFragment(getFragmentToHost());
        }
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
        if (AndroidApplication.instance().getUserComponent() != null)
            return;
//        Runtime runtime = AndroidApplication.instance().getAppComponent().runtime();
//        if (runtime.isUserRegistered()) {
//            DatabaseAppScope databaseHelper = ZingMobileApplication.getInstance().getAppComponent().databaseAppScope();
//            User user = databaseHelper.getUser();
//            ZingMobileApplication.getInstance().createUserComponent(user);
//        }

		//longlv: todo test only
//        User user = new User();
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        user.accesstoken = sharedPreferences.getString(Constants.PREF_USER_SESSION, "");
//        user.uid = sharedPreferences.getLong(Constants.PREF_USER_ID, 0);
//        user.uid = 8808474179545805942l;
//        AndroidApplication.instance().createUserComponent(user);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ButterKnife.bind(this);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }


    @Override
    public void onBackPressed() {
        BaseFragment activeFragment = getActiveFragment();
        if (activeFragment == null || !activeFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }


    protected BaseFragment getActiveFragment() {
        return (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
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
}
