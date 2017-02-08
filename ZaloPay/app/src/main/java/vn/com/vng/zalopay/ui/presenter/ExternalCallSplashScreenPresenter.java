package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.activities.ChangePinActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.ArgumentException;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.ExternalCallSplashScreenActivity;
import vn.com.vng.zalopay.ui.view.IExternalCallSplashScreenView;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

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
    private Context mApplicationContext;

    private ApplicationSession mApplicationSession;

    @Inject
    public ExternalCallSplashScreenPresenter(Context context, UserConfig userConfig, ApplicationState applicationState,
                                             EventBus eventBus, Navigator navigator, ApplicationSession applicationSession) {
        this.mUserConfig = userConfig;
        this.mApplicationState = applicationState;
        this.mEventBus = eventBus;
        this.mNavigator = navigator;
        this.mApplicationContext = context;
        this.mApplicationSession = applicationSession;
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
            if (requestCode == LOGIN_REQUEST_CODE) {
                handleAppToAppPayment(data.getData());
            } else if (requestCode == ZALO_INTEGRATION_LOGIN_REQUEST_CODE) {
                handleZaloIntegration(data.getData());
            }
        }

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
                handleZaloIntegration(data);
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

    private void handleZaloIntegration(final Uri data) {

        final String accesstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ACCESSTOKEN);
        String senderId = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.SENDER);
        String receiverId = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.RECEIVER);

        boolean shouldFinishCurrentActivity = true;
        try {

            final long sender;
            long receiver;

            try {
                sender = Long.valueOf(senderId);
                receiver = Long.valueOf(receiverId);
            } catch (NumberFormatException e) {
                Timber.e(e, "Argument is invalid senderId [%s] receiverId [%s]", senderId, receiverId);
                return;
            }

            if (!mUserConfig.hasCurrentUser()) {
                Timber.d("start login activity");
                mNavigator.startLoginActivityForResult((ExternalCallSplashScreenActivity) mView.getContext(), ZALO_INTEGRATION_LOGIN_REQUEST_CODE,
                        data, sender, accesstoken);
                shouldFinishCurrentActivity = false;
                return;
            }

            long ownerZaloId = mUserConfig.getZaloId();

            Timber.d("sender %s receiver %s ownerZaloId %s", sender, receiver, ownerZaloId);

            if (ownerZaloId != sender) {
                Timber.d("show dialog: %s", ((Activity) mView.getContext()).isFinishing());
                mDialog = DialogHelper.yesNoDialog((Activity) mView.getContext(), mApplicationContext.getString(R.string.confirm_change_account),
                        mApplicationContext.getString(R.string.accept), mApplicationContext.getString(R.string.cancel),
                        new ZPWOnEventConfirmDialogListener() {
                            @Override
                            public void onCancelEvent() {
                                finish();
                            }

                            @Override
                            public void onOKevent() {
                                Timber.d("Change account");
                                mApplicationSession.clearUserSessionWithoutSignOut();
                                mNavigator.startLoginActivityForResult((ExternalCallSplashScreenActivity) mView.getContext(),
                                        ZALO_INTEGRATION_LOGIN_REQUEST_CODE, data, sender, accesstoken);
                            }
                        });
                mDialog.show();
                shouldFinishCurrentActivity = false;
                return;
            }

            HandleZaloIntegration payment = new HandleZaloIntegration();
            payment.initialize();
            payment.getBalance();


            if (mView == null) {
                return;
            }

            Timber.d("Processing send money on behalf of Zalo request");
            RecentTransaction item = new RecentTransaction();
            item.zaloId = receiver;

            Bundle bundle = new Bundle();
            bundle.putInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_ZALO);
            bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
            mNavigator.startTransferActivity(mView.getContext(), bundle, true);
            shouldFinishCurrentActivity = true;
        } finally {
            Timber.d("should finish current activity [%s] ", shouldFinishCurrentActivity);
            if (shouldFinishCurrentActivity) {
                finish();
            }
        }
    }

    private SweetAlertDialog mDialog;

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

        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mView != null) {
            ((Activity) mView.getContext()).finish();
        }
    }

}
