package vn.com.vng.zalopay.share;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by hieuvm on 2/13/17.
 */

public class IntentHandlerActivity extends BaseActivity implements IIntentHandlerView {

    @Inject
    IntentHandlerPresenter mPresenter;

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
        Timber.d("onCreate taskid %s", getTaskId());
        mPresenter.attachView(this);
        mPresenter.handleIntent(getIntent());
        View view = findViewById(R.id.fragment_container);
        if (view != null) {
            view.setBackgroundResource(R.color.background);
        }
        Timber.d("onCreate: ");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mPresenter.handleIntent(getIntent());
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
        Timber.d("onDestroy: ");
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void finish() {
        Timber.d("finish: ");
        super.finish();
    }
}