package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
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

        Timber.d("handle deep links [%s]", data);

        String scheme = data.getScheme();
        String host = data.getHost();
        Timber.d("handleDeepLink: host %s", host);

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
                finish();
            } else {
                navigateToApp();
            }

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

        return true;
    }

    private void pay(Uri data) {
        Timber.d("pay with uri [%s] isAppToApp [%s]", data);

        String appid = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.APPID);
        String zptranstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ZPTRANSTOKEN);

        if (TextUtils.isEmpty(appid) ||
                !TextUtils.isDigitsOnly(appid) ||
                TextUtils.isEmpty(zptranstoken)) {
            finish();
            return;
        }

        mEventBus.postSticky(new PaymentDataEvent(Long.parseLong(appid), zptranstoken, false));
        Timber.d("post sticky payment");
        navigateToApp();
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
                startLogin((ExternalCallSplashScreenActivity) mView.getContext(), LOGIN_REQUEST_CODE, data, 0, "");
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

    private void startLogin(ExternalCallSplashScreenActivity act, int requestCode, Uri data, long zaloid, String authCode) {
        mNavigator.startLoginActivity(act, requestCode, data, zaloid, authCode);
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

}
