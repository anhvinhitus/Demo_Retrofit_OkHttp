package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

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
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.ExternalCallSplashScreenActivity;
import vn.com.vng.zalopay.ui.view.IExternalCallSplashScreenView;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.controller.SDKPayment;

/**
 * Created by hieuvm on 12/4/16.
 */

public class ExternalCallSplashScreenPresenter extends AbstractPresenter<IExternalCallSplashScreenView> {

    private static final int LOGIN_REQUEST_CODE = 100;

    private final UserConfig mUserConfig;
    private final ApplicationState mApplicationState;
    private final EventBus mEventBus;
    private final Navigator mNavigator;
    private final Context mApplicationContext;

    private HandleInAppPayment mHandleInAppPayment;

    @Inject
    ExternalCallSplashScreenPresenter(Context context, UserConfig userConfig, ApplicationState applicationState,
                                      EventBus eventBus, Navigator navigator) {
        this.mUserConfig = userConfig;
        this.mApplicationState = applicationState;
        this.mEventBus = eventBus;
        this.mNavigator = navigator;
        this.mApplicationContext = context;
    }

    @Override
    public void resume() {
        super.resume();
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    @Override
    public void detachView() {
        if (mHandleInAppPayment != null) {
            mHandleInAppPayment.cleanUp();
        }
        super.detachView();
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
                handleDeepLink(data.getData());
                return;
            }
        }

        finish();
    }

    private void handleDeepLink(Uri data) {
        if (data == null) {
            finish();
            return;
        }

        Timber.d("handle deep links [%s]", data.toString());

        String scheme = data.getScheme();
        String host = data.getHost();

        if (TextUtils.isEmpty(scheme)) {
            finish();
            return;
        }

        if (TextUtils.isEmpty(host)) {
            navigateToApp();
            return;
        }

        if (scheme.equalsIgnoreCase("zalopay-1")) {

            if (host.equalsIgnoreCase("post")) {
                pay(data);
            } else {
                navigateToApp();
            }

        } else if (scheme.equalsIgnoreCase("zalopay")) {

            if (host.equalsIgnoreCase("zalopay.vn")) {
                handleAppToAppPayment(data);
            } else if (host.equalsIgnoreCase("otp")) {
                handleOTPDeepLink(data);
            } else if (host.equalsIgnoreCase("pay")) {
                handleWebToApp(data);
            } else {
                navigateToApp();
            }

        }
    }

    private void handleWebToApp(Uri data) {
        payOrder(data);
    }

    private boolean handleOTPDeepLink(Uri data) {

        try {

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

            int lengthOtp = mApplicationContext.getResources().getInteger(R.integer.max_length_otp);

            if (otp.length() != lengthOtp) {
                return false;
            }

            if (AppLifeCycle.isLastActivity(ChangePinActivity.class.getSimpleName())) {
                mNavigator.startChangePin((Activity) mView.getContext(), otp);
            } else if (AppLifeCycle.isLastActivity(UpdateProfileLevel2Activity.class.getSimpleName())) {
                mNavigator.startUpdateLevel2(mView.getContext(), otp);
            } else {
                Timber.d("No subscriber otp");
            }
        } finally {
            finish();
        }

        return true;
    }

    private void pay(Uri data) {
        Timber.d("pay with uri [%s] isAppToApp [%s]", data);

        String appid = data.getQueryParameter(Constants.APPID);
        String zptranstoken = data.getQueryParameter(Constants.ZPTRANSTOKEN);

        if (!validateOrderParams(appid, zptranstoken)) {
            finish();
            return;
        }

        Timber.d("post sticky payment");
        mEventBus.postSticky(new PaymentDataEvent(Long.parseLong(appid), zptranstoken, false));
        navigateToApp();
    }

    private void handleAppToAppPayment(Uri data) {
        payOrder(data);
    }

    private void payOrder(Uri data) {

        if (data == null) {
            Timber.d("URI data is null");
            return;
        }

        Timber.d("handle uri %s", data.toString());

        String appid = data.getQueryParameter(Constants.APPID);
        String zptranstoken = data.getQueryParameter(Constants.ZPTRANSTOKEN);
        String source = data.getQueryParameter("source");
        String browser = data.getQueryParameter("browser");

        boolean shouldFinishCurrentActivity = true;
        try {

            int codePreProcess = prepareIntegration(data, appid, zptranstoken);
            if (codePreProcess != PrepareIntegration.SUCCESS) {
                shouldFinishCurrentActivity = codePreProcess == PrepareIntegration.ERROR_AND_FINISH;
                return;
            }

            makePayment(Long.valueOf(appid), zptranstoken, source, browser);
            shouldFinishCurrentActivity = false;
        } finally {
            Timber.d("should finish current activity [%s] ", shouldFinishCurrentActivity);
            if (shouldFinishCurrentActivity) {
                finish();
            }
        }
    }

    private void makePayment(long appid, String zptranstoken, String source, String browser) {
        mHandleInAppPayment = new HandleInAppPayment((Activity) mView.getContext());
        mHandleInAppPayment.initialize();
        if (mApplicationState.currentState() != ApplicationState.State.MAIN_SCREEN_CREATED) {
            Timber.d("need load payment sdk");
            mHandleInAppPayment.loadPaymentSdk();
        }

        mHandleInAppPayment.doPay(appid, zptranstoken, source, browser);
    }

    private void finish() {
        if (mView != null) {
            ((Activity) mView.getContext()).finish();
        }
    }

    private void startLogin(ExternalCallSplashScreenActivity act, int requestCode, Uri data) {
        mNavigator.startLoginFromOtherTask(act, requestCode, data);
    }

    private void navigateToApp() {
        if (mView == null) {
            return;
        }
        Intent intent;
        if (mUserConfig.hasCurrentUser()) {
            intent = mNavigator.intentHomeActivity(mView.getContext(), false);

        } else {
            intent = mNavigator.getIntentLogin(mView.getContext(), false);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mView.getContext().startActivity(intent);
        finish();
    }

    private boolean validateOrderParams(String appid, String zptranstoken) {
        return !(TextUtils.isEmpty(appid) ||
                !TextUtils.isDigitsOnly(appid) ||
                TextUtils.isEmpty(zptranstoken));
    }

    private boolean shouldSignIn(ExternalCallSplashScreenActivity activity, int requestCode, Uri data) {
        if (mUserConfig.hasCurrentUser()) {
            return false;
        }

        startLogin(activity, requestCode, data);

        return true;
    }

    private boolean insidePaymentOrder(final Context context) {

        if (!SDKPayment.isOpenSdk()) {
            return false;
        }

        if (SDKPayment.canCloseSdk()) {
            try {
                SDKPayment.closeSdk();
            } catch (Exception e) {
                Timber.d(e, "close sdk error");
            }
            return false;
        }

        DialogHelper.showWarningDialog((Activity) context, mApplicationContext.getString(R.string.you_having_a_transaction_in_process), mApplicationContext.getString(R.string.accept),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        finish();
                    }

                    @Override
                    public void onOKevent() {
                        finish();
                    }
                });

        return true;
    }


    private int prepareIntegration(Uri data, String appid, String zptranstoken) {

        if (!validateOrderParams(appid, zptranstoken)) {
            return PrepareIntegration.ERROR_AND_FINISH;
        }

        if (mView == null) {
            Timber.d("mView is null");
            return PrepareIntegration.ERROR_AND_FINISH;
        }

        if (shouldSignIn((ExternalCallSplashScreenActivity) mView.getContext(), LOGIN_REQUEST_CODE, data)) {
            return PrepareIntegration.ERROR;
        }

        if (insidePaymentOrder(mView.getContext())) {
            return PrepareIntegration.ERROR;
        }

        return PrepareIntegration.SUCCESS;
    }

    private interface PrepareIntegration {
        int SUCCESS = 1;
        int ERROR_AND_FINISH = 0;
        int ERROR = -1;
    }

}
