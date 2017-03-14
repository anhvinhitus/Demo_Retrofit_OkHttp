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
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.activities.ChangePinActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
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
        mEventBus.unregister(this);
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
            } else {
                navigateToApp();
            }

        }
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

        String appid = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.APPID);
        String zptranstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ZPTRANSTOKEN);

        if (!validateOrderParams(appid, zptranstoken)) {
            finish();
            return;
        }

        Timber.d("post sticky payment");
        mEventBus.postSticky(new PaymentDataEvent(Long.parseLong(appid), zptranstoken, false));
        navigateToApp();
    }

    private void handleAppToAppPayment(Uri data) {

        if (data == null) {
            Timber.d("URI data is null");
            return;
        }

        Timber.d("handle uri %s", data.toString());

        String appid = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.APPID);
        String zptranstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ZPTRANSTOKEN);

        boolean shouldFinishCurrentActivity = true;
        try {

            if (!validateOrderParams(appid, zptranstoken)) {
                return;
            }

            if (mView == null) {
                Timber.d("mView is null");
                return;
            }

            if (shouldSignIn((ExternalCallSplashScreenActivity) mView.getContext(), LOGIN_REQUEST_CODE, data)) {
                shouldFinishCurrentActivity = false;
                return;
            }

            if (insidePaymentOrder(mView.getContext())) {
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

}
