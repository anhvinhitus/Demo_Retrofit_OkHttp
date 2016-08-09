package vn.com.vng.zalopay.internal.di.components;

import dagger.Subcomponent;
import vn.com.vng.zalopay.account.ui.activities.ChangePinActivity;
import vn.com.vng.zalopay.account.ui.activities.ProfileInfo2Activity;
import vn.com.vng.zalopay.account.ui.activities.ProfileInfoActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinFragment;
import vn.com.vng.zalopay.account.ui.fragment.EditProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.OTPRecoveryPinFragment;
import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PreProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.UpdateProfile3Fragment;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.data.appresources.AppResource;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.modules.UserAccountModule;
import vn.com.vng.zalopay.internal.di.modules.UserFriendModule;
import vn.com.vng.zalopay.internal.di.modules.UserNotificationModule;
import vn.com.vng.zalopay.internal.di.modules.UserRedPacketModule;
import vn.com.vng.zalopay.internal.di.modules.UserApiModule;
import vn.com.vng.zalopay.internal.di.modules.AppResourceModule;
import vn.com.vng.zalopay.internal.di.modules.UserBalanceModule;
import vn.com.vng.zalopay.internal.di.modules.UserTransactionModule;
import vn.com.vng.zalopay.internal.di.modules.UserControllerModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.internal.di.modules.UserPresenterModule;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.mdl.ReactNativeHostable;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.vng.zalopay.notification.NotificationHelper;
import vn.com.vng.zalopay.notification.ZPNotificationService;
import vn.com.vng.zalopay.paymentapps.ui.PaymentApplicationActivity;
import vn.com.vng.zalopay.scanners.beacons.CounterBeaconFragment;
import vn.com.vng.zalopay.scanners.nfc.ScanNFCFragment;
import vn.com.vng.zalopay.scanners.qrcode.QRCodeFragment;
import vn.com.vng.zalopay.scanners.sound.ScanSoundFragment;
import vn.com.vng.zalopay.transfer.ui.activities.TransferHomeActivity;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferFragment;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferHomeFragment;
import vn.com.vng.zalopay.transfer.ui.fragment.ZaloContactFragment;
import vn.com.vng.zalopay.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.activity.QRCodeScannerActivity;
import vn.com.vng.zalopay.ui.fragment.IntroFragment;
import vn.com.vng.zalopay.ui.fragment.LeftMenuFragment;
import vn.com.vng.zalopay.ui.fragment.LinkCardFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;

@UserScope
@Subcomponent(
        modules = {
                UserModule.class,
                UserApiModule.class,
                UserControllerModule.class,
                UserPresenterModule.class,
                UserBalanceModule.class,
                UserTransactionModule.class,
                AppResourceModule.class,
                UserNotificationModule.class,
                UserAccountModule.class,
                UserFriendModule.class,
                UserRedPacketModule.class
        }
)
public interface UserComponent {

    User currentUser();

    AppResource.Repository appResourceRepository();

    AccountStore.Repository accountRepository();

    ZaloPayRepository zaloPayRepository();

    RedPacketStore.Repository redPackageStoreRepository();

    IPaymentService paymentService();

    BalanceStore.Repository balanceRepository();

    TransactionStore.Repository transactionRepository();

    NotificationStore.Repository notificationRepository();

    NotificationHelper notificationHelper();

    FriendStore.Repository friendRepository();

    TransferStore.LocalStorage transferLocalStorage();

    ReactNativeHostable reactNativeInstanceManager();
 /*   ApplicationRepository applicationRepository();*/

    /* inject Fragment */
    void inject(ZaloPayFragment f);

    void inject(LinkCardFragment link);

    void inject(BalanceTopupFragment f);

    void inject(LeftMenuFragment f);

    void inject(ProfileFragment f);

    void inject(PreProfileFragment f);

    void inject(PinProfileFragment f);

    void inject(OtpProfileFragment f);

    void inject(ChangePinFragment f);

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

    void inject(ScanNFCFragment fragment);

    void inject(CounterBeaconFragment fragment);

    void inject(ProfileInfo2Activity a);

    void inject(UpdateProfileLevel2Activity a);

    void inject(ChangePinActivity a);

    void inject(ScanSoundFragment fragment);

    void inject(QRCodeFragment f);

    void inject(TransferHomeActivity activity);

    void inject(ZPNotificationService service);

    void inject(UpdateProfile3Fragment f);

    void inject(IntroFragment f);
}
