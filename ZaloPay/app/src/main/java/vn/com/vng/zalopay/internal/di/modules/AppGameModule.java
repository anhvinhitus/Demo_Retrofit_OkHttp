package vn.com.vng.zalopay.internal.di.modules;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.game.config.AppGameDialogImpl;
import vn.com.vng.zalopay.game.config.AppGameNetworkingImpl;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.zalopay.game.businnesslogic.provider.dialog.IDialog;
import vn.com.zalopay.game.businnesslogic.provider.networking.INetworking;

@Module
public class AppGameModule {

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
