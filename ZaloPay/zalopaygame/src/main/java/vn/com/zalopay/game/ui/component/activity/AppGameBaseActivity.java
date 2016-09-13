package vn.com.zalopay.game.ui.component.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import java.util.Stack;

import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.ui.webview.AppGameWebViewProcessor;


public abstract class AppGameBaseActivity extends AppCompatActivity {
    private FragmentTransaction mFragmentTransaction;
    private FragmentManager mFragmentManager;

    private static Stack<AppGameBaseActivity> mActivitiesStack = new Stack<AppGameBaseActivity>();

    public static Activity getCurrentActivity() {
        synchronized (mActivitiesStack) {
            if (mActivitiesStack == null || mActivitiesStack.isEmpty()) {
                return AppGameGlobal.getOwnerActivity();
            }

            return mActivitiesStack.peek();
        }
    }

    protected void fadeOutTransition() {
        this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    protected void fadeInTransition() {
        this.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
    }

    protected void slideOutTransition() {
        this.overridePendingTransition(R.anim.closeslidein, R.anim.closeslideout);
    }

    @Override
    public void finish() {
        super.finish();

        if (!AppGameWebViewProcessor.canPayment)
            slideOutTransition();
        else
            fadeInTransition();
    }

    //replace sreen
    public void inflatFragment(Fragment f, boolean isAddToStack) {
        if (f.isAdded())
            return;

        if (mFragmentManager == null)
            mFragmentManager = getSupportFragmentManager();

        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.contentLayout, f, f.getClass().getName());
        if (isAddToStack)
            mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        synchronized (mActivitiesStack) {
            if (mActivitiesStack == null)
                mActivitiesStack = new Stack<>();

            mActivitiesStack.push(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        synchronized (mActivitiesStack) {
            mActivitiesStack.remove(this);
        }
    }

    //region abstract methods
    protected abstract void logout();

    protected abstract void startUrl(String pUrl);
    //endregion

}
