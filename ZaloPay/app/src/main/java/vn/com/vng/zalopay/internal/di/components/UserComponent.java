
package vn.com.vng.zalopay.internal.di.components;

import com.zalopay.apploader.ReactNativeHostable;

import javax.inject.Named;

import dagger.Subcomponent;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.account.ui.activities.ProfileActivity;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinContainerFragment;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinFragment;
import vn.com.vng.zalopay.account.ui.fragment.ChangePinVerifyFragment;
import vn.com.vng.zalopay.account.ui.fragment.EditAccountNameFragment;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.UpdateProfile3Fragment;
import vn.com.vng.zalopay.authentication.AuthenticationDialog;
import vn.com.vng.zalopay.authentication.AuthenticationPassword;
import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.bank.list.BankListFragment;
import vn.com.vng.zalopay.bank.ui.BankSupportSelectionFragment;
import vn.com.vng.zalopay.bank.ui.NotificationLinkCardFragment;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.paymentconnector.PaymentConnectorService;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.feedback.FeedbackFragment;
import vn.com.vng.zalopay.internal.di.modules.QRCodeModule;
import vn.com.vng.zalopay.internal.di.modules.UserAccountModule;
import vn.com.vng.zalopay.internal.di.modules.UserApiModule;
import vn.com.vng.zalopay.internal.di.modules.UserBalanceModule;
import vn.com.vng.zalopay.internal.di.modules.UserControllerModule;
import vn.com.vng.zalopay.internal.di.modules.UserFileLogModule;
import vn.com.vng.zalopay.internal.di.modules.UserFriendModule;
import vn.com.vng.zalopay.internal.di.modules.UserMerchantModule;
import vn.com.vng.zalopay.internal.di.modules.UserModule;
import vn.com.vng.zalopay.internal.di.modules.UserNotificationModule;
import vn.com.vng.zalopay.internal.di.modules.UserPresenterModule;
import vn.com.vng.zalopay.internal.di.modules.UserRedPacketModule;
import vn.com.vng.zalopay.internal.di.modules.UserSocketModule;
import vn.com.vng.zalopay.internal.di.modules.UserTransactionModule;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.notification.NotificationHelper;
import vn.com.vng.zalopay.paymentapps.ui.PaymentApplicationActivity;
import vn.com.vng.zalopay.protect.ui.ProtectAccountFragment;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.react.base.ExternalReactFragment;
import vn.com.vng.zalopay.react.base.InternalReactFragment;
import vn.com.vng.zalopay.scanners.beacons.CounterBeaconFragment;
import vn.com.vng.zalopay.scanners.nfc.ScanNFCFragment;
import vn.com.vng.zalopay.scanners.qrcode.QRCodeFragment;
import vn.com.vng.zalopay.searchcategory.SearchCategoryFragment;
import vn.com.vng.zalopay.service.UserSession;
import vn.com.vng.zalopay.share.HandleZaloIntegration;
import vn.com.vng.zalopay.transfer.ui.ReceiveMoneyFragment;
import vn.com.vng.zalopay.transfer.ui.TransferFragment;
import vn.com.vng.zalopay.transfer.ui.TransferHomeFragment;
import vn.com.vng.zalopay.transfer.ui.TransferViaZaloPayNameFragment;
import vn.com.vng.zalopay.transfer.ui.friendlist.SyncContactFragment;
import vn.com.vng.zalopay.transfer.ui.friendlist.ZaloPayContactListFragment;
import vn.com.vng.zalopay.ui.activity.HomeActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.fragment.BalanceManagementFragment;
import vn.com.vng.zalopay.ui.fragment.HomeCollapseHeaderFragment;
import vn.com.vng.zalopay.ui.fragment.HomeListAppFragment;
import vn.com.vng.zalopay.ui.fragment.HomeTopHeaderFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.PersonalFragment;
import vn.com.vng.zalopay.ui.presenter.HandleInAppPayment;
import vn.com.vng.zalopay.warningrooted.WarningRootedFragment;
import vn.com.vng.zalopay.webapp.WebAppFragment;
import vn.com.vng.zalopay.webapp.WebAppPromotionFragment;
import vn.com.vng.zalopay.webapp.WebBottomSheetDialogFragment;
import vn.com.vng.zalopay.webview.ui.WebViewFragment;
import vn.com.vng.zalopay.webview.ui.WebViewFromQRScanFragment;
import vn.com.vng.zalopay.webview.ui.WebViewPromotionFragment;
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
                QRCodeModule.class,
                UserSocketModule.class,
                UserFileLogModule.class

        }
)
public interface UserComponent {

    User currentUser();

    UserSession userSession();

    AccountStore.Repository accountRepository();

    BalanceStore.Repository balanceRepository();

    TransactionStore.Repository transactionRepository();

    NotificationHelper notificationHelper();

    ReactNativeHostable reactNativeInstanceManager();

    @Named("retrofitConnector")
    Retrofit retrofitConnector();

    PaymentConnectorService connectorService();

    /* inject Fragment */

    void inject(BalanceTopupFragment f);

    void inject(ChangePinFragment f);

    void inject(ProfileFragment f);

    void inject(TransferHomeFragment f);

    void inject(TransferFragment f);

    void inject(PersonalFragment f);

    void inject(WebAppPromotionFragment f);

    void inject(WebViewPromotionFragment f);

    void inject(ScanNFCFragment fragment);

    void inject(CounterBeaconFragment fragment);

    void inject(QRCodeFragment f);

    void inject(UpdateProfile3Fragment f);

    void inject(BalanceManagementFragment f);

    void inject(WithdrawFragment f);

    void inject(WithdrawConditionFragment f);

    void inject(ChangePinContainerFragment f);

    void inject(AuthenticationPassword f);

    void inject(ChangePinVerifyFragment f);

    void inject(ReceiveMoneyFragment f);

    void inject(TransferViaZaloPayNameFragment f);

    void inject(WebViewFragment f);

    void inject(ServiceWebViewFragment f);

    void inject(EditAccountNameFragment f);

    void inject(ZaloPayContactListFragment f);

    void inject(NotificationLinkCardFragment f);

    void inject(WarningRootedFragment f);

    void inject(HandleInAppPayment obj);

    void inject(HandleZaloIntegration obj);

    void inject(CardSupportWithdrawFragment f);

    void inject(ProtectAccountFragment f);

    void inject(AuthenticationDialog f);

    void inject(FeedbackFragment f);

    void inject(WebAppFragment f);

    void inject(WebBottomSheetDialogFragment f);

    void inject(InternalReactFragment d);

    void inject(ExternalReactFragment f);

    void inject(SearchCategoryFragment f);

    void inject(HomeListAppFragment f);

    void inject(HomeCollapseHeaderFragment f);

    void inject(HomeTopHeaderFragment f);

    void inject(PaymentWrapper paymentWrapper);

    void inject(BankSupportSelectionFragment f);

    void inject(WebViewFromQRScanFragment f);

    void inject(BankListFragment f);

    void inject(SyncContactFragment f);

      /* inject activity */

    void inject(MiniApplicationActivity activity);

    void inject(PaymentApplicationActivity activity);

    void inject(ProfileActivity activity);

    void inject(HomeActivity activity);

}
