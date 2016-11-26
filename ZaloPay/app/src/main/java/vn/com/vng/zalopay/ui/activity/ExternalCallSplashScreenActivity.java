package vn.com.vng.zalopay.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by huuhoa on 11/26/16.
 * Activity for receiving external calls
 */

public class ExternalCallSplashScreenActivity extends BaseActivity {
    @Inject
    ApplicationState mApplicationState;

    @Inject
    Navigator mNavigator;

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
            if (mApplicationState.currentState() == ApplicationState.State.MAIN_SCREEN_CREATED) {
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
                bundle.putInt(vn.com.vng.zalopay.Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_ZALO);
                bundle.putParcelable(vn.com.vng.zalopay.Constants.ARG_TRANSFERRECENT, item);
                mNavigator.startTransferActivity(this, bundle, true);
                finish();
                return;
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
}
