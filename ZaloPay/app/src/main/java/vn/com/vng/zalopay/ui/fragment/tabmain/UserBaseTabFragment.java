package vn.com.vng.zalopay.ui.fragment.tabmain;

import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.user.UserBaseActivity;

/**
 * Created by hieuvm on 7/20/17.
 * *
 */

public abstract class UserBaseTabFragment extends RuntimePermissionFragment {

    @Override
    public UserComponent getUserComponent() {
        UserComponent userComponent = super.getUserComponent();
        if (userComponent != null) {
            return userComponent;
        }

        if (!(getContext() instanceof UserBaseActivity)) {
            throw new IllegalStateException("This activity isn't an instance of UserBaseActivity");
        }

        userComponent = ((UserBaseActivity) getContext()).getUserComponent();

        if (userComponent == null) {
            throw new IllegalStateException("UserComponent already release");
        }

        return userComponent;

    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {

    }
}
