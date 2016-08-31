package vn.com.vng.zalopay.internal.di.modules;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.game.AppGameConfigImpl;
import vn.com.vng.zalopay.game.AppGameDialogImpl;
import vn.com.vng.zalopay.game.AppGameNetworkingImpl;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.zalopay.game.businnesslogic.provider.config.IGetUrlConfig;
import vn.com.zalopay.game.businnesslogic.provider.dialog.IDialog;
import vn.com.zalopay.game.businnesslogic.provider.networking.INetworking;

@Module
public class AppGameModule {
    @UserScope
    @Provides
    IGetUrlConfig provideConfig() {
        return new AppGameConfigImpl();
    }

    @UserScope
    @Provides
    INetworking provideNetworking() {
        return new AppGameNetworkingImpl();
    }

    @UserScope
    @Provides
    IDialog provideDialog() {
        return new AppGameDialogImpl();
    }
}
