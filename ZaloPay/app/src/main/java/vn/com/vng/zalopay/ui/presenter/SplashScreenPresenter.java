package vn.com.vng.zalopay.ui.presenter;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.event.ZaloIntegrationEvent;
import vn.com.vng.zalopay.ui.view.ISplashScreenView;
import vn.com.vng.zalopay.utils.IntroAppUtils;

/**
 * Created by AnhHieu on 5/13/16.
 * Splash startup
 */
@Singleton
public class SplashScreenPresenter extends BaseAppPresenter implements IPresenter<ISplashScreenView> {
    private final UserConfig mUserConfig;
    private ISplashScreenView mView;
    private final EventBus mEventBus;
    private final ApplicationState mApplicationState;

    @Inject
    SplashScreenPresenter(EventBus eventBus, UserConfig userConfig, ApplicationState applicationState) {
        mEventBus = eventBus;
        mUserConfig = userConfig;
        mApplicationState = applicationState;
    }

    @Override
    public void setView(ISplashScreenView iSplashScreenView) {
        mView = iSplashScreenView;
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

    public void verifyUser() {
        if (mUserConfig.hasCurrentUser()) {
            if (mApplicationState.currentState() == ApplicationState.State.MAIN_SCREEN_CREATED) {
                Timber.d("MainActivity is already created. Skip navigation");
                return;
            }

            Timber.i("go to Home Screen");
            getZaloProfileInfo(mView.getContext(), mUserConfig);
            mView.gotoHomeScreen();
        } else if (IntroAppUtils.isShowedIntro()) {
            Timber.d("gotoLoginScreen");
            mView.gotoLoginScreen();
        } else {
            Timber.d("gotoOnBoardingScreen");
            mView.gotoOnBoardingScreen();
        }
    }

    public void handleDeepLinks(Intent intent) {
        // Test : adb shell 'am start -d "zalopay-1://post?appid={}&zptranstoken={}"'
        if (intent == null) {
            Timber.d("Intent is null");
            return;
        }

        String action = intent.getAction();
        Timber.d("Launch from intent action: %s", action);

        if (handleIntentActionFilter(intent, action)) {
            return;
        }

        if (intent.getData() != null) {
            Uri data = intent.getData();
            String link = String.valueOf(data);
            Timber.d("handle deep links [%s]", link);

            String scheme = data.getScheme();
            String host = data.getHost();

            if (scheme.equalsIgnoreCase("zalopay-1") && host.equalsIgnoreCase("post")) {
                pay(data, false);
            } else if (scheme.equalsIgnoreCase("zalopay") && host.equalsIgnoreCase("pay")) {
                pay(data, true);
            }
        }
    }

    private boolean handleIntentActionFilter(Intent intent, String action) {
        if (TextUtils.isEmpty(action)) {
            return false;
        }

        if (!action.equalsIgnoreCase("vn.zalopay.intent.action.SEND_MONEY")) {
            return false;
        }

        String appId = intent.getStringExtra("android.intent.extra.APPID");
        String receiverId = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_ID");
        String receiverName = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_NAME");
        String receiverAvatar = intent.getStringExtra("vn.zalopay.intent.extra.RECEIVER_AVATAR");
        String type = intent.getStringExtra("vn.zalopay.intent.extra.TYPE");

        if (TextUtils.isEmpty(appId) ||
                TextUtils.isEmpty(receiverId) ||
                TextUtils.isEmpty(receiverName) ||
                TextUtils.isEmpty(receiverAvatar) ||
                TextUtils.isEmpty(type)) {
            Timber.d("Missing required parameters for handling SEND_MONEY");
            return false;
        }

        if (appId.equalsIgnoreCase("Zalo") && type.equalsIgnoreCase("SEND_MONEY")) {
            Timber.d("All required parameters are valid. Launching SEND MONEY");

            try {
                long value = Long.parseLong(receiverId);
                mEventBus.postSticky(new ZaloIntegrationEvent(
                        ZaloIntegrationEvent.EventType.SEND_MONEY,
                        value,
                        receiverName,
                        receiverAvatar));
                return true;
            } catch (NumberFormatException e) {
                Timber.d("Invalid number for receiverId: %s", receiverId);
                return false;
            }
        }
        return false;
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
    }
}
