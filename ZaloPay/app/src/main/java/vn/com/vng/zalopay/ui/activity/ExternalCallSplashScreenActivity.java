package vn.com.vng.zalopay.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.ExternalCallSplashScreenPresenter;
import vn.com.vng.zalopay.ui.view.IExternalCallSplashScreenView;

/**
 * Created by huuhoa on 11/26/16.
 * Activity for receiving external calls
 */

public class ExternalCallSplashScreenActivity extends BaseActivity implements IExternalCallSplashScreenView {

    @Inject
    ExternalCallSplashScreenPresenter mPresenter;

    @Override
    protected void setupActivityComponent() {
        getAppComponent().inject(this);
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate new ExternalCallSplashScreenActivity");
        mPresenter.handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.d("onNewIntent for ExternalCallSplashScreenActivity");
    }

    @Override
    public Context getContext() {
        return this;
    }
}
