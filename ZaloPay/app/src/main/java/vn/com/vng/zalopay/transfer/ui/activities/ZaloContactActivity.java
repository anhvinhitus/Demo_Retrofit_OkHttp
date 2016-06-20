package vn.com.vng.zalopay.transfer.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.fragment.ZaloContactFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class ZaloContactActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ZaloContactFragment.newInstance(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
