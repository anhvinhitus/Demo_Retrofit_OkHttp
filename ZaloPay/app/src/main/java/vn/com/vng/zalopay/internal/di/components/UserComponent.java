package vn.com.vng.zalopay.internal.di.components;

import com.zalopay.apploader.ReactNativeHostable;

import dagger.Subcomponent;
import vn.com.vng.zalopay.account.ui.activities.ChangePinActivity;
import vn.com.vng.zalopay.account.ui.activities.ProfileActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinContainerFragment;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinFragment;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinVerifyFragment;
import vn.com.vng.zalopay.account.ui.fragment.EditAccountNameFragment;
import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.UpdateProfile3Fragment;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.bank.ui.ListBankDialog;
import vn.com.vng.zalopay.banner.ui.fragment.BannerFragment;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.authentication.AuthenticationDialog;
import vn.com.vng.zalopay.feedback.FeedbackFragment;
import vn.com.vng.zalopay.bank.ui.BankSupportFragment;
import vn.com.vng.zalopay.bank.ui.LinkAccountFragment;
import vn.com.vng.zalopay.internal.di.modules.UserLogModule;
import vn.com.vng.zalopay.protect.ui.ProtectAccountFragment;
import vn.com.vng.zalopay.internal.di.modules.UserAccountModule;
import vn.com.vng.zalopay.internal.di.modules.UserApiModule;
import vn.com.vng.zalopay.internal.di.modules.UserBalanceModule;
import vn.com.vng.zalopay.internal.di.modules.UserControllerModule;
import vn.com.vng.zalopay.internal.di.modules.UserFriendModule;
import vn.com.vng.zalopay.internal.di.modules.UserMerchantModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.internal.di.modules.UserNotificationModule;
import vn.com.vng.zalopay.internal.di.modules.UserPresenterModule;
import vn.com.vng.zalopay.internal.di.modules.UserRedPacketModule;
import vn.com.vng.zalopay.internal.di.modules.UserTransactionModule;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.bank.ui.CardSupportActivity;
import vn.com.vng.zalopay.bank.ui.LinkCardFragment;
import vn.com.vng.zalopay.bank.ui.NotificationLinkCardFragment;
import vn.com.vng.zalopay.notification.NotificationHelper;
import vn.com.vng.zalopay.paymentapps.ui.PaymentApplicationActivity;
import vn.com.vng.zalopay.requestsupport.RequestSupportFragment;
import vn.com.vng.zalopay.scanners.beacons.CounterBeaconFragment;
import vn.com.vng.zalopay.scanners.nfc.ScanNFCFragment;
import vn.com.vng.zalopay.scanners.qrcode.QRCodeFragment;
import vn.com.vng.zalopay.service.UserSession;
import vn.com.vng.zalopay.transfer.ui.ReceiveMoneyFragment;
import vn.com.vng.zalopay.transfer.ui.TransferFragment;
import vn.com.vng.zalopay.transfer.ui.TransferHomeFragment;
import vn.com.vng.zalopay.transfer.ui.TransferViaZaloPayNameFragment;
import vn.com.vng.zalopay.transfer.ui.friendlist.ZaloFriendListFragment;
import vn.com.vng.zalopay.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.activity.QRCodeScannerActivity;
import vn.com.vng.zalopay.ui.fragment.BalanceManagementFragment;
import vn.com.vng.zalopay.ui.fragment.LeftMenuFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;
import vn.com.vng.zalopay.ui.presenter.HandleInAppPayment;
import vn.com.vng.zalopay.warningrooted.WarningRootedFragment;
import vn.com.vng.zalopay.webview.ui.WebViewFragment;
import vn.com.vng.zalopay.webview.ui.service.ServiceWebViewFragment;
import vn.com.vng.zalopay.withdraw.ui.fragment.CardSupportWithdrawFragment;
import vn.com.vng.zalopay.withdraw.ui.fragment.WithdrawConditionFragment;
import vn.com.vng.zalopay.withdraw.ui.fragment.WithdrawFragment;

@UserScope
@Subcomponent(
        modules = {
                UserModule.class,
                UserApiModule.class,
                UserControllerModule.class,
                UserMerchantModule.class,
                UserPresenterModule.class,
                UserBalanceModule.class,
                UserTransactionModule.class,
                UserNotificationModule.class,
                UserAccountModule.class,
                UserFriendModule.class,
                UserRedPacketModule.class,
                UserLogModule.class

        }
)
public interface UserComponent {

    User currentUser();

    UserSession userSession();

    AccountStore.Repository accountRepository();

    ZaloPayRepository zaloPayRepository();

    BalanceStore.Repository balanceRepository();

    TransactionStore.Repository transactionRepository();

    NotificationHelper notificationHelper();

    ReactNativeHostable reactNativeInstanceManager();

    /* inject Fragment */
    void inject(ZaloPayFragment f);

    void inject(LinkCardFragment link);

    void inject(BalanceTopupFragment f);

    void inject(LeftMenuFragment f);

    void inject(PinProfileFragment f);

    void inject(OtpProfileFragment f);

    void inject(ChangePinFragment f);

    void inject(ProfileFragment f);

    void inject(TransferHomeFragment f);

    void inject(TransferFragment f);

    /* inject activity */

    void inject(QRCodeScannerActivity activity);

    void inject(BalanceTopupActivity activity);

    void inject(MiniApplicationActivity activity);

    void inject(PaymentApplicationActivity activity);

    void inject(MainActivity act);

    void inject(ScanNFCFragment fragment);

    void inject(CounterBeaconFragment fragment);

    void inject(ProfileActivity a);

    void inject(UpdateProfileLevel2Activity a);

    void inject(ChangePinActivity a);

    void inject(QRCodeFragment f);

    void inject(UpdateProfile3Fragment f);

    void inject(BalanceManagementFragment f);

    void inject(WithdrawFragment f);

    void inject(WithdrawConditionFragment f);

    void inject(ChangePinContainerFragment f);

    void inject(ChangePinVerifyFragment f);

    void inject(ReceiveMoneyFragment f);

    void inject(TransferViaZaloPayNameFragment f);

    void inject(WebViewFragment f);

    void inject(ServiceWebViewFragment f);

    void inject(EditAccountNameFragment f);

    void inject(ZaloFriendListFragment f);

    void inject(NotificationLinkCardFragment f);

    void inject(WarningRootedFragment f);

    void inject(CardSupportActivity a);

    void inject(HandleInAppPayment obj);

    void inject(CardSupportWithdrawFragment f);

    void inject(BannerFragment f);

    void inject(ProtectAccountFragment f);

    void inject(AuthenticationDialog f);

    void inject(FeedbackFragment f);

    void inject(RequestSupportFragment f);

    void inject(BankSupportFragment f);

    void inject(LinkAccountFragment f);

    void inject(ListBankDialog f);
}
