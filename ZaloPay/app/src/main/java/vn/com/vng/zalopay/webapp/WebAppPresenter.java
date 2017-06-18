package vn.com.vng.zalopay.webapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.json.JSONObject;

import java.util.Arrays;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.webapp.framework.ZPWebViewApp;
import vn.com.vng.webapp.framework.ZPWebViewAppProcessor;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.ZPTransfer;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.presenter.AbstractPaymentPresenter;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.wallet.paymentinfo.IBuilder;

/**
 * Created by longlv on 2/9/17.
 * *
 */

class WebAppPresenter extends AbstractPaymentPresenter<IWebAppView> implements WebBottomSheetDialogFragment.BottomSheetEventListener {

    private static final int TRANSFER_MONEY_WEB_APP_REQUEST_CODE = 101;

    private IPaymentListener mResponseListener;
    private AccountStore.Repository mAccountRepository;
    private ZPTransfer mZPTransfer;
    private ZPWebViewAppProcessor mWebViewProcessor;
    protected AppResourceStore.Repository mAppResourceRepository;
    protected MerchantStore.Repository mMerchantRepository;
    protected Navigator mNavigator;
    private final ProcessMessageListener mProcessMessageListener = new ProcessMessageListener(this);


    @Inject
    WebAppPresenter(AccountStore.Repository accountRepository,
                    Navigator navigator,
                    AppResourceStore.Repository appResourceRepository,
                    MerchantStore.Repository merchantRepository) {
        super(navigator);
        this.mAccountRepository = accountRepository;
        this.mAppResourceRepository = appResourceRepository;
        this.mMerchantRepository = merchantRepository;
        this.mNavigator = navigator;
    }

    public void pay(JSONObject data, IPaymentListener listener) {
        if (data == null) {
            Timber.i("Pay fail because json is null.");
            return;
        }
        mResponseListener = listener;
        Timber.d("start to process paying order: %s", data.toString());
        if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
            listener.onPayError(WebAppConstants.RETURN_CODE_NETWORK_ERRORS, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
            return;
        }

        try {
            showLoading();
            if (zpTransaction(data, ZPPaymentSteps.OrderSource_WebToApp)) {
                hideLoading();
                return;
            }

            if (orderTransaction(data, ZPPaymentSteps.OrderSource_WebToApp)) {
                hideLoading();
                return;
            }

            hideLoading();
            listener.onPayError(WebAppConstants.RETURN_CODE_INVALID_PARAMETERS, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
        } catch (IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
        }
    }

    void transferMoney(JSONObject data, IPaymentListener listener) {
        if (data == null) {
            Timber.i("Pay fail because json is null.");
            return;
        }
        mResponseListener = listener;
        Timber.d("start to process transfer money: %s", data.toString());
        if (!NetworkHelper.isNetworkAvailable(mView.getContext())) {
            listener.onPayError(WebAppConstants.RETURN_CODE_NETWORK_ERRORS, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
            return;
        }

        try {
            showLoading();
            if (zpTransfer(data)) {
                hideLoading();
                return;
            }

            hideLoading();
            listener.onPayError(WebAppConstants.RETURN_CODE_INVALID_PARAMETERS, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
        } catch (IllegalArgumentException e) {
            Timber.i("Invalid JSON input: %s", e.getMessage());
        }
    }

    private boolean zpTransfer(JSONObject jsonObject) {
        mZPTransfer = new ZPTransfer(jsonObject);
        boolean isValidZPTransfer = mZPTransfer.isValid();

        Timber.d("Trying with zptransfermoney [%s] activity [%s]", isValidZPTransfer, getActivity());
        if (isValidZPTransfer) {
            getUserInfo(mZPTransfer.zpid);
        }
        return isValidZPTransfer;
    }

    private void getUserInfo(String zpName) {
        showLoading();
        Subscription subscription = mAccountRepository.getUserInfoByZaloPayName(zpName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new WebAppPresenter.UserInfoSubscriber(zpName));
        mSubscription.add(subscription);
    }

    void showLoading() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    void hideLoading() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    void initWebView(ZPWebViewApp webView) {
        mWebViewProcessor = new ZPWebViewAppProcessor(webView, mView);
        mWebViewProcessor.registerNativeModule(new WebAppNativeModule(mProcessMessageListener));

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                Timber.d("WebLoading progress: %s", progress);
                mView.updateLoadProgress(progress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (view.canGoBack()) {
                    setHomeDisplayStatus(false);
                } else {
                    setHomeDisplayStatus(true);
                    title = getActivity().getString(R.string.promotion_title);
                }

                mView.onReceivedTitle(title);
            }
        });
    }

    private void setHomeDisplayStatus(boolean isHome) {
        if(isHome) {
            mView.setHiddenBackButton(true);
            mView.setHiddenShareButton(true);
            mView.setHiddenTabBar(false);
        } else {
            mView.setHiddenBackButton(false);
            mView.setHiddenShareButton(false);
            mView.setHiddenTabBar(true);
        }
    }

    public void shareContent(String url) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        getActivity().startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || requestCode != TRANSFER_MONEY_WEB_APP_REQUEST_CODE) {
            return;
        }

        if (mResponseListener == null) {
            return;
        }

        Bundle result = data.getExtras();
        int code = result.getInt("code");
        String param = result.getString("param");
        if (code == 1) {
            mResponseListener.onPaySuccess();
        } else {
            code = Arrays.asList(mZPTransfer.errorCodeList).contains(code) ? code : WebAppConstants.RETURN_CODE_OTHER_ERRORS;
            mResponseListener.onPayError(code, param);
        }
    }

    @Override
    public void onPayParameterError(String param) {
        if (mResponseListener != null) {
            mResponseListener.onPayError(param);
        }
    }

    @Override
    public void onPayResponseError(PaymentError paymentError) {
        if (mResponseListener == null) {
            return;
        }

        if (paymentError == PaymentError.ERR_CODE_USER_CANCEL) {
            mResponseListener.onPayError(WebAppConstants.RETURN_CODE_USER_CANCEL, PaymentError.getErrorMessage(paymentError));
        } else {
            mResponseListener.onPayError(PaymentError.getErrorMessage(paymentError));
        }
    }

    @Override
    public void onPayResponseSuccess(IBuilder builder) {
        if (mResponseListener != null) {
            mResponseListener.onPaySuccess();
        }
    }

    @Override
    public void onPayAppError(String msg) {
        if (mResponseListener != null) {
            mResponseListener.onPayError(msg);
        }
    }

    /**
     * notify the presenter that view is resumed (onResume on Activity, Fragment)
     */
    @Override
    public void resume() {
        super.resume();
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onResume();
        }
    }

    /**
     * notify the presenter that view is destroyed (onDestroy on Activity, Fragment)
     */
    @Override
    public void destroy() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroy();
        }

        super.destroy();
    }

    /**
     * Call to remove/detach the attached view from presenter.
     * This is done to break the memory reference between presenter and view, so the GC will
     * know how to collect them.
     * <p>
     * detachView is called when the view is about to be destroyed by Android framework
     */
    @Override
    public void detachView() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroyView();
        }

        super.detachView();
    }

    /**
     * notify the presenter that view is paused (onPause on Activity, Fragment)
     */
    @Override
    public void pause() {
        super.pause();
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onPause();
        }
    }

    private void onGetProfileSuccess(Person person, String zaloPayName) {
        Timber.d("Got profile for %s: %s", zaloPayName, person);

        TransferObject object = new TransferObject(person);
        object.amount = mZPTransfer.amount;
        object.message = mZPTransfer.message;
        object.activateSource = Constants.ActivateSource.FromWebApp_QRType2;
        object.transferMode = Constants.TransferMode.TransferToZaloPayID;

        mNavigator.startTransferActivity(getFragment(), object, TRANSFER_MONEY_WEB_APP_REQUEST_CODE);
    }

    public WebBottomSheetDialogFragment createBottomSheetFragment() {
        Bundle bundle = new Bundle();
        bundle.putString(WebAppConstants.PARAM_CURRENT_URL, mWebViewProcessor.getCurrentUrl());
        WebBottomSheetDialogFragment bottomSheetDialog = WebBottomSheetDialogFragment.newInstance(bundle);
        bottomSheetDialog.setArguments(bundle);
        bottomSheetDialog.setBottomSheetEventListener(this);
        return bottomSheetDialog;
    }

    /**
     * Called when User choose to refresh current page.
     * Expected behavior: the host invoke webview to reload current url
     */
    @Override
    public void onRequestRefreshPage() {
        Timber.d("Request to reload web view");
        mWebViewProcessor.refreshWeb(getActivity());
        mView.hideError();
        mView.dismissBottomSheet();
        mView.setRefreshing(false);
    }

    public boolean onBackPressed() {
        return mWebViewProcessor != null && mWebViewProcessor.onBackPress();
    }

    void loadUrl(String url) {
        if (mWebViewProcessor == null) {
            return;
        }

        mWebViewProcessor.start(url, getActivity());
    }

    boolean isLoadPageFinished() {
        return mWebViewProcessor != null && mWebViewProcessor.isLoadPageFinished();
    }

    private class UserInfoSubscriber extends DefaultSubscriber<Person> {

        String zaloPayName;

        UserInfoSubscriber(String zaloPayName) {
            this.zaloPayName = zaloPayName;
        }

        @Override
        public void onError(Throwable e) {
            hideLoading();
            String message = ErrorMessageFactory.create(getFragment().getContext(), e);
            mResponseListener.onPayError(WebAppConstants.RETURN_CODE_INVALID_PARAMETERS, message);
        }

        @Override
        public void onNext(Person person) {
            hideLoading();
            onGetProfileSuccess(person, zaloPayName);
        }
    }
}
