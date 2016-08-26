package vn.com.vng.zalopay.internal.di.modules;

import android.content.SharedPreferences;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import vn.com.vng.zalopay.account.ui.presenter.ChangePinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.EditAccountNamePresenter;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.OTPProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.OTPRecoveryPinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.UpdateProfile3Presenter;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.transfer.ui.presenter.TransferHomePresenter;
import vn.com.vng.zalopay.transfer.ui.presenter.TransferPresenter;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.presenter.InvitationCodePresenter;
import vn.com.vng.zalopay.ui.presenter.LeftMenuPresenter;
import vn.com.vng.zalopay.ui.presenter.LinkCardPresenter;
import vn.com.vng.zalopay.ui.presenter.MainPresenter;
import vn.com.vng.zalopay.ui.presenter.QRCodePresenter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenterImpl;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawConditionPresenter;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawHomePresenter;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawPresenter;

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
    LinkCardPresenter provideLinkCardPresenter(User user, SharedPreferences sharedPreferences) {
        return new LinkCardPresenter(user, sharedPreferences);
    }

    @UserScope
    @Provides
    PreProfilePresenter providePreProfilePresenter(UserConfig userConfig) {
        return new PreProfilePresenter(userConfig);
    }

    @UserScope
    @Provides
    MainPresenter providerMainPresenter(FriendStore.Repository repository, OkHttpClient okHttpClient,
                                        @Named("OkHttpClientTimeoutLonger") OkHttpClient okHttpClientTimeoutLonger) {
        return new MainPresenter(repository, okHttpClient, okHttpClientTimeoutLonger);
    }

    @UserScope
    @Provides
    ZaloPayPresenter providerZaloPayPresenter() {
        return new ZaloPayPresenterImpl();
    }

    @UserScope
    @Provides
    ProfileInfoPresenter provideProfileInfoPresenter() {
        return new ProfileInfoPresenter();

    }

    @UserScope
    @Provides
    ProfilePresenter provideProfilePresenter() {
        return new ProfilePresenter();
    }

    @UserScope
    @Provides
    PinProfilePresenter providePinProfilePresenter(User user) {
        return new PinProfilePresenter(user);
    }

    @UserScope
    @Provides
    OTPProfilePresenter provideOTPProfilePresenter() {
        return new OTPProfilePresenter();
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
    TransferPresenter provideTransferPresenter(User user) {
        return new TransferPresenter(user);
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

    @UserScope
    @Provides
    WithdrawHomePresenter providesWithdrawHomePresenter(User user) {
        return new WithdrawHomePresenter(user);
    }

    @UserScope
    @Provides
    WithdrawPresenter providesWithdrawPresenter(User user) {
        return new WithdrawPresenter(user);
    }

    @UserScope
    @Provides
    EditAccountNamePresenter providesEditAccountNamePresenter() {
        return new EditAccountNamePresenter();
    }

    @UserScope
    @Provides
    TransferHomePresenter providesTransferHomePresenter() {
        return new TransferHomePresenter();
    }

    @UserScope
    @Provides
    WithdrawConditionPresenter providesWithdrawConditionPresenter() {
        return new WithdrawConditionPresenter();
    }

    @UserScope
    @Provides
    IChangePinPresenter providesChangePinPresenter() {
        return new ChangePinPresenter();
    }


}
