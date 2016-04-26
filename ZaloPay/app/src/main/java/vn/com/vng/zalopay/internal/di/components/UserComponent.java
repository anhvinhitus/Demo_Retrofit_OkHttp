package vn.com.vng.zalopay.internal.di.components;

import dagger.Subcomponent;
import vn.com.vng.zalopay.internal.di.modules.user.DomainUserModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserPresenterModule;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;

@UserScope
@Subcomponent(
        modules = {
                UserModule.class,
                DomainUserModule.class,
                UserPresenterModule.class
        }
)
public interface UserComponent {
    void inject(ZaloPayFragment f);
}