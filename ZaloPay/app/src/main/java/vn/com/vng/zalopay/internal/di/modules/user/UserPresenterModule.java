package vn.com.vng.zalopay.internal.di.modules.user;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.account.ui.presenter.OTPProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.OTPRecoveryPinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.RecoveryPinPresenter;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.transfer.ZaloFriendsFactory;
import vn.com.vng.zalopay.transfer.ui.presenter.TransferPresenter;
import vn.com.vng.zalopay.transfer.ui.presenter.ZaloContactPresenter;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.presenter.LeftMenuPresenter;
import vn.com.vng.zalopay.ui.presenter.LinkCardPresenter;
import vn.com.vng.zalopay.ui.presenter.LinkCardProcedurePresenter;
import vn.com.vng.zalopay.ui.presenter.MainPresenter;
import vn.com.vng.zalopay.ui.presenter.QRCodePresenter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenterImpl;

@Module
public class UserPresenterModule {

    @UserScope
    @Provides
    QRCodePresenter provideProductPresenter() {
        return new QRCodePresenter();
    }

    @UserScope
    @Provides
    LeftMenuPresenter provideLeftMenuPresenter(User user) {
        return new LeftMenuPresenter(user);
    }

    @UserScope
    @Provides
    BalanceTopupPresenter provideBalanceTopupPresenter() {
        return new BalanceTopupPresenter();
    }

    @UserScope
    @Provides
    LinkCardPresenter provideLinkCardPresenter(User user) {
        return new LinkCardPresenter(user);
    }

    @UserScope
    @Provides
    LinkCardProcedurePresenter provideLinkCardProcedurePresenter(User user) {
        return new LinkCardProcedurePresenter(user);
    }

    @UserScope
    @Provides
    PreProfilePresenter providePreProfilePresenter(UserConfig userConfig) {
        return new PreProfilePresenter(userConfig);
    }

    @UserScope
    @Provides
    MainPresenter providerMainPresenter(ZaloFriendsFactory zaloFriendsFactory) {
        return new MainPresenter(zaloFriendsFactory);
    }

    @UserScope
    @Provides
    ZaloPayPresenter providerZaloPayPresenter() {
        return new ZaloPayPresenterImpl();
    }

    @UserScope
    @Provides
    ProfileInfoPresenter provideProfileInfoPresenter(UserConfig userConfig) {
        return new ProfileInfoPresenter(userConfig);

    }

    @UserScope
    @Provides
    ProfilePresenter provideProfilePresenter(UserConfig userConfig) {
        return new ProfilePresenter(userConfig);
    }

    @UserScope
    @Provides
    PinProfilePresenter providePinProfilePresenter(UserConfig userConfig) {
        return new PinProfilePresenter(userConfig);
    }

    @UserScope
    @Provides
    OTPProfilePresenter provideOTPProfilePresenter() {
        return new OTPProfilePresenter();
    }

    @UserScope
    @Provides
    RecoveryPinPresenter provideResetPassCodePresenter(UserConfig userConfig) {
        return new RecoveryPinPresenter(userConfig);
    }

    @UserScope
    @Provides
    OTPRecoveryPinPresenter provideOTPResetPassCodePresenter() {
        return new OTPRecoveryPinPresenter();
    }

    @UserScope
    @Provides
    ZaloContactPresenter provideZaloContactPresenter(ZaloFriendsFactory zaloFriendsFactory) {
        return new ZaloContactPresenter(zaloFriendsFactory);
    }

    @UserScope
    @Provides
    TransferPresenter provideTransferPresenter(User user, ZaloFriendsFactory zaloFriendsFactory) {
        return new TransferPresenter(user, zaloFriendsFactory);
    }
}
