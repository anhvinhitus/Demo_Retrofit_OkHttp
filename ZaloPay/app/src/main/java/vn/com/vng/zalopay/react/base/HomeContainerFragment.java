package vn.com.vng.zalopay.react.base;

import android.os.Bundle;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by hieuvm on 2/28/17.
 */

public class HomeContainerFragment extends BaseFragment {
    
    public static HomeContainerFragment newInstance() {

        Bundle args = new Bundle();

        HomeContainerFragment fragment = new HomeContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {

    }

    @Override
    protected int getResLayoutId() {
        return 0;
    }
}
