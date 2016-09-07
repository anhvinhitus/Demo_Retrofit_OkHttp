package vn.com.vng.zalopay.internal.di.modules;

import android.content.SharedPreferences;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.account.ui.presenter.ChangePinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.EditAccountNamePresenter;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.OTPProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.UpdateProfile3Presenter;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.transfer.ui.TransferHomePresenter;
import vn.com.vng.zalopay.transfer.ui.TransferMoneyPresenter;
import vn.com.vng.zalopay.transfer.ui.TransferPresenter;
import vn.com.vng.zalopay.ui.presenter.BalanceManagementPresenter;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.presenter.InvitationCodePresenter;
import vn.com.vng.zalopay.ui.presenter.LeftMenuPresenter;
import vn.com.vng.zalopay.ui.presenter.LinkCardPresenter;
import vn.com.vng.zalopay.ui.presenter.MainPresenter;
import vn.com.vng.zalopay.scanners.qrcode.QRCodePresenter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.ZaloPayPresenterImpl;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawConditionPresenter;
import vn.com.vng.zalopay.withdraw.ui.presenter.WithdrawPresenter;

@Module
public class UserPresenterModule {

    @UserScope
    @Provides
    IChangePinPresenter providesChangePinPresenter() {
        return new ChangePinPresenter();
    }

    @UserScope
    @Provides
    TransferMoneyPresenter provideTransferPresenter() {
        return new TransferPresenter();
    }

    @UserScope
    @Provides
    ZaloPayPresenter providerZaloPayPresenter() {
        return new ZaloPayPresenterImpl();
    }
}
