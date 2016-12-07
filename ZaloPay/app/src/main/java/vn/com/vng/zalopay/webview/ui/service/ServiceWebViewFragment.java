package vn.com.vng.zalopay.webview.ui.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.webview.ui.WebViewFragment;

/**
 * Created by longlv on 10/3/16.
 * Service web
 */
public class ServiceWebViewFragment extends WebViewFragment {

    @Inject
    ServiceWebViewPresenter mPresenter;

    public static ServiceWebViewFragment newInstance(Bundle bundle) {
        ServiceWebViewFragment fragment = new ServiceWebViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated start");
    }

    @Override
    protected void initPresenter(View view) {
        super.initPresenter(view);
        mPresenter.setView(this);
        mPresenter.initData(getArguments());
    }

    @Override
    protected void loadWebView() {
        loadUrl(mPresenter.getWebViewUrl());
    }

    @Override
    public void payOrder(final String url) {
        mPresenter.pay(url);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.servicewebapp_menu, menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Timber.d("onOptionsItemSelected: %s", id);
        if (id == R.id.webapp_action_history) {
            loadUrl(mPresenter.getHistoryWebViewUrl());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        if (mPresenter != null && !mPresenter.isServiceWeb(mCurrentUrl)) {
            loadWebView();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_DEPOSIT) {
            if (resultCode == Activity.RESULT_OK) {
                mPresenter.payPendingOrder();
            }
            return;
        } else if (requestCode == Constants.REQUEST_CODE_UPDATE_PROFILE_LEVEL_2) {
            if (resultCode == Activity.RESULT_OK) {
                mPresenter.payPendingOrder();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
