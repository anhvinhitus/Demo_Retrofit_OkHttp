package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 6/30/16.
 */
public class UpdateProfile3Fragment extends BaseFragment {


    public static UpdateProfile3Fragment newInstance() {

        Bundle args = new Bundle();

        UpdateProfile3Fragment fragment = new UpdateProfile3Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_update_profile_3;
    }
}
