package vn.com.vng.zalopay.webview.ui.service;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.service.DefaultPaymentRedirectListener;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.webview.WebViewConstants;
import vn.com.vng.zalopay.webview.config.WebViewConfig;
import vn.com.vng.zalopay.webview.entity.WebViewPayInfo;
import vn.com.vng.zalopay.webview.ui.IWebView;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 14/09/2016.
 * *
 */
public class ServiceWebViewPresenter extends AbstractPresenter<IWebView> {
    private String mHost;
    private WebViewPayInfo mAppGamePayInfo;
    private Navigator mNavigator;
    private PaymentWrapper mPaymentWrapper;

    @Inject
    ServiceWebViewPresenter(BalanceStore.Repository balanceRepository,
                            ZaloPayRepository zaloPayRepository,
                            TransactionStore.Repository transactionRepository,
                            Navigator navigator) {
        this.mNavigator = navigator;
        this.mPaymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new DefaultPaymentRedirectListener(mNavigator) {
                    @Override
                    public Object getContext() {
                        if (mView == null) {
                            return null;
                        }
                        return mView.getFragment();
                    }
                })
                .build();
    }

    void initData(Bundle arguments) {
        if (arguments == null) {
            return;
        }

        mHost = arguments.getString(WebViewConstants.WEBURL);
        mAppGamePayInfo = arguments.getParcelable(WebViewConstants.APPGAMEPAYINFO);
    }

    boolean isServiceWeb(String url) {
        Timber.d("isServiceWeb url [%s] mHost [%s]", url, mHost);
        return !TextUtils.isEmpty(url) && url.startsWith(mHost);
    }

    String getHistoryWebViewUrl() {
        Timber.d("getHistoryWebViewUrl mAppGamePayInfo [%s]", mAppGamePayInfo);
        if (mAppGamePayInfo == null) {
            return "";
        }

        final String url = String.format(
                WebViewConfig.getHistoryWebViewUrl(mHost),
                mAppGamePayInfo.uid,
                mAppGamePayInfo.accessToken);
        Timber.d("getHistoryWebViewUrl url [%s]", url);
        return url;
    }

    String getWebViewUrl() {
        Timber.d("getWebViewUrl mAppGamePayInfo [%s]", mAppGamePayInfo);
        if (mAppGamePayInfo == null) {
            return "";
        }

        final String url = String.format(
                WebViewConfig.getWebViewUrl(mHost),
                mAppGamePayInfo.uid,
                mAppGamePayInfo.accessToken,
                mAppGamePayInfo.appId);
        Timber.d("getWebViewUrl url [%s]", url);
        return url;
    }

    public void pay(final String url) {
        Timber.d("payOrder url [%s]", url);
        //Check param valid
        Uri data = Uri.parse(url);
        String muid = data.getQueryParameter("muid");
        String accesstoken = data.getQueryParameter("maccesstoken");
        String appid = data.getQueryParameter("appid");
        String apptransid = data.getQueryParameter("apptransid");
        String appuser = data.getQueryParameter("appuser");
        String apptime = data.getQueryParameter("apptime");
        String item = data.getQueryParameter("item");
        String description = data.getQueryParameter("description");
        String embeddata = data.getQueryParameter("embeddata");
        String amount = data.getQueryParameter("amount");
        String mac = data.getQueryParameter("mac");

        if (TextUtils.isEmpty(muid) ||
                TextUtils.isEmpty(apptransid) ||
                TextUtils.isEmpty(appuser) ||
                TextUtils.isEmpty(amount) ||
                TextUtils.isEmpty(mac)) {
            showInputErrorDialog();
            return;
        }

        //decode Base64
        //String decodeDescription = new String(Base64.decode(description.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE));
        String decodeDescription = description;
        try {
            decodeDescription = URLDecoder.decode(description, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Timber.w(e, "Url decode exception [%s]", e.getMessage());
        }
        final Order order = new Order(Long.valueOf(appid), accesstoken, apptransid, appuser, Long.valueOf(apptime),
                embeddata, item, Long.parseLong(amount), decodeDescription, null, mac);
        pay(order);
    }

    public void pay(Order order) {
        if (order == null) {
            return;
        }
        Timber.d("pay order [%s] view [%s]", order.toString(), mView);
        mPaymentWrapper.payWithOrder(mView.getActivity(), order, ZPPaymentSteps.OrderSource_WebToApp);
    }

    private void showInputErrorDialog() {
        if (mView == null) {
            return;
        }
        mView.showInputErrorDialog();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPaymentWrapper == null) {
            return;
        }

        mPaymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    void onPageFinished(String url) {
        if (getHistoryWebViewUrl().equals(url)) {
            mView.hideHistoryMenuItem();
        } else {
            mView.showHistoryMenuItem();
        }
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {
        @Override
        protected ILoadDataView getView() {
            return mView;
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (zpPaymentResult == null || zpPaymentResult.paymentInfo == null || mView == null || mAppGamePayInfo == null) {
                return;
            }

            Timber.d("onResponseSuccess uid [%s] accessToken [%s] appId [%s] transid [%s]", mAppGamePayInfo.uid,
                    mAppGamePayInfo.accessToken, mAppGamePayInfo.appId, mAppGamePayInfo.apptransid);

            mAppGamePayInfo.apptransid = (zpPaymentResult.paymentInfo.appTransID);

            String urlPage = String.format(WebViewConfig.getResultWebViewUrl(mHost), mAppGamePayInfo.apptransid,
                    mAppGamePayInfo.uid, mAppGamePayInfo.accessToken);

            Timber.d("Page url : [%s]", urlPage);

            mView.loadUrl(urlPage);
        }

    }

}
