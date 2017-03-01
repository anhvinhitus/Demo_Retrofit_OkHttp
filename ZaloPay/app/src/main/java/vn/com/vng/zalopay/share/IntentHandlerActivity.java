package vn.com.vng.zalopay.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
        mPresenter.handleIntent(getIntent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void finishActivity(boolean removeTask) {
        if (!removeTask) {
            this.finish();
            return;
        }

        if (!isTaskRoot()) {
            Timber.d("move task to back");
            this.moveTaskToBack(true);
            this.finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            Timber.d("finish and remove task");
            this.finishAndRemoveTask();
        } else {
            this.finish();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }
}
