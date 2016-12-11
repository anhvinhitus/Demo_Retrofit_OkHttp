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
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.webview.WebViewConstants;
import vn.com.vng.zalopay.webview.config.WebViewConfig;
import vn.com.vng.zalopay.webview.entity.WebViewPayInfo;
import vn.com.vng.zalopay.webview.ui.IWebView;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 14/09/2016.
 * *
 */
public class ServiceWebViewPresenter extends BaseUserPresenter implements IPresenter<IWebView> {

    private IWebView mView;

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
                .setRedirectListener(new PaymentRedirectListener())
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
                mAppGamePayInfo.getUid(),
                mAppGamePayInfo.getAccessToken());
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
                mAppGamePayInfo.getUid(),
                mAppGamePayInfo.getAccessToken(),
                mAppGamePayInfo.getAppId());
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
        mPaymentWrapper.payWithOrder(mView.getActivity(), order);
    }

    private void showInputErrorDialog() {
        if (mView == null) {
            return;
        }
        mView.showInputErrorDialog();
    }

    @Override
    public void attachView(IWebView iWebView) {
        mView = iWebView;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPaymentWrapper == null) {
            return;
        }

        mPaymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {
        @Override
        protected ILoadDataView getView() {
            return mView;
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            Timber.d("onResponseSuccess zpPaymentResult [%s]", zpPaymentResult);
            if (zpPaymentResult == null || mView == null || mAppGamePayInfo == null) {
                return;
            }
            mAppGamePayInfo.setApptransid(zpPaymentResult.paymentInfo.appTransID);

            Timber.d("onResponseSuccess appGamePayInfo [%s]", mAppGamePayInfo);
            Timber.d("onResponseSuccess getAccessToken [%s]", mAppGamePayInfo.getAccessToken());
            Timber.d("onResponseSuccess getAppId [%s]", mAppGamePayInfo.getAppId());
            Timber.d("onResponseSuccess getApptransid [%s]", mAppGamePayInfo.getApptransid());
            Timber.d("onResponseSuccess getUid [%s]", mAppGamePayInfo.getUid());
            mAppGamePayInfo.setApptransid(mAppGamePayInfo.getApptransid());

            final String urlPage = String.format(WebViewConfig.getResultWebViewUrl(mHost), mAppGamePayInfo.getApptransid(),
                    mAppGamePayInfo.getUid(), mAppGamePayInfo.getAccessToken());
            Timber.d("onResponseSuccess url [%s]", urlPage);
            mView.loadUrl(urlPage);
        }

        @Override
        public void onNotEnoughMoney() {
            Timber.d("onNotEnoughMoney");
            if (mNavigator == null || mView == null || mView.getFragment() == null) {
                return;
            }
            mNavigator.startDepositForResultActivity(mView.getFragment());
        }
    }

    private class PaymentRedirectListener implements PaymentWrapper.IRedirectListener {
        @Override
        public void startUpdateProfileLevel(String walletTransId) {
            if (mView == null || mView.getFragment() == null) {
                return;
            }
            mNavigator.startUpdateProfile2ForResult(mView.getFragment(), walletTransId);
        }
    }
}
