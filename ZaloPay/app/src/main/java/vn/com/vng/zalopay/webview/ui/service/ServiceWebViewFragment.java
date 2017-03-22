package vn.com.vng.zalopay.webview.ui.service;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.utils.RootUtils;
import vn.com.vng.zalopay.webview.ui.IWebView;
import vn.com.vng.zalopay.webview.ui.WebViewFragment;
import vn.com.vng.zalopay.webview.widget.ZPWebView;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;

/**
 * Created by longlv on 10/3/16.
 * Service web
 */
public class ServiceWebViewFragment extends WebViewFragment implements IWebView {

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
        mPresenter.attachView(this);
        mPresenter.initData(getArguments());
    }

    @Override
    protected void initWebViewUserAgent(ZPWebView webView) {
        webView.setUserAgent(getUserAgentWebService());
    }

    private String getUserAgentWebService() {
        String userAgent = Constants.UserAgent.ZALO_PAY_CLIENT + AppVersionUtils.getMainVersionName();
        userAgent += " " + Constants.UserAgent.PLATFORM;
        userAgent += " " + Constants.UserAgent.OS + Build.VERSION.RELEASE;
        userAgent += " " + Constants.UserAgent.SECURED + !RootUtils.isDeviceRooted();
        return userAgent;
    }

    @Override
    protected void loadDefaultWebView() {
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
        mPresenter.detachView();
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
        if (mPresenter != null && !mPresenter.isServiceWeb(getCurrentUrl())) {
            loadDefaultWebView();
            return true;
        }
        if (mWebViewProcessor == null) {
            return false;
        }
        if (mWebViewProcessor.hasError()) {
            return false;
        }

        boolean canBack = mWebViewProcessor.canBack();
        Timber.d("Can WebApp navigate back: %s", canBack);
        if (canBack) {
            mWebViewProcessor.runScript("utils.back()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Timber.d("navigation back: %s", value);
                }
            });
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void showInputErrorDialog() {
        DialogHelper.showWarningDialog(getActivity(),
                getContext().getString(R.string.appgame_alert_input_error),
                new ZPWOnEventDialogListener() {
                    @Override
                    public void onOKevent() {
                        getActivity().finish();
                    }
                });
    }
}
