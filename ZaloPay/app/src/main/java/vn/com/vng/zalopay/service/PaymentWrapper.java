package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.NewSessionEvent;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.EPayError;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;
import vn.com.zalopay.wallet.controller.WalletSDKPayment;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.listener.ZPWSaveMapCardListener;

/**
 * Created by huuhoa on 6/3/16.
 * Wrapper for handle common processing involves with wallet SDK
 */
public class PaymentWrapper {
    public interface IGetOrderCallback {
        void onResponseSuccess(Order order);

        void onResponseError(int status);
    }

    private final IViewListener viewListener;
    private final IResponseListener responseListener;
    private final ZaloPayRepository zaloPayRepository;
    private final BalanceStore.Repository balanceRepository;
    private final TransactionStore.Repository transactionRepository;
    private final Navigator mNavigator = AndroidApplication.instance().getAppComponent().navigator();
    private final boolean mShowNotificationLinkCard;
    public ZPWPaymentInfo mPaymentInfoNotEnoughMoney;
    public EPaymentChannel mPaymentChannelEnoughMoney;

    private ZPPaymentListener zpPaymentListener = new ZPPaymentListener() {
        @Override
        public void onComplete(ZPPaymentResult pPaymentResult) {
            Timber.d("pay onComplete pPaymentResult [%s]", pPaymentResult);
            if (pPaymentResult == null) {
                if (NetworkHelper.isNetworkAvailable(viewListener.getActivity())) {
                    responseListener.onResponseError(PaymentError.ERR_CODE_SYSTEM);
                } else {
                    responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
                }
                mPaymentInfoNotEnoughMoney = null;
                mPaymentChannelEnoughMoney = null;
            } else {
                EPaymentStatus resultStatus = pPaymentResult.paymentStatus;
                Timber.d("pay onComplete resultStatus [%s]", pPaymentResult.paymentStatus);
                switch (resultStatus) {
                    case ZPC_TRANXSTATUS_SUCCESS:
                        if (mShowNotificationLinkCard) {
                            mNavigator.startNotificationLinkCardActivity(viewListener.getActivity(),
                                    pPaymentResult.mapCardResult);
                        }
                        responseListener.onResponseSuccess(pPaymentResult);
                        break;
                    case ZPC_TRANXSTATUS_TOKEN_INVALID:
                        responseListener.onResponseTokenInvalid();
                        break;
                    case ZPC_TRANXSTATUS_UPGRADE:
                        //Hien update profile level 2
                        startUpdateProfileLevel(null);
                        responseListener.onResponseError(PaymentError.ERR_CODE_UPGRADE_PROFILE_LEVEL);
                        break;
                    case ZPC_TRANXSTATUS_UPGRADE_SAVECARD:
                        String walletTransId = null;
                        if (pPaymentResult.paymentInfo != null) {
                            walletTransId = pPaymentResult.paymentInfo.walletTransID;
                        }
                        //Hien update profile level 2
                        startUpdateProfileLevel(walletTransId);
                        responseListener.onResponseError(PaymentError.ERR_CODE_UPGRADE_PROFILE_LEVEL);
                        break;
                    case ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH:
                        responseListener.onResponseError(PaymentError.ERR_CODE_MONEY_NOT_ENOUGH);
                        responseListener.onNotEnoughMoney();
                        break;
                    case ZPC_TRANXSTATUS_CLOSE:
                        responseListener.onResponseError(PaymentError.ERR_CODE_USER_CANCEL);
                        break;
                    case ZPC_TRANXSTATUS_INPUT_INVALID:
                        responseListener.onResponseError(PaymentError.ERR_CODE_INPUT);
                        break;
                    case ZPC_TRANXSTATUS_FAIL:
                        responseListener.onResponseError(PaymentError.ERR_CODE_FAIL);
                        break;
                    case ZPC_TRANXSTATUS_PROCESSING:
                        responseListener.onResponseError(PaymentError.ERR_CODE_PROCESSING);
                        break;
                    case ZPC_TRANXSTATUS_SERVICE_MAINTENANCE:
                        responseListener.onResponseError(PaymentError.ERR_CODE_SERVICE_MAINTENANCE);
                        break;
                    case ZPC_TRANXSTATUS_NO_INTERNET:
                        responseListener.onResponseError(PaymentError.ERR_TRANXSTATUS_NO_INTERNET);
                        break;
                    case ZPC_TRANXSTATUS_NEED_LINKCARD:
                        responseListener.onResponseError(PaymentError.ERR_TRANXSTATUS_NEED_LINKCARD);
                        break;
                    default:
                        responseListener.onResponseError(PaymentError.ERR_CODE_UNKNOWN);
                        break;
                }
                if (resultStatus != EPaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH) {
                    mPaymentInfoNotEnoughMoney = null;
                    mPaymentChannelEnoughMoney = null;
                }
            }
        }

        @Override
        public void onError(CError cError) {
            Timber.d("pay onError code [%s] msg [%s]", cError.payError, cError.messError);
            switch (cError.payError) {
                case DATA_INVALID:
                    responseListener.onParameterError(cError.messError);
                    break;
                case COMPONENT_NULL:
                    responseListener.onAppError(cError.messError);
                    break;
                case NETWORKING_ERROR:
                    responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
                    break;
                default:
                    responseListener.onAppError(cError.messError);
                    break;
            }
        }

        @Override
        public void onUpVersion(boolean forceUpdate, String latestVersion, String msg) {
            Timber.d("onUpVersion forceUpdate[%s] latestVersion [%s] msg [%s]",
                    forceUpdate, latestVersion, msg);
            AppVersionUtils.setVersionInfoInServer(forceUpdate, latestVersion, msg);
            AppVersionUtils.showDialogUpgradeAppIfNeed(viewListener.getActivity());
        }

        @Override
        public void onUpdateAccessToken(String token) {
            if (!TextUtils.isEmpty(token)) {
                return;
            }
            EventBus.getDefault().post(new NewSessionEvent(token));
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {

            responseListener.onPreComplete(isSuccessful, pTransId, pAppTransId);

            if (isSuccessful) {
                updateBalance();
                updateTransactionSuccess();
            } else {
                updateTransactionFail();
            }
        }
    };

    public PaymentWrapper(BalanceStore.Repository balanceRepository, ZaloPayRepository zaloPayRepository,
                          TransactionStore.Repository transactionRepository, IViewListener viewListener,
                          IResponseListener responseListener) {
        this.balanceRepository = balanceRepository;
        this.zaloPayRepository = zaloPayRepository;
        this.viewListener = viewListener;
        this.responseListener = responseListener;
        this.transactionRepository = transactionRepository;
        this.mShowNotificationLinkCard = true;
    }

    public PaymentWrapper(BalanceStore.Repository balanceRepository, ZaloPayRepository zaloPayRepository,
                          TransactionStore.Repository transactionRepository, IViewListener viewListener,
                          IResponseListener responseListener, boolean showNotificationLinkCard) {
        this.balanceRepository = balanceRepository;
        this.zaloPayRepository = zaloPayRepository;
        this.viewListener = viewListener;
        this.responseListener = responseListener;
        this.transactionRepository = transactionRepository;
        this.mShowNotificationLinkCard = showNotificationLinkCard;
    }

    public void payWithToken(long appId, String transactionToken) {
        Subscription subscription = zaloPayRepository.getOrder(appId, transactionToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetOrderSubscriber());
    }

    private UserInfo getUserInfo(String displayName, String avatar, String phoneNumber, String zaloPayName) {
        UserInfo mUserInfo = getUserInfo();
        mUserInfo.phoneNumber = phoneNumber;
        mUserInfo.userName = displayName;
        mUserInfo.zaloPayName = zaloPayName;
        return mUserInfo;
    }

    private UserInfo getUserInfo() {

        User user = null;
        if (getUserComponent() != null) {
            user = getUserComponent().currentUser();
        }

        UserInfo mUserInfo = new UserInfo();
        if (user != null) {
            mUserInfo.zaloUserId = String.valueOf(user.zaloId);
            mUserInfo.zaloPayUserId = user.zaloPayId;
            mUserInfo.accessToken = user.accesstoken;
            mUserInfo.level = getUserProfileLevel();
            mUserInfo.userProfile = getUserPermission();
        }
        return mUserInfo;
    }

    public void withdraw(Order order, String displayName, String avatar, String phoneNumber, String zaloPayName) {
        EPaymentChannel forcedPaymentChannel = EPaymentChannel.WITHDRAW;
        ZPWPaymentInfo paymentInfo = transform(order);
        paymentInfo.userInfo = getUserInfo(displayName, avatar, phoneNumber, zaloPayName);
        callPayAPI(paymentInfo, forcedPaymentChannel);
    }

    public void transfer(Order order, String displayName, String avatar, String phoneNumber, String zaloPayName) {

        EPaymentChannel forcedPaymentChannel = EPaymentChannel.WALLET_TRANSFER;
        ZPWPaymentInfo paymentInfo = transform(order);
        paymentInfo.userInfo = getUserInfo(displayName, avatar, phoneNumber, zaloPayName);
        callPayAPI(paymentInfo, forcedPaymentChannel);
    }

    public void payWithOrder(Order order) {
        Timber.d("payWithOrder: Start");
        if (order == null) {
            Timber.i("payWithOrder: order is invalid");
            responseListener.onParameterError("order");
//            showErrorView(mView.getContext().getString(R.string.order_invalid));
            return;
        }
        Timber.d("payWithOrder: Order is valid");

        User user = AndroidApplication.instance().getUserComponent().currentUser();

        if (TextUtils.isEmpty(user.zaloPayId)) {
            Timber.i("payWithOrder: zaloPayId is invalid");
            responseListener.onParameterError("uid");
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = transform(order);

            Timber.d("payWithOrder: ZPWPaymentInfo is ready");

//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
            callPayAPI(paymentInfo);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception with number format");
            responseListener.onParameterError("exception");
        }
    }

    public void getOrder(long appId, String transactionToken, final IGetOrderCallback callback) {
        zaloPayRepository.getOrder(appId, transactionToken)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Order>() {
                    @Override
                    public void onError(Throwable e) {
                        callback.onResponseError(-1);
                    }

                    @Override
                    public void onNext(Order order) {
                        callback.onResponseSuccess(order);
                    }
                });
    }

    public void linkCard() {
        Timber.d("linkCard Start");
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (TextUtils.isEmpty(user.zaloPayId)) {
            Timber.i("payWithOrder: zaloPayId is invalid");
            responseListener.onParameterError("uid");
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
            paymentInfo.appID = BuildConfig.ZALOPAY_APP_ID;
            paymentInfo.appTime = System.currentTimeMillis();
            paymentInfo.userInfo = getUserInfo();

            Timber.d("payWithOrder: ZPWPaymentInfo is ready");

//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
            callPayAPI(paymentInfo, EPaymentChannel.LINK_CARD);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception with number format");
            responseListener.onParameterError("exception");
        }
    }

    public void saveCardMap(String walletTransId, ZPWSaveMapCardListener listener) {
        Timber.d("saveCardMap, viewListener: %s", viewListener);
        if (viewListener == null) {
            return;
        }

        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.userInfo = getUserInfo();
        paymentInfo.walletTransID = walletTransId;

        Timber.d("saveCardMap, start paymentsdk");
        WalletSDKApplication.saveCardMap(paymentInfo, listener);
    }

    private void callPayAPI(ZPWPaymentInfo paymentInfo, EPaymentChannel paymentChannel) {
        if (paymentInfo == null) {
            return;
        }
        if (paymentInfo.userInfo == null
                || paymentInfo.userInfo.level < 0
                || TextUtils.isEmpty(paymentInfo.userInfo.userProfile)) {
            paymentInfo.userInfo = getUserInfo();
        }
        if (paymentInfo.userInfo.level < 0 || TextUtils.isEmpty(paymentInfo.userInfo.userProfile)) {
            zpPaymentListener.onError(new CError(EPayError.DATA_INVALID, "Vui lòng cập nhật thông tin tài khoản."));
            return;
        }
        if (balanceRepository != null) {
            paymentInfo.userInfo.balance = balanceRepository.currentBalance();
        }

        Timber.d("Call Pay to sdk activity [%s] paymentChannel [%s] paymentInfo [%s]",
                viewListener.getActivity(), paymentChannel, paymentInfo);
        mPaymentInfoNotEnoughMoney = paymentInfo;
        mPaymentChannelEnoughMoney = paymentChannel;
        WalletSDKPayment.pay(viewListener.getActivity(), paymentChannel, paymentInfo, zpPaymentListener);
    }

    private int getUserProfileLevel() {
        UserComponent userComponent = getUserComponent();
        if (userComponent != null) {
            return userComponent.currentUser().profilelevel;
        }
        return -1;
    }

    private UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    private String getUserPermission() {
        if (getUserComponent() == null) {
            return null;
        }

        User user = getUserComponent().currentUser();
        if (user == null) {
            return null;
        }

        String permissionsStr = "{\"profilelevelpermisssion\":";
        permissionsStr += user.profilePermissions;
        permissionsStr += "}";
        Timber.d("permissionsStr: %s", permissionsStr);
        return permissionsStr;
    }

    private void callPayAPI(ZPWPaymentInfo paymentInfo) {
        callPayAPI(paymentInfo, null);
    }

    @NonNull
    private ZPWPaymentInfo transform(Order order) {
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

        paymentInfo.appID = order.appid;
        paymentInfo.userInfo = getUserInfo();
        paymentInfo.appTime = order.apptime;
        paymentInfo.appTransID = order.apptransid;
        paymentInfo.itemName = order.item;
        paymentInfo.amount = order.amount;
        paymentInfo.description = order.description;
        paymentInfo.embedData = order.embeddata;
        //lap vao ví appId = appUser = 1
        paymentInfo.appUser = order.appuser;
        paymentInfo.mac = order.mac;
        return paymentInfo;
    }

    private void startUpdateProfileLevel(String walletTransID) {
        if (viewListener == null || viewListener.getActivity() == null) {
            return;
        }

        Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();
        navigator.startUpdateProfileLevel2Activity(viewListener.getActivity(), walletTransID);
    }

    public interface IViewListener {
        Activity getActivity();
    }

    public interface IResponseListener {
        void onParameterError(String param);

        void onResponseError(PaymentError status);

        void onResponseSuccess(ZPPaymentResult zpPaymentResult);

        void onResponseTokenInvalid();

        void onAppError(String msg);

        void onNotEnoughMoney();

        void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId);
    }

    private final class GetOrderSubscriber extends DefaultSubscriber<Order> {

        @Override
        public void onNext(Order order) {
            Timber.d("getOrder response: %s", order.item);
            payWithOrder(order);
        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, "onError " + e);
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            if (e instanceof NetworkConnectionException) {
                responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
            } else {
                responseListener.onParameterError("token");
            }
        }
    }


    private void updateTransactionSuccess() {
        Subscription subscription = transactionRepository.fetchTransactionHistorySuccessLatest()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    private void updateTransactionFail() {
        Subscription subscription = transactionRepository.fetchTransactionHistoryFailLatest()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    private void updateBalance() {
        // update balance
        Subscription subscription = balanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }

    public boolean hasOrderNotPayBecauseNotEnoughMoney() {
        return (mPaymentInfoNotEnoughMoney != null);
    }

    public void continuePayAfterDeposit() {
        if (mPaymentInfoNotEnoughMoney == null) {
            return;
        }
        callPayAPI(mPaymentInfoNotEnoughMoney, mPaymentChannelEnoughMoney);
    }
}
