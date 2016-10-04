package vn.com.vng.zalopay.webview.ui.service;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.webview.ui.IWebView;
import vn.com.vng.zalopay.webview.ui.WebViewActivity;
import vn.com.vng.zalopay.webview.ui.WebViewFragment;
import vn.com.vng.zalopay.webview.widget.ZPWebViewProcessor;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

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
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroy();
        }
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (mWebViewProcessor == null) {
            return false;
        }
        if (mWebViewProcessor.hasError()) {
            return false;
        }
        mWebViewProcessor.runScript("utils.back()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Timber.d("navigation back: %s", value);
            }
        });
        return true;
    }




}
