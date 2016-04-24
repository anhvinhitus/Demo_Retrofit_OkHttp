package vn.com.vng.zalopay.internal.di.components;

import dagger.Subcomponent;
import vn.com.vng.zalopay.internal.di.modules.user.UserControllerModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserPresenterModule;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.ui.fragment.HomeFragment;

@UserScope
@Subcomponent(
        modules = {
                UserModule.class,
                UserControllerModule.class,
                UserPresenterModule.class
        }
)
public interface UserComponent {
    void inject(HomeFragment f);
}