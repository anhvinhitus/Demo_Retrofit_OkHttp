package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import java.util.Locale;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.pw.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.wallet.paymentinfo.IBuilder;

/**
 * Created by longlv on 02/06/2016.
 * Implement IPaymentService
 */
public class ReactPaymentServiceImpl implements IPaymentService {

    private final MerchantStore.Repository mMerchantRepository;
    private PaymentWrapper mPaymentWrapper;
    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ReactPaymentServiceImpl(MerchantStore.Repository zaloPayIAPRepository) {
        this.mMerchantRepository = zaloPayIAPRepository;
    }

    private void initializePaymentWrapper(final Promise promise){
        this.mPaymentWrapper = new PaymentWrapperBuilder()
                .setResponseListener(new PaymentResponseListener(promise))
                .build();
        this.mPaymentWrapper.initializeComponents();
    }

    @Override
    public void pay(Activity activity, final Promise promise, Order order) {
        initializePaymentWrapper(promise);
        this.mPaymentWrapper.payWithOrder(activity, order, ZPPaymentSteps.OrderSource_MerchantApp);
    }

    @Override
    public void pay(Activity activity, Promise promise, long appId, String transactionToken) {
        initializePaymentWrapper(promise);
        this.mPaymentWrapper.payWithToken(activity, appId, transactionToken, ZPPaymentSteps.OrderSource_MerchantApp);
    }

    private void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    private void reportInvalidParameter(Promise promise, String parameterName) {
        if (promise == null) {
            return;
        }

        String message = String.format(Locale.getDefault(), "invalid %s", parameterName);
        Timber.d("Invalid parameter [%s]", parameterName);
        Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), message);
    }

    @Override
    public void getUserInfo(Promise promise, long appId) {
        Timber.d("get user info appId %s", appId);
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribe(new MerchantUserInfoSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    public void destroyVariable() {
//        paymentListener = null;
        mPaymentWrapper = null;
        unsubscribeIfNotNull(compositeSubscription);
    }


    @Override
    public void shareMessageToOtherApp(Activity activity, String message) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        try {
            activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } catch (Exception e) {
            //empty
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPaymentWrapper != null) {
            mPaymentWrapper.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class PaymentResponseListener extends DefaultPaymentResponseListener {
        private final Promise mPromise;
        private final WritableMap mSuccessParams;

        public PaymentResponseListener(Promise promise) {
            mPromise = promise;
            mSuccessParams = Arguments.createMap();
        }

        @Override
        protected ILoadDataView getView() {
            return null;
        }

        @Override
        public void onParameterError(String param) {
            reportInvalidParameter(mPromise, param);
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            Helpers.promiseResolveError(mPromise, paymentError.value(),
                    PaymentError.getErrorMessage(paymentError));
        }

        @Override
        public void onResponseSuccess(IBuilder builder) {
            Helpers.promiseResolveSuccess(mPromise, mSuccessParams);
        }

        @Override
        public void onAppError(String msg) {
            Helpers.promiseResolveError(mPromise, PaymentError.ERR_CODE_SYSTEM.value(),
                    PaymentError.getErrorMessage(PaymentError.ERR_CODE_SYSTEM));
            destroyVariable();
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {
            super.onPreComplete(isSuccessful, pTransId, pAppTransId);
            mSuccessParams.putString("zptransid", pTransId);
            mSuccessParams.putString("apptransid", pAppTransId);
            mSuccessParams.putInt("result", isSuccessful ? 1 : 0);
        }
    }
}
