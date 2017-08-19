package vn.com.zalopay.wallet.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.Stack;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.ui.channellist.ChannelListActivity;

/*
 * Created by chucvv on 6/12/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static Stack<BaseActivity> mActivityStack = new Stack<>();//stack to keep activity
    protected final String TAG = getClass().getSimpleName();

    public static Activity getCurrentActivity() {
        synchronized (mActivityStack) {
            if (mActivityStack.size() == 0) {
                try {
                    return GlobalData.getMerchantActivity();
                } catch (Exception e) {
                    Timber.w(e);
                }
            }
            return mActivityStack.peek();
        }
    }

    public static ChannelListActivity getChannelListActivity() {
        for (Activity activity : mActivityStack) {
            if (activity instanceof ChannelListActivity) {
                return (ChannelListActivity) activity;
            }
        }
        return null;
    }

    public static ChannelActivity getChannelActivity() {
        for (Activity activity : mActivityStack) {
            if (activity instanceof ChannelActivity) {
                return (ChannelActivity) activity;
            }
        }
        return null;
    }

    public static int getActivityCount() {
        return mActivityStack.size();
    }

    public static void release() {
        for (Activity activity : mActivityStack) {
            activity.finish();
        }
    }

    @CallSuper
    @Override
    protected void onStart() {
        super.onStart();
        ZPAnalytics.trackScreen(TAG);
    }

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        synchronized (mActivityStack) {
            mActivityStack.push(this);
        }
        setContentView(getLayoutId());
        if (savedInstanceState == null) {
            hostFragment(getFragmentToHost(getIntent().getExtras()));
        }
        Timber.d("onCreate");
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (mActivityStack) {
            mActivityStack.remove(this);
            if (getActivityCount() == 0) {
                SingletonLifeCircleManager.disposeAll();
            }
        }
        Timber.d("onDestroy");
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isFinishing() || isDestroyed()) {
                return;
            }
        }
        Fragment activeFragment = getActiveFragment();
        if (!(activeFragment instanceof BaseFragment)) {
            super.onBackPressed();
            return;
        }

        if (!((BaseFragment) activeFragment).onBackPressed()) {
            super.onBackPressed();
        }
    }

    public Fragment getActiveFragment() {
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

    protected void hostFragment(BaseFragment fragment, int id) {
        if (fragment != null && getFragmentManager().findFragmentByTag(fragment.getTag()) == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(id, fragment, fragment.TAG);
            ft.commit();
        }
    }

    public void hostFragment(BaseFragment fragment) {
        hostFragment(fragment, R.id.fragment_container);
    }

    protected abstract BaseFragment getFragmentToHost(Bundle bundle);

    protected abstract int getLayoutId();
}
