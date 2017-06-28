package vn.com.vng.zalopay.transfer.ui;

import android.content.Intent;
import android.os.Build;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

public class TransferActivity extends UserBaseToolBarActivity {

    Constants.ActivateSource mActivateSource = Constants.ActivateSource.FromTransferActivity;

    @Override
    public BaseFragment getFragmentToHost() {
        TransferObject object = getIntent().getParcelableExtra("transfer");
        mActivateSource = object.activateSource;
        return TransferFragment.newInstance(object);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        TransferObject object = intent.getParcelableExtra("transfer");
        mActivateSource = object.activateSource;
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
}
