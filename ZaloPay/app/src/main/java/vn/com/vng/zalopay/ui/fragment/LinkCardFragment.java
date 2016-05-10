package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;

import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LinkCardFragment extends BaseFragment {


    public static LinkCardFragment newInstance() {

        Bundle args = new Bundle();

        LinkCardFragment fragment = new LinkCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_recycleview;
    }


}
