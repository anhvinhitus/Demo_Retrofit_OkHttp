package vn.com.vng.zalopay.fingerprint;

import android.os.Bundle;
import android.view.View;

import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by hieuvm on 12/24/16.
 */

public class ProtectAccountFragment extends BaseFragment {
    public static ProtectAccountFragment newInstance() {

        Bundle args = new Bundle();

        ProtectAccountFragment fragment = new ProtectAccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_protect_account;
    }

    @OnClick(R.id.vgChangePass)
    public void onClickChangePassword(View v) {
        navigator.startChangePinActivity(getActivity());
    }
}
