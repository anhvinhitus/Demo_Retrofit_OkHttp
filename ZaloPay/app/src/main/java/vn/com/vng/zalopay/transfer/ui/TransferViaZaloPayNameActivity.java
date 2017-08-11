package vn.com.vng.zalopay.transfer.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.transfer.model.TransferMode;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class TransferViaZaloPayNameActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTFViaZPIDTracker = new ActivityTracker(ZPScreens.MONEYTRANSFER_INPUTZPID, -1, ZPEvents.MONEYTRANSFER_ZPID_TOUCH_BACK);
    private final ActivityTracker mActivityTFViaPNTracker = new ActivityTracker(ZPScreens.MONEYTRANSFER_INPUTZPID, -1, ZPEvents.MONEYTRANSFER_ZPID_TOUCH_BACK);
    private String mTransferMode = null;

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        if (mTransferMode != null && mTransferMode.equals(TransferMode.PHONE_NUMBER))
            return mActivityTFViaPNTracker;

        return mActivityTFViaZPIDTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        // get transfer mode from bundle
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(Constants.TRANSFER_MODE))
            mTransferMode = bundle.getString(Constants.TRANSFER_MODE);
        return TransferViaZaloPayNameFragment.newInstance(mTransferMode);
    }
}
