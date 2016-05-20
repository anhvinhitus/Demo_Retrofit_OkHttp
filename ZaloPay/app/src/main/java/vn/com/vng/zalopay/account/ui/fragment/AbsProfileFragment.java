package vn.com.vng.zalopay.account.ui.fragment;

import javax.inject.Inject;

import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by longlv on 20/05/2016.
 */
public abstract class AbsProfileFragment extends BaseFragment {

    @Inject
    Navigator navigator;

    public abstract void onClickContinue();

    @Override
    protected void setupFragmentComponent() {

    }

    @Override
    protected int getResLayoutId() {
        return 0;
    }

}
