package vn.com.vng.zalopay.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.controller.SDKPayment;

/**
 * Created by hieuvm on 2/14/17.
 */

public class IntentHandlerPresenter extends AbstractPresenter<IIntentHandlerView> {

    private static final int ZALO_INTEGRATION_LOGIN_REQUEST_CODE = 101;

    private UserConfig mUserConfig;

    private ApplicationState mApplicationState;

    private EventBus mEventBus;

    private Navigator mNavigator;
    private Context mApplicationContext;

    private ApplicationSession mApplicationSession;

    @Inject
    public IntentHandlerPresenter(Context context, UserConfig userConfig, ApplicationState applicationState,
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
            finish(true);
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
            if (requestCode == ZALO_INTEGRATION_LOGIN_REQUEST_CODE) {
                handleDeepLink(data.getData());
            }
        } else {
            ActivityCompat.finishAffinity(activity);
        }

    }

    private void handleDeepLink(Uri data) {
        if (data == null) {
            finish(true);
            return;
        }

        Timber.d("handle deep links [%s]", data);

        String scheme = data.getScheme();
        String host = data.getHost();
        String pathPrefix = data.getPath();

        if (TextUtils.isEmpty(scheme) || TextUtils.isEmpty(host)) {
            finish(true);
            return;
        }

        if (scheme.equalsIgnoreCase("zalopay-zapi-28")) {

            ZPAnalytics.trackEvent(ZPEvents.ZALO_LAUNCH_28);
            if (host.equalsIgnoreCase("app") && "/transfer".equalsIgnoreCase(pathPrefix)) {
                handleZaloIntegration(data);
            } else {
                finish(true);
            }

        } else if (scheme.equalsIgnoreCase("zalopay-zapi-29")) {

            if (host.equalsIgnoreCase("app") && "/mywallet".equalsIgnoreCase(pathPrefix)) {
                handMyWallet(data);
            } else {
                finish(true);
            }

        } else {
            finish(true);
        }
    }

    private void handMyWallet(final Uri data) {
        String senderId = data.getQueryParameter("sender");
        boolean shouldFinishCurrentActivity = true;
        try {
            final long sender;

            try {
                sender = Long.valueOf(senderId);
            } catch (NumberFormatException e) {
                Timber.e(e, "Argument is invalid senderId [%s]", senderId);
                return;
            }

            if (shouldSignIn(mView.getContext(), data, sender, "")) {
                shouldFinishCurrentActivity = false;
                return;
            }

            if (signInAnotherAccount(mView.getContext(), data, sender, "")) {
                shouldFinishCurrentActivity = false;
                return;
            }

            Activity activity = (Activity) mView.getContext();
            if (activity.isTaskRoot()) {
                mNavigator.startHomeActivity(activity, false);
            }

        } finally {
            if (shouldFinishCurrentActivity) {
                finish(false);
            }
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

            if (mView == null) {
                Timber.d("mView IntentHandler is Null");
                return;
            }


            if (shouldSignIn(mView.getContext(), data, sender, accesstoken)) {
                shouldFinishCurrentActivity = false;
                return;
            }

            if (signInAnotherAccount(mView.getContext(), data, sender, accesstoken)) {
                shouldFinishCurrentActivity = false;
                return;
            }

            if (insidePaymentOrder(mView.getContext())) {
                shouldFinishCurrentActivity = false;
                return;
            }

            HandleZaloIntegration payment = new HandleZaloIntegration();
            payment.initialize();
            if (mApplicationState.currentState() != ApplicationState.State.MAIN_SCREEN_CREATED) {
                Timber.d("need load payment sdk");
                payment.loadPaymentSdk();
            }

            payment.getBalance();

            startTransfer(sender, receiver);
            shouldFinishCurrentActivity = true;
        } finally {
            Timber.d("should finish current activity [%s] ", shouldFinishCurrentActivity);
            if (shouldFinishCurrentActivity) {
                finish(false);
            }
        }
    }

    private SweetAlertDialog mDialog;

    private void finish(boolean removeTask) {

        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mView == null) {
            return;
        }

        mView.finishActivity(removeTask);
    }

    private void startLogin(IntentHandlerActivity act, int requestCode, Uri data, long zaloid, String authCode) {
        mNavigator.startLoginActivity(act, requestCode, data, zaloid, authCode);
    }

    private boolean signInAnotherAccount(final Context context, final Uri data, final long sender, final String accesstoken) {
        long ownerZaloId = mUserConfig.getZaloId();
        Timber.d("sender %s ownerZaloId %s", sender, ownerZaloId);

        if (sender == ownerZaloId) {
            return false;
        }

        ZPAnalytics.trackEvent(ZPEvents.ZALO_NOTLOGIN);

        String messageFormat = context.getString(R.string.confirm_change_account_format);
        User user = mUserConfig.getCurrentUser();
        String message;
        if (user != null && !TextUtils.isEmpty(user.displayName)) {
            message = String.format(messageFormat, user.displayName);
        } else {
            message = String.format(messageFormat, context.getString(R.string.other_account));
        }

        mDialog = DialogHelper.yesNoDialog((Activity) context, message,
                mApplicationContext.getString(R.string.accept), mApplicationContext.getString(R.string.cancel),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        finish(true);
                    }

                    @Override
                    public void onOKevent() {
                        Timber.d("Change account");
                        mApplicationSession.clearUserSessionWithoutSignOut();
                        startLogin((IntentHandlerActivity) context, ZALO_INTEGRATION_LOGIN_REQUEST_CODE, data, sender, accesstoken);
                    }
                });
        mDialog.show();
        return true;
    }

    private void startTransfer(long sender, long receiver) {
        Timber.d("startTransfer sender %s receiver %s", sender, receiver);
        RecentTransaction item = new RecentTransaction();
        item.zaloId = receiver;
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_ZALO);
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        mNavigator.startTransferActivity(mView.getContext(), bundle);
    }

    private boolean insidePaymentOrder(final Context context) {

        if (!SDKPayment.isOpenSdk()) {
            return false;
        }

        if (SDKPayment.canCloseSdk()) {
            ZPAnalytics.trackEvent(ZPEvents.ZALO_PAYMENT_ISINCOMPLETED);
            try {
                SDKPayment.closeSdk();
            } catch (Exception e) {
                Timber.d(e, "close sdk error");
            }
            return false;
        }

        ZPAnalytics.trackEvent(ZPEvents.ZALO_PAYMENT_ISINPROGRESS);

        DialogHelper.showWarningDialog((Activity) context, mApplicationContext.getString(R.string.you_having_a_transaction_in_process), mApplicationContext.getString(R.string.accept),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        finish(true);
                    }

                    @Override
                    public void onOKevent() {
                        finish(true);
                    }
                });

        return true;
    }

    private boolean shouldSignIn(Context context, Uri data, long sender, String accesstoken) {
        if (mUserConfig.hasCurrentUser()) {
            return false;
        }

        ZPAnalytics.trackEvent(ZPEvents.ZALO_NOTLOGIN);
        startLogin((IntentHandlerActivity) context, ZALO_INTEGRATION_LOGIN_REQUEST_CODE,
                data, sender, accesstoken);
        return true;
    }
}