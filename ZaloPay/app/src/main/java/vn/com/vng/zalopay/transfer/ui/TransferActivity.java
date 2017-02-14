package vn.com.vng.zalopay.transfer.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class TransferActivity extends BaseToolBarActivity {

    int mTransferMode = Constants.MoneyTransfer.MODE_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMode(getIntent());
        Timber.d("onCreate: taskid %s", getTaskId());
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initMode(intent);
        Timber.d("onNewIntent: ");
    }

    private void initMode(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mTransferMode = bundle.getInt(Constants.ARG_MONEY_TRANSFER_MODE, Constants.MoneyTransfer.MODE_DEFAULT);
        }

    }

    @Override
    public void finish() {

        Timber.d("finish mode %s ", mTransferMode);

        if (mTransferMode != Constants.MoneyTransfer.MODE_ZALO) {
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
