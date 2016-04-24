package vn.com.vng.zalopay.ui.fragment.tabmain;

import timber.log.Timber;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 4/21/16.
 */
public abstract class BaseMainFragment extends BaseFragment {


    protected abstract void onScreenVisible();

    private boolean mIsVisibleToUser = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Timber.d("setUserVisibleHint " + isVisibleToUser);

        if (isVisibleToUser && !mIsVisibleToUser) {
            onScreenVisible();
        }
        mIsVisibleToUser = isVisibleToUser;
    }

    public boolean isVisibleToUser() {
        return mIsVisibleToUser;
    }

}
