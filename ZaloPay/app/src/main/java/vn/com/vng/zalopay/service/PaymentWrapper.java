package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.authentication.PaymentFingerPrint;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
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

    final IRedirectListener mRedirectListener;
    final IResponseListener responseListener;
    private final ZaloPayRepository zaloPayRepository;
    private final BalanceStore.Repository balanceRepository;
    final Navigator mNavigator = AndroidApplication.instance().getAppComponent().navigator();
    boolean mShowNotificationLinkCard;
    private ZPWPaymentInfo mPendingOrder;
    private EPaymentChannel mPendingChannel;

    private final ZPPaymentListener mWalletListener;

    Activity mActivity;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    PaymentWrapper(BalanceStore.Repository balanceRepository, ZaloPayRepository zaloPayRepository,
                   TransactionStore.Repository transactionRepository,
                   IResponseListener responseListener, IRedirectListener redirectListener,
                   boolean showNotificationLinkCard) {
        Timber.d("Create new instance of PaymentWrapper");
        this.balanceRepository = balanceRepository;
        this.zaloPayRepository = zaloPayRepository;
        this.responseListener = responseListener;
        this.mRedirectListener = redirectListener;
        this.mShowNotificationLinkCard = showNotificationLinkCard;
        mWalletListener = new WalletListener(this, transactionRepository, balanceRepository);
    }

    public void payWithToken(Activity activity, long appId, String transactionToken) {
        Timber.d("start payWithToken [%s-%s]", appId, transactionToken);
        mActivity = activity;
        Subscription subscription = zaloPayRepository.getOrder(appId, transactionToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetOrderSubscriber());
        mCompositeSubscription.add(subscription);
    }

    public void withdraw(Activity activity, Order order, String displayName, String avatar, String phoneNumber, String zaloPayName) {
        EPaymentChannel forcedPaymentChannel = EPaymentChannel.WITHDRAW;
        ZPWPaymentInfo paymentInfo = transform(order);
        paymentInfo.userInfo = createUserInfo(displayName, avatar, phoneNumber, zaloPayName);
        callPayAPI(activity, paymentInfo, forcedPaymentChannel);
    }

    public void transfer(Activity activity, Order order, String displayName, String avatar, String phoneNumber, String zaloPayName) {
        EPaymentChannel forcedPaymentChannel = EPaymentChannel.WALLET_TRANSFER;
        ZPWPaymentInfo paymentInfo = transform(order);
        paymentInfo.userInfo = createUserInfo(displayName, avatar, phoneNumber, zaloPayName);
        callPayAPI(activity, paymentInfo, forcedPaymentChannel);
    }

    public void payWithOrder(Activity activity, Order order) {
        Timber.d("payWithOrder: Start");
        if (order == null) {
            Timber.i("payWithOrder: order is invalid");
            responseListener.onParameterError("order");
//            showErrorView(mView.getContext().getString(R.string.order_invalid));
            return;
        }
        Timber.d("payWithOrder: Order is valid");

        User user = AndroidApplication.instance().getUserComponent().currentUser();

        if (!user.hasZaloPayId()) {
            Timber.i("payWithOrder: zaloPayId is invalid");
            responseListener.onParameterError("uid");
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = transform(order);

            Timber.d("payWithOrder: ZPWPaymentInfo is ready");

//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
            callPayAPI(activity, paymentInfo, null);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception with number format");
            responseListener.onParameterError("exception");
        }
    }

    public void getOrder(long appId, String transactionToken, final IGetOrderCallback callback) {
        Subscription subscription = zaloPayRepository.getOrder(appId, transactionToken)
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
        mCompositeSubscription.add(subscription);
    }

    public void linkCard(Activity activity) {
        Timber.d("linkCard Start");
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (!user.hasZaloPayId()) {
            Timber.i("payWithOrder: zaloPayId is invalid");
            responseListener.onParameterError("uid");
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
            paymentInfo.appID = BuildConfig.ZALOPAY_APP_ID;
            paymentInfo.appTime = System.currentTimeMillis();

            Timber.d("payWithOrder: ZPWPaymentInfo is ready");

//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
            callPayAPI(activity, paymentInfo, EPaymentChannel.LINK_CARD);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception with number format");
            responseListener.onParameterError("exception");
        }
    }

    public void saveCardMap(String walletTransId, ZPWSaveMapCardListener listener) {
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.userInfo = assignBaseUserInfo(paymentInfo.userInfo);
        paymentInfo.walletTransID = walletTransId;

        Timber.d("saveCardMap, start paymentsdk");
        WalletSDKApplication.saveCardMap(paymentInfo, listener);
    }

    private boolean hasPendingOrder() {
        return (mPendingOrder != null);
    }

    private void continuePayPendingOrder() {
        if (!hasPendingOrder()) {
            return;
        }

        //Require reset forceChannelIds & mappedCreditCard before continue payment
        if (mPendingOrder != null) {
            mPendingOrder.forceChannelIds = null;
            mPendingOrder.mappedCreditCard = null;
        }
        callPayAPI(mActivity, mPendingOrder, mPendingChannel);
    }

    /**
     * Handle fragment/activity result
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean shouldProcessPendingOrder = false;
        if (resultCode != Activity.RESULT_OK && resultCode != Activity.RESULT_CANCELED) {
            return;
        }

        if (requestCode == Constants.REQUEST_CODE_DEPOSIT ||
                requestCode == Constants.REQUEST_CODE_UPDATE_PROFILE_LEVEL_2) {
            shouldProcessPendingOrder = true;
        }

        if (shouldProcessPendingOrder && hasPendingOrder()) {
            continuePayPendingOrder();
        }
    }

    void cleanup() {
        Timber.i("PaymentWrapper is cleaning up");
        mActivity = null;
        clearPendingOrder();
        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Timber.d("PaymentWrapper is finalize");
    }

    private UserInfo createUserInfo(String displayName, String avatar, String phoneNumber, String zaloPayName) {
        UserInfo mUserInfo = new UserInfo();
        mUserInfo.phoneNumber = phoneNumber;
        mUserInfo.userName = displayName;
        mUserInfo.zaloPayName = zaloPayName;
        return mUserInfo;
    }

    private UserInfo assignBaseUserInfo(UserInfo userInfo) {
        User user = null;
        if (getUserComponent() != null) {
            user = getUserComponent().currentUser();
        }

        if (userInfo == null) {
            userInfo = new UserInfo();
        }
        if (user != null) {
            userInfo.zaloUserId = String.valueOf(user.zaloId);
            userInfo.zaloPayUserId = user.zaloPayId;
            userInfo.accessToken = user.accesstoken;
            userInfo.level = getUserProfileLevel();
            userInfo.userProfile = getUserPermission();
        }
        return userInfo;
    }

    private void callPayAPI(Activity owner, ZPWPaymentInfo paymentInfo, EPaymentChannel paymentChannel) {
        mActivity = owner;
        if (paymentInfo == null || owner == null) {
            mActivity = null;
            return;
        }

        paymentInfo.userInfo = assignBaseUserInfo(paymentInfo.userInfo);
        if (paymentInfo.userInfo.level < 0 || TextUtils.isEmpty(paymentInfo.userInfo.userProfile)) {
            mWalletListener.onError(new CError(EPayError.DATA_INVALID, owner.getString(R.string.please_update_profile)));
            mActivity = null;
            return;
        }
        if (balanceRepository != null) {
            paymentInfo.userInfo.balance = balanceRepository.currentBalance();
        }

        if (paymentChannel != EPaymentChannel.LINK_CARD && !validPaymentInfo(paymentInfo)) {
            responseListener.onAppError(owner.getString(R.string.data_invalid_try_again));
            Timber.e(new Exception(
                    String.format("PaymentInfo is invalid, appId[%s] transId[%s] amount[%s] appTime[%s]  mac[%s]",
                            paymentInfo.appID,
                            paymentInfo.appTransID,
                            paymentInfo.amount,
                            paymentInfo.appTime,
                            paymentInfo.mac)));
            return;
        }

        Timber.d("Call Pay to sdk activity [%s] paymentChannel [%s] paymentInfo [%s]",
                owner, paymentChannel, paymentInfo);
        mPendingOrder = paymentInfo;
        mPendingChannel = paymentChannel;
        WalletSDKPayment.pay(owner, paymentChannel, paymentInfo, mWalletListener, new PaymentFingerPrint(AndroidApplication.instance()));
    }

    private boolean validPaymentInfo(ZPWPaymentInfo paymentInfo) {
        if (paymentInfo.amount <= 0) {
            return false;
        } else if (paymentInfo.appID <= 0) {
            return false;
        } else if (TextUtils.isEmpty(paymentInfo.appTransID)) {
            return false;
        } else if (paymentInfo.appTime <= 0) {
            return false;
        } else if (TextUtils.isEmpty(paymentInfo.mac)) {
            return false;
        }
        return true;
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

    @NonNull
    private ZPWPaymentInfo transform(Order order) {
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

        paymentInfo.appID = order.appid;
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

    void startUpdateProfileLevel(String walletTransID) {
        if (mActivity == null) {
            return;
        }

        mNavigator.startUpdateProfileLevel2Activity(mActivity, walletTransID);
    }

    public interface IRedirectListener {
        void startUpdateProfileLevel(String walletTransId);
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
            payWithOrder(mActivity, order);
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

    boolean shouldClearPendingOrder(EPaymentStatus resultStatus) {
        if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH) {
            return false;
        } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_UPGRADE_SAVECARD &&
                mRedirectListener != null) {
            return false;
        } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_UPGRADE &&
                mRedirectListener != null) {
            return false;
        }
        return true;
    }

    void clearPendingOrder() {
        mPendingOrder = null;
        mPendingChannel = null;
    }

    public void setShowNotificationLinkCard(boolean showNotificationLinkCard) {
        mShowNotificationLinkCard = showNotificationLinkCard;
    }
}
