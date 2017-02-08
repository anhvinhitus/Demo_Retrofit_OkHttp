package vn.com.vng.zalopay.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
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
        mPresenter.attachView(this);
        mPresenter.handleIntent(getIntent());
        View view = findViewById(R.id.fragment_container);
        if (view != null) {
            view.setBackgroundResource(R.color.background);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.d("onNewIntent for ExternalCallSplashScreenActivity");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }
}
