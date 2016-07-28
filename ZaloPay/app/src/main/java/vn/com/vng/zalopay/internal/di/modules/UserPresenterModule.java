package vn.com.vng.zalopay.internal.di.modules;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.account.ui.presenter.OTPProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.OTPRecoveryPinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.RecoveryPinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.UpdateProfile3Presenter;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.transfer.ui.presenter.TransferPresenter;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.presenter.InvitationCodePresenter;
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
    MainPresenter providerMainPresenter(FriendStore.Repository repository) {
        return new MainPresenter(repository);
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
    ProfilePresenter provideProfilePresenter() {
        return new ProfilePresenter();
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
    RecoveryPinPresenter provideResetPassCodePresenter() {
        return new RecoveryPinPresenter();
    }

    @UserScope
    @Provides
    OTPRecoveryPinPresenter provideOTPResetPassCodePresenter() {
        return new OTPRecoveryPinPresenter();
    }

//    @UserScope
//    @Provides
//    ZaloContactPresenter provideZaloContactPresenter() {
//        return new ZaloContactPresenter();
//    }

    @UserScope
    @Provides
    TransferPresenter provideTransferPresenter(User user, TransferStore.LocalStorage localStorage) {
        return new TransferPresenter(user, localStorage);
    }

    @UserScope
    @Provides
    InvitationCodePresenter providesInvitationCodePresenter() {
        return new InvitationCodePresenter();
    }

    @UserScope
    @Provides
    UpdateProfile3Presenter providesUpdateProfile3Presenter() {
        return new UpdateProfile3Presenter();
    }
}
