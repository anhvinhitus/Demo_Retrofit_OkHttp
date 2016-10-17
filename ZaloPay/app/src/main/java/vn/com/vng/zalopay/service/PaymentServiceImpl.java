package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.bridge.Promise;
import com.zing.zalo.zalosdk.core.helper.FeedData;
import com.zing.zalo.zalosdk.oauth.ZaloPluginCallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import java.lang.ref.WeakReference;
import java.util.Locale;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 02/06/2016.
 * Implement IPaymentService
 */
public class PaymentServiceImpl implements IPaymentService {

    private final MerchantStore.Repository mMerchantRepository;
    private final BalanceStore.Repository mBalanceRepository;
    private final User mUser;
    private final TransactionStore.Repository mTransactionRepository;
    private PaymentWrapper mPaymentWrapper;
    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private Context mApplicationContext;

    public PaymentServiceImpl(Context context,
                              MerchantStore.Repository zaloPayIAPRepository,
                              BalanceStore.Repository balanceRepository,
                              User user,
                              TransactionStore.Repository transactionRepository) {

        this.mApplicationContext = context;
        this.mMerchantRepository = zaloPayIAPRepository;
        this.mBalanceRepository = balanceRepository;
        this.mUser = user;
        this.mTransactionRepository = transactionRepository;
    }

    @Override
    public void pay(Activity activity, final Promise promise, Order order) {

        final WeakReference<Activity> mWeakReference = new WeakReference<>(activity);

        this.mPaymentWrapper = new PaymentWrapper(mBalanceRepository, null, mTransactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mWeakReference.get();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                reportInvalidParameter(promise, param);
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                Helpers.promiseResolveError(promise, paymentError.value(),
                        PaymentError.getErrorMessage(paymentError));
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                Helpers.promiseResolveSuccess(promise, null);
            }

            @Override
            public void onResponseTokenInvalid() {
                Timber.d("onResponseTokenInvalid errorCode");
                /*Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_TOKEN_INVALID.value(),
                        PaymentError.getErrorMessage(PaymentError.ERR_CODE_TOKEN_INVALID));*/
                logout();
            }

            @Override
            public void onAppError(String msg) {
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_SYSTEM.value(),
                        PaymentError.getErrorMessage(PaymentError.ERR_CODE_SYSTEM));
                destroyVariable();
            }

            @Override
            public void onNotEnoughMoney() {
                navigator.startDepositActivity(AndroidApplication.instance().getApplicationContext());
            }

            @Override
            public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {

            }
        });

        this.mPaymentWrapper.payWithOrder(order);
    }

    private void logout() {
        Timber.d("logout");
        AndroidApplication.instance().getAppComponent().applicationSession().clearUserSession();
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
       /* FeedData feed = new FeedData();
        feed.setMsg(message);
        feed.setAppName(activity.getString(R.string.app_name));
        feed.setLink("http://news.zing.vn");
        feed.setLinkTitle("Zing News");
        feed.setLinkSource("http://news.zing.vn");
        feed.setLinkThumb(new String[]{"http://img.v3.news.zdn.vn/w660/Uploaded/xpcwvovb/2015_12_15/cua_kinh_2.jpg"});

        ZaloSDK.Instance.shareMessage(activity, feed, new ZaloPluginCallback() {
            @Override
            public void onResult(boolean b, int i, String s, String s1) {
                Timber.d("onResult: b [%s] i [% ] s [%s] s1 [%s]");

            }
        });*/

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        try {
            activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } catch (Exception e) {
            //empty
        }
    }
}
