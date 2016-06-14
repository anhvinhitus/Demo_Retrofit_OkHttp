package vn.com.vng.zalopay.transfer.ui.activities;

import android.os.Bundle;

import javax.inject.Inject;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.models.TransferRecent;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferHomeFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class TransferHomeActivity extends BaseToolBarActivity implements TransferHomeFragment.OnListFragmentInteractionListener {

    @Inject
    Navigator navigator;

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferHomeFragment.newInstance(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserComponent().inject(this);
    }

    @Override
    public void onListFragmentInteraction(TransferRecent item) {

    }
}
