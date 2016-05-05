package vn.com.vng.zalopay.internal.di.components;

import dagger.Subcomponent;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
import vn.com.vng.zalopay.domain.repository.ApplicationRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.modules.user.ApiUserModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserControllerModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserPresenterModule;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;

@UserScope
@Subcomponent(
        modules = {
                UserModule.class,
                ApiUserModule.class,
                UserControllerModule.class,
                UserPresenterModule.class
        }
)
public interface UserComponent {
    void inject(ZaloPayFragment f);

    User currentUser();

    AppConfigRepository appConfigRepository();

    ZaloPayRepository zaloPayRepository();

    ApplicationRepository applicationRepository();
}