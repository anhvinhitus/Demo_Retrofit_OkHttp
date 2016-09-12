package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Intent;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class TransferHomeActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferHomeFragment.newInstance();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == vn.com.vng.zalopay.Constants.REQUEST_CODE_TRANSFER) {
            if (resultCode == Activity.RESULT_OK) {
                getActivity().finish();
            }
        }
    }
}
