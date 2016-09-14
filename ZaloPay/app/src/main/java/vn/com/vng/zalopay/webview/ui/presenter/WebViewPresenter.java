package vn.com.vng.zalopay.webview.ui.presenter;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import org.parceler.Parcels;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.navigation.INavigator;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.webview.entity.WebViewPayInfo;
import vn.com.vng.zalopay.webview.ui.view.IWebView;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.webview.config.WebViewConfig;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

import static android.text.TextUtils.isEmpty;

/**
 * Created by longlv on 14/09/2016.
 *
 */
public class WebViewPresenter extends BaseUserPresenter implements IPresenter<IWebView> {

    private IWebView mView;

    protected String mHost;
    protected WebViewPayInfo mAppGamePayInfo;
    private BalanceStore.Repository balanceRepository;
    private ZaloPayRepository zaloPayRepository;
    private TransactionStore.Repository transactionRepository;
    private Navigator mNavigator;

    @Inject
    public WebViewPresenter(BalanceStore.Repository balanceRepository,
                            ZaloPayRepository zaloPayRepository,
                            TransactionStore.Repository transactionRepository,
                            Navigator navigator) {
        this.balanceRepository = balanceRepository;
        this.zaloPayRepository = zaloPayRepository;
        this.transactionRepository = transactionRepository;
        mNavigator = navigator;
    }

    public void initData(Bundle arguments) {
        if (arguments == null) {
            return;
        }

        mHost = arguments.getString("webUrl");
        mAppGamePayInfo = Parcels.unwrap(arguments.getParcelable("appGamePayInfo"));
    }


    public String getWebViewUrl() {
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
            mView.showInputErrorDialog();
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
        Timber.d("pay order [%s]", order.toString());
        PaymentWrapper paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mView.getActivity();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                Timber.d("onParameterError");
                if ("order".equalsIgnoreCase(param)) {
                    showError(mView.getActivity().getString(R.string.order_invalid));
                } else if ("uid".equalsIgnoreCase(param)) {
                    showError(mView.getActivity().getString(R.string.user_invalid));
                } else if ("token".equalsIgnoreCase(param)) {
                    showError(mView.getActivity().getString(R.string.order_invalid));
                }
            }

            @Override
            public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {
                Timber.d("onPreComplete appTransID [%s]", pAppTransId);

            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                Timber.d("onResponseError");
                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                    showError(mView.getActivity().getString(R.string.exception_no_connection_try_again));
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                Timber.d("onResponseSuccess zpPaymentResult [%s]", zpPaymentResult);
                if (zpPaymentResult == null) {
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
            public void onResponseTokenInvalid() {
                Timber.d("onResponseTokenInvalid");
                onSessionExpired();
            }

            @Override
            public void onAppError(String msg) {
                Timber.d("onAppError msg [%s]", msg);
                showError(mView.getActivity().getString(R.string.exception_generic));
            }

            @Override
            public void onNotEnoughMoney() {
                Timber.d("onNotEnoughMoney activity [%s]", mView.getActivity());
                mNavigator.startDepositActivity(mView.getActivity());
            }

            private void showError(String text) {
                if (isEmpty(text)) {
                    return;
                }
                Toast.makeText(mView.getActivity(), text, Toast.LENGTH_SHORT).show();
            }

            private void onSessionExpired() {
                showError(mView.getActivity().getString(R.string.exception_token_expired_message));
                AndroidApplication.instance().getAppComponent().applicationSession().clearUserSession();
            }
        });

        paymentWrapper.payWithOrder(order);
    }

    @Override
    public void setView(IWebView iWebView) {
        mView = iWebView;
    }

    @Override
    public void destroyView() {
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
}
