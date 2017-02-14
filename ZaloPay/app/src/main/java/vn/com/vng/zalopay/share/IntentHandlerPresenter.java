package vn.com.vng.zalopay.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

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
                handleZaloIntegration(data.getData());
            }
        } else {
            ActivityCompat.finishAffinity((Activity) mView.getContext());
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

            if (host.equalsIgnoreCase("app") && "/transfer".equalsIgnoreCase(pathPrefix)) {
                handleZaloIntegration(data);
            } else {
                finish(true);
            }

        } else {
            finish(true);
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
                startLogin((IntentHandlerActivity) mView.getContext(), ZALO_INTEGRATION_LOGIN_REQUEST_CODE, data, sender, accesstoken);
                shouldFinishCurrentActivity = false;
                return;
            }

            if (mView == null) {
                Timber.d("mView IntentHandler is Null");
                return;
            }

            long ownerZaloId = mUserConfig.getZaloId();
            Timber.d("sender %s receiver %s ownerZaloId %s", sender, receiver, ownerZaloId);

            if (ownerZaloId != sender) {
                Timber.d("show dialog: %s", ((Activity) mView.getContext()).isFinishing());
                signInAnotherAccount(data, sender, accesstoken);
                shouldFinishCurrentActivity = false;
                return;
            }

            HandleZaloIntegration payment = new HandleZaloIntegration();
            payment.initialize();
            payment.getBalance();

            Timber.d("Processing send money on behalf of Zalo request");
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

        Activity activity = ((Activity) mView.getContext());

        if (!removeTask) {
            activity.finish();
            return;
        }

        if (!activity.isTaskRoot()) {
            Timber.d("move task to back");
            activity.moveTaskToBack(true);
            activity.finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            Timber.d("finish and remove task");
            activity.finishAndRemoveTask();
        } else {
            activity.finish();
        }
    }

    private void startLogin(IntentHandlerActivity act, int requestCode, Uri data, long zaloid, String authCode) {
        mNavigator.startLoginActivity(act, requestCode, data, zaloid, authCode);
    }


    private void signInAnotherAccount(final Uri data, final long sender, final String accesstoken) {
        mDialog = DialogHelper.yesNoDialog((Activity) mView.getContext(), mApplicationContext.getString(R.string.confirm_change_account),
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
                        startLogin((IntentHandlerActivity) mView.getContext(), ZALO_INTEGRATION_LOGIN_REQUEST_CODE, data, sender, accesstoken);
                    }
                });
        mDialog.show();
    }

    private void startTransfer(long sender, long receiver) {
        RecentTransaction item = new RecentTransaction();
        item.zaloId = receiver;
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_ZALO);
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        mNavigator.startTransferActivity(mView.getContext(), bundle);
    }

    @Override
    public void destroy() {
        Timber.d("destroy: call");
        super.destroy();
    }
}