package vn.com.vng.zalopay.internal.di.components;

import dagger.Subcomponent;
import vn.com.vng.zalopay.account.ui.activities.PreProfileActivity;
import vn.com.vng.zalopay.account.ui.activities.ProfileInfo2Activity;
import vn.com.vng.zalopay.account.ui.activities.ProfileInfoActivity;
import vn.com.vng.zalopay.account.ui.activities.RecoveryPinActivity;
import vn.com.vng.zalopay.account.ui.fragment.EditProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.OTPRecoveryPinFragment;
import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PreProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.RecoveryPinFragment;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AccountRepository;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
import vn.com.vng.zalopay.domain.repository.BalanceRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.modules.user.ApiUserModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserControllerModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserModule;
import vn.com.vng.zalopay.internal.di.modules.user.UserPresenterModule;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.mdl.IPaymentService;
import vn.com.vng.zalopay.paymentapps.ui.PaymentApplicationActivity;
import vn.com.vng.zalopay.transfer.provider.TransferRecentContentProviderImpl;
import vn.com.vng.zalopay.transfer.ui.activities.TransferHomeActivity;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferFragment;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferHomeFragment;
import vn.com.vng.zalopay.transfer.ui.fragment.ZaloContactFragment;
import vn.com.vng.zalopay.transfer.ui.presenter.TransferPresenter;
import vn.com.vng.zalopay.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.activity.QRCodeScannerActivity;
import vn.com.vng.zalopay.ui.fragment.LeftMenuFragment;
import vn.com.vng.zalopay.ui.fragment.LinkCardFragment;
import vn.com.vng.zalopay.ui.fragment.LinkCardProdureFragment;
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

    User currentUser();

    AppConfigRepository appConfigRepository();

    AccountRepository accountRepository();

    ZaloPayRepository zaloPayRepository();

    IPaymentService paymentService();

    BalanceRepository balanceRepository();
 /*   ApplicationRepository applicationRepository();*/

    /* inject Fragment */
    void inject(ZaloPayFragment f);

    void inject(LinkCardFragment link);

    void inject(BalanceTopupFragment f);

    void inject(LeftMenuFragment f);

    void inject(LinkCardProdureFragment f);

    void inject(ProfileFragment f);

    void inject(PreProfileFragment f);

    void inject(PinProfileFragment f);

    void inject(OtpProfileFragment f);

    void inject(RecoveryPinFragment f);

    void inject(OTPRecoveryPinFragment f);

    void inject(EditProfileFragment f);

    void inject(TransferHomeFragment f);

    void inject(ZaloContactFragment f);

    void inject(TransferFragment f);

    /* inject activity */

    void inject(QRCodeScannerActivity activity);

    void inject(BalanceTopupActivity activity);

    void inject(MiniApplicationActivity activity);

    void inject(PaymentApplicationActivity activity);

    void inject(MainActivity act);

    void inject(ProfileInfoActivity a);

    void inject(ProfileInfo2Activity a);

    void inject(PreProfileActivity a);

    void inject(RecoveryPinActivity a);

    void inject(TransferRecentContentProviderImpl contentProvider);

    void inject(TransferHomeActivity activity);
}
