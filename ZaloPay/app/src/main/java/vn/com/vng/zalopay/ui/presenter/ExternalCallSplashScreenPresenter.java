package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.account.ui.activities.ChangePinActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.ExternalCallSplashScreenActivity;
import vn.com.vng.zalopay.ui.view.IExternalCallSplashScreenView;

/**
 * Created by hieuvm on 12/4/16.
 */

public class ExternalCallSplashScreenPresenter extends AbstractPresenter<IExternalCallSplashScreenView> {

    private static final int LOGIN_REQUEST_CODE = 100;
    private static final int ZALO_INTEGRATION_LOGIN_REQUEST_CODE = 101;

    private UserConfig mUserConfig;

    private ApplicationState mApplicationState;

    private EventBus mEventBus;

    private Navigator mNavigator;

    @Inject
    public ExternalCallSplashScreenPresenter(UserConfig userConfig, ApplicationState applicationState,
                                             EventBus eventBus, Navigator navigator) {
        this.mUserConfig = userConfig;
        this.mApplicationState = applicationState;
        this.mEventBus = eventBus;
        this.mNavigator = navigator;
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Timber.d("Launching with empty action");
            finish();
            return;
        }

        Timber.d("Launching with action: %s", action);
        if (Intent.ACTION_VIEW.equals(action)) {
            handleDeepLink(intent.getData());
        }
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult: requestCode [%s] resultCode [%s]", requestCode, resultCode);
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == LOGIN_REQUEST_CODE) {
                handleAppToAppPayment(data.getData());
            } else if(requestCode == ZALO_INTEGRATION_LOGIN_REQUEST_CODE) {
                handleZaloIntegration(data.getData(), false);
            }
        }

        finish();
    }

//    private boolean handleZaloIntegration(Intent intent) {
//        if (mApplicationState.currentState() != ApplicationState.State.MAIN_SCREEN_CREATED) {
//            return false;
//        }
//
//        String appId = intent.getStringExtra("android.intent.extra.APPID");
//        String receiverId = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_ID");
//        String receiverName = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_NAME");
//        String receiverAvatar = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_AVATAR");
//        String type = intent.getStringExtra("vn.zalopay.intent.extra.TYPE");
//
//        Timber.d("Processing send money on behalf of Zalo request");
//        RecentTransaction item = new RecentTransaction();
//        item.zaloId = Long.parseLong(receiverId);
//        item.displayName = receiverName;
//        item.avatar = receiverAvatar;
//
//        Bundle bundle = new Bundle();
//        bundle.putInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_ZALO);
//        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
//        mNavigator.startTransferActivity(mView.getContext(), bundle, true);
//        finish();
//        return true;
//    }

    private void handleZaloIntegration(Uri data, boolean shouldLogin) {
        if (mApplicationState.currentState() != ApplicationState.State.MAIN_SCREEN_CREATED) {
            return;
        }

        String accesstoken = data.getQueryParameter("accesstoken");
        String senderId = data.getQueryParameter("sender");
        String receiverId = data.getQueryParameter("receiver");

        if (accesstoken == null && shouldLogin == true) {
            Timber.d("start login activity");
            mNavigator.startLoginActivityForResult((ExternalCallSplashScreenActivity) mView.getContext(), ZALO_INTEGRATION_LOGIN_REQUEST_CODE, data);
            return;
        }

        Timber.d("Processing send money on behalf of Zalo request");
        RecentTransaction item = new RecentTransaction();
        item.zaloId = Long.parseLong(receiverId);

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_ZALO);
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        mNavigator.startTransferActivity(mView.getContext(), bundle, true);
        finish();
    }

    private void handleDeepLink(Uri data) {
        if (data == null) {
            finish();
            return;
        }

        Timber.d("handle deep links [%s]", data);

        String scheme = data.getScheme();
        String host = data.getHost();
        String pathPrefix = data.getPath();

        if (scheme.equalsIgnoreCase("zalopay-1") && host.equalsIgnoreCase("post")) {
            pay(data, false);
        } else if (scheme.equalsIgnoreCase("zalopay-zapi-28")) {

            if (host.equalsIgnoreCase("app") && pathPrefix.equalsIgnoreCase("/transfer")) {
                handleZaloIntegration(data, true);
            }

        } else if (scheme.equalsIgnoreCase("zalopay")) {

            if (host.equalsIgnoreCase("zalopay.vn")) {
                handleAppToAppPayment(data);
            } else if (host.equalsIgnoreCase("otp")) {
                handleOTPDeepLink(data);
                finish();
            } else {
                finish();
            }

        } else {
            finish();
        }
    }

    private boolean handleOTPDeepLink(Uri data) {
        if (!mUserConfig.hasCurrentUser()) {
            return false;
        }

        List<String> list = data.getPathSegments();
        if (Lists.isEmptyOrNull(list)) {
            return false;
        }

        String otp = list.get(0);
        Timber.d("handleDeepLink: %s", otp);
        if (TextUtils.isEmpty(otp) || !TextUtils.isDigitsOnly(otp)) {
            return false;
        }

        if (AppLifeCycle.isLastActivity(ChangePinActivity.class.getSimpleName())) {
            mNavigator.startChangePinActivity((Activity) mView.getContext(), otp);
        } else if (AppLifeCycle.isLastActivity(UpdateProfileLevel2Activity.class.getSimpleName())) {
            mNavigator.startUpdateProfileLevel2ActivityWithOtp(mView.getContext(), otp);
        }

        return true;
    }

    private void pay(Uri data, boolean isAppToApp) {
        Timber.d("pay with uri [%s] isAppToTpp [%s]", data, isAppToApp);

        String appid = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.APPID);
        String zptranstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ZPTRANSTOKEN);

        if (TextUtils.isEmpty(appid) ||
                !TextUtils.isDigitsOnly(appid) ||
                TextUtils.isEmpty(zptranstoken)) {
            finish();
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
            if (TextUtils.isEmpty(appid) ||
                    !TextUtils.isDigitsOnly(appid) ||
                    TextUtils.isEmpty(zptranstoken)) {
                return;
            }

            if (!mUserConfig.hasCurrentUser()) {
                Timber.d("start login activity");
                mNavigator.startLoginActivityForResult((ExternalCallSplashScreenActivity) mView.getContext(), LOGIN_REQUEST_CODE, data);
                shouldFinishCurrentActivity = false;
                return;
            }

            HandleInAppPayment payment = new HandleInAppPayment((Activity) mView.getContext());
            payment.initialize();
            if (mApplicationState.currentState() != ApplicationState.State.MAIN_SCREEN_CREATED) {
                Timber.d("need load payment sdk");
                payment.loadPaymentSdk();
            }

            payment.start(Long.parseLong(appid), zptranstoken);
            shouldFinishCurrentActivity = false;
        } finally {
            Timber.d("should finish current activity [%s] ", shouldFinishCurrentActivity);
            if (shouldFinishCurrentActivity) {
                finish();
            }
        }
    }

    private void finish() {
        if (mView != null) {
            ((Activity) mView.getContext()).finish();
        }
    }

}
