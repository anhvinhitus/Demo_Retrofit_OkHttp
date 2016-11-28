package vn.com.vng.zalopay.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.balance.BalanceRepository;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by huuhoa on 11/26/16.
 * Activity for receiving external calls
 */

public class ExternalCallSplashScreenActivity extends BaseActivity {
    @Inject
    ApplicationState mApplicationState;

    @Inject
    Navigator mNavigator;

    @Inject
    EventBus mEventBus;

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
        Timber.d("onCreate new ExternalCallSplashScreenActivity");

        Intent intent = getIntent();
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Timber.d("Launching with empty action");
            finish();
            return;
        }

        Timber.d("Launching with action: %s", action);
        if ("vn.zalopay.intent.action.SEND_MONEY".equals(action)) {
            if (handleZaloIntegration(intent)) {
                return;
            }
        }

        if (Intent.ACTION_VIEW.equals(action)) {
            handleDeepLink(intent.getData());
        }
    }

    private boolean handleZaloIntegration(Intent intent) {
        if (mApplicationState.currentState() != ApplicationState.State.MAIN_SCREEN_CREATED) {
            return false;
        }

        String appId = intent.getStringExtra("android.intent.extra.APPID");
        String receiverId = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_ID");
        String receiverName = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_NAME");
        String receiverAvatar = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_AVATAR");
        String type = intent.getStringExtra("vn.zalopay.intent.extra.TYPE");

        Timber.d("Processing send money on behalf of Zalo request");
        RecentTransaction item = new RecentTransaction();
        item.zaloId = Long.parseLong(receiverId);
        item.displayName = receiverName;
        item.avatar = receiverAvatar;

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_ZALO);
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        mNavigator.startTransferActivity(this, bundle, true);
        finish();
        return true;
    }

    private void handleDeepLink(Uri data) {
        if (data == null) {
            return;
        }

        Timber.d("handle deep links [%s]", data);

        String scheme = data.getScheme();
        String host = data.getHost();

        if (scheme.equalsIgnoreCase("zalopay-1") && host.equalsIgnoreCase("post")) {
            pay(data, false);
        } else if (scheme.equalsIgnoreCase("zalopay") && host.equalsIgnoreCase("zalopay.vn")) {
            handleAppToAppPayment(data);
        }
    }

    private void pay(Uri data, boolean isAppToApp) {
        String appid = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.APPID);
        String zptranstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ZPTRANSTOKEN);

        if (TextUtils.isEmpty(appid)) {
            return;
        }

        if (!TextUtils.isDigitsOnly(appid)) {
            return;
        }

        if (TextUtils.isEmpty(zptranstoken)) {
            return;
        }

        mEventBus.postSticky(new PaymentDataEvent(Long.parseLong(appid), zptranstoken, isAppToApp));
        Timber.d("post sticky payment");
        finish();
    }

    private void handleAppToAppPayment(Uri data) {
        String appid = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.APPID);
        String zptranstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ZPTRANSTOKEN);

        boolean shouldFinishCurrentActivity = true;
        try {
            if (TextUtils.isEmpty(appid)) {
                return;
            }

            if (!TextUtils.isDigitsOnly(appid)) {
                return;
            }

            if (TextUtils.isEmpty(zptranstoken)) {
                return;
            }

            if (mApplicationState.currentState() != ApplicationState.State.MAIN_SCREEN_CREATED) {
                return;
            }

            HandleInAppPayment payment = new HandleInAppPayment(this);
            payment.initialize();
            payment.start(Long.parseLong(appid), zptranstoken);

            shouldFinishCurrentActivity = false;
        } finally {
            if (shouldFinishCurrentActivity) {
                finish();
            }
        }
    }

    /**
     * Handle onNewIntent() to inform the fragment manager that the
     * state is not saved.  If you are handling new intents and may be
     * making changes to the fragment state, you want to be sure to call
     * through to the super-class here first.  Otherwise, if your state
     * is saved but the activity is not stopped, you could get an
     * onNewIntent() call which happens before onResume() and trying to
     * perform fragment operations at that point will throw IllegalStateException
     * because the fragment manager thinks the state is still saved.
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Timber.d("onNewIntent for ExternalCallSplashScreenActivity");
    }

    public static class HandleInAppPayment {

        private final Activity mActivity;
        private PaymentWrapper paymentWrapper;

        @Inject
        BalanceStore.Repository mBalanceRepository;

        @Inject
        ZaloPayRepository mZaloPayRepository;

        @Inject
        TransactionStore.Repository mTransactionRepository;

        HandleInAppPayment(Activity activity) {
            mActivity = activity;
        }

        void initialize() {
            AndroidApplication.instance().getUserComponent().inject(this);

            if (paymentWrapper == null) {
                paymentWrapper = new PaymentWrapper(mBalanceRepository, mZaloPayRepository, mTransactionRepository, new PaymentWrapper.IViewListener() {
                    @Override
                    public Activity getActivity() {
                        return mActivity;
                    }
                }, new PaymentWrapper.IResponseListener() {
                    private String mTransactionId;
                    @Override
                    public void onParameterError(String param) {
                        Timber.d("onParameterError: %s", param);
                        Intent data = new Intent();

                        data.putExtra("code", -1);
                        mActivity.setResult(RESULT_OK, data);
                        mActivity.finish();
                    }

                    @Override
                    public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {
                        mTransactionId = transId;
                    }

                    @Override
                    public void onResponseError(PaymentError paymentError) {
                        Timber.d("onResponseError: %s", paymentError);
                        Intent data = new Intent();

                        data.putExtra("code", paymentError.value());
                        mActivity.setResult(RESULT_OK, data);
                        mActivity.finish();
                    }

                    @Override
                    public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                        Timber.d("onResponseSuccess");
                        Intent data = new Intent();

                        data.putExtra("code", 1);
                        data.putExtra("transactionId", mTransactionId);
                        mActivity.setResult(RESULT_OK, data);
                        mActivity.finish();
                    }

                    @Override
                    public void onResponseTokenInvalid() {
                    }

                    @Override
                    public void onAppError(String msg) {
                    }

                    @Override
                    public void onNotEnoughMoney() {
                    }
                });
            }
        }

        void start(long appId, String zptranstoken) {
            paymentWrapper.payWithToken(appId, zptranstoken);
        }
    }
}
