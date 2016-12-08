package vn.com.vng.zalopay.account.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinContainerFragment;
import vn.com.vng.zalopay.event.ReceiveOTPEvent;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;

public class ChangePinActivity extends BaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return ChangePinContainerFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate %s taskId %s", this, getTaskId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String otp = intent.getStringExtra("otp");
        Timber.d("onNewIntent: %s taskId %s ", this, getTaskId());
        if (!TextUtils.isEmpty(otp)) {
            eventBus.postSticky(new ReceiveOTPEvent(otp));
        }
    }

    @Override
    protected void onDestroy() {
        eventBus.removeStickyEvent(ReceiveOTPEvent.class);
        super.onDestroy();
    }
}
