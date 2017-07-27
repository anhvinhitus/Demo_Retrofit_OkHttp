package vn.com.vng.zalopay.transfer.ui;

import android.content.Intent;
import android.os.Build;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

public class TransferActivity extends UserBaseToolBarActivity {

    Constants.ActivateSource mActivateSource = Constants.ActivateSource.FromTransferActivity;


    /**
     * Tham khảo crash get intent null
     * https://fabric.io/zalo-pay/android/apps/vn.com.vng.zalopay/issues/59751e43be077a4dcc05f9c9?time=last-seven-days
     */
    @Override
    public BaseFragment getFragmentToHost() {
        if (getIntent() == null) {
            Timber.e("Intent transfer activity is null");
            return null;
        }

        getActivateSource(getIntent());
        return TransferFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getActivateSource(intent);
    }

    @Override
    public void finish() {

        Timber.d("Finish : Activate source [%s] ", mActivateSource);

        if (mActivateSource != Constants.ActivateSource.FromZalo) {
            super.finish();
            return;
        }

        if (!isTaskRoot()) {
            Timber.d("move task to back");
            moveTaskToBack(true);
            super.finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            Timber.d("finish and remove task");
            finishAndRemoveTask();
        } else {
            super.finish();
        }

    }

    private void getActivateSource(Intent intent) {
        TransferObject object = intent.getParcelableExtra(Constants.ARGUMENT_KEY_TRANSFER);
        if (object == null) {
            throw new RuntimeException("you should add transfer object.");
        }

        mActivateSource = object.activateSource;
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.MONEYTRANSFER_INPUTAMOUNT;
    }
}
