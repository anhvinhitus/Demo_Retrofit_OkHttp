package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 3/24/16.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupActivityComponent() {
    }

    public abstract BaseFragment getFragmentToHost();

    protected final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);

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
       /* if (AndroidApplication.getInstance().getUserComponent() != null)
            return;
        com.meplay.app.Runtime runtime = ZingMobileApplication.getInstance().getAppComponent().runtime();
        if (runtime.isUserRegistered()) {
            DatabaseAppScope databaseHelper = ZingMobileApplication.getInstance().getAppComponent().databaseAppScope();
            User user = databaseHelper.getUser();
            ZingMobileApplication.getInstance().createUserComponent(user);
        }*/
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
}
