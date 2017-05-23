package vn.com.vng.zalopay.pw;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.PaymentFingerPrint;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.location.AppLocation;
import vn.com.vng.zalopay.location.LocationProvider;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.zpsdk.PaymentFeedBackCollector;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.ELinkAccType;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKPayment;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;

import static vn.com.zalopay.wallet.constants.PaymentError.DATA_INVALID;

/**
 * Created by huuhoa on 6/3/16.
 * Wrapper for handle common processing involves with wallet SDK
 */
public class PaymentWrapper {
    public interface IGetOrderCallback {
        void onResponseSuccess(Order order);

        void onResponseError(int status);
    }

    @Inject
    ZaloPayRepository zaloPayRepository;

    @Inject
    BalanceStore.Repository balanceRepository;

    @Inject
    TransactionStore.Repository transactionRepository;

    @Inject
    Navigator mNavigator;

    @Inject
    User mCurrentUser;

    final ILinkCardListener mLinkCardListener;
    final IRedirectListener mRedirectListener;
    final IResponseListener responseListener;

    Activity mActivity;
    boolean mShowNotificationLinkCard;

    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private ZPWPaymentInfo mPendingOrder;
    @TransactionType
    private int mPendingTransaction;
    private ZPPaymentListener mWalletListener;

    PaymentWrapper(IResponseListener responseListener, IRedirectListener redirectListener,
                   ILinkCardListener linkCardListener, boolean showNotificationLinkCard) {
        Timber.d("Create new instance of PaymentWrapper[%s]", this);
        this.responseListener = responseListener;
        this.mRedirectListener = redirectListener;
        this.mLinkCardListener = linkCardListener;
        this.mShowNotificationLinkCard = showNotificationLinkCard;
    }

    /**
     * Initialize internal components.
     * Should call this method right after calling build() from PaymentWrapperBuilder.
     *
     * Throws IllegalStateException when UserComponent is NULL
     */
    public void initializeComponents() {
        UserComponent component = getUserComponent();

        if (component == null) {
            throw new IllegalStateException("Illegal app's state to initialize PaymentWrapper");
        }

        component.inject(this);

        mWalletListener = new WalletListener(this, transactionRepository,
                balanceRepository, mCompositeSubscription);
    }

    public void payWithToken(Activity activity, long appId, String transactionToken, int source) {
        Timber.d("start payWithToken [%s-%s]", appId, transactionToken);
        mActivity = activity;
        Subscription subscription = zaloPayRepository.getOrder(appId, transactionToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetOrderSubscriber(source));
        mCompositeSubscription.add(subscription);
    }

    public void withdraw(@NonNull Activity activity, Order order) {
        int transactionType = TransactionType.WITHDRAW;
        ZPWPaymentInfo paymentInfo = transform(order);
        paymentInfo.userInfo = createUserInfo(mCurrentUser.displayName, mCurrentUser.avatar, String.valueOf(mCurrentUser.phonenumber), mCurrentUser.zalopayname);

        ZPApptransidLog log = new ZPApptransidLog();
        log.apptransid = order.apptransid;
        log.appid = order.appid;
        log.start_time = System.currentTimeMillis();
        ZPAnalytics.trackApptransidEvent(log);

        invokePayAPI(activity, paymentInfo, transactionType);
    }

    public void transfer(@NonNull Activity activity, Order order, String displayName, String avatar, String phoneNumber, String zaloPayName, int source) {
        int transactionType = TransactionType.MONEY_TRANSFER;
        mActivity = activity;
        ZPWPaymentInfo paymentInfo = transform(order);
        paymentInfo.userInfo = createUserInfo(displayName, mCurrentUser.avatar, phoneNumber, zaloPayName);
        paymentInfo.receiverInfo = createUserInfo(displayName, avatar, "", zaloPayName);

        ZPApptransidLog log = new ZPApptransidLog();
        log.apptransid = order.apptransid;
        log.appid = order.appid;
        log.source = source;
        log.start_time = System.currentTimeMillis();
        ZPAnalytics.trackApptransidEvent(log);

        invokePayAPI(activity, paymentInfo, transactionType);
    }

    public void payWithOrder(@NonNull Activity activity, Order order, int source) {
        Timber.d("payWithOrder: Start");
        if (order == null) {
            Timber.i("payWithOrder: order is invalid");
            responseListener.onParameterError("order");
//            showErrorView(mView.getContext().getString(R.string.order_invalid));
            return;
        }
        Timber.d("payWithOrder: Order is valid");

        User user = getUserComponent().currentUser();

        if (user == null) {
            Timber.i("payWithOrder: current user is null");
            responseListener.onParameterError("Thông tin người dùng không hợp lệ");
            return;
        }

        if (!user.hasZaloPayId()) {
            Timber.i("payWithOrder: zaloPayId is invalid");
            responseListener.onParameterError("uid");
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = transform(order);

            Timber.d("payWithOrder: ZPWPaymentInfo is ready");

            ZPApptransidLog log = new ZPApptransidLog();
            log.apptransid = order.apptransid;
            log.appid = order.appid;
            log.source = source;
            log.start_time = System.currentTimeMillis();
            ZPAnalytics.trackApptransidEvent(log);

//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, keyMac);
            invokePayAPI(activity, paymentInfo, TransactionType.PAY);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception with number format");
            responseListener.onParameterError("exception");
        }
    }

    // only used in CounterBeaconFragment
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

    public void linkCard(@NonNull Activity activity) {
        if (mCurrentUser == null) {
            Timber.i("payWithOrder: current user is null");
            responseListener.onParameterError("Thông tin người dùng không hợp lệ");
            return;
        }

        if (!mCurrentUser.hasZaloPayId()) {
            Timber.i("payWithOrder: zaloPayId is invalid");
            responseListener.onParameterError("uid");
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
            paymentInfo.appID = BuildConfig.ZALOPAY_APP_ID;
            paymentInfo.appTime = System.currentTimeMillis();

            ZPApptransidLog log = new ZPApptransidLog();
            log.apptransid = paymentInfo.appTransID;
            log.appid = paymentInfo.appID;
            log.start_time = System.currentTimeMillis();
            ZPAnalytics.trackApptransidEvent(log);

            invokePayAPI(activity, paymentInfo, TransactionType.LINK_CARD);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception with number format");
            responseListener.onParameterError("exception");
        }
    }

    public void linkAccount(@NonNull Activity activity, String bankType) {
        invokeManageBankAccountAPI(activity, bankType, ELinkAccType.LINK);
    }

    public void unlinkAccount(@NonNull Activity activity, String bankType) {
        invokeManageBankAccountAPI(activity, bankType, ELinkAccType.UNLINK);
    }

    private void invokeManageBankAccountAPI(@NonNull Activity activity, String bankType, ELinkAccType linkAccType) {
        if (mCurrentUser == null) {
            Timber.i("payWithOrder: current user is null");
            responseListener.onParameterError("Thông tin người dùng không hợp lệ");
            return;
        }

        if (!mCurrentUser.hasZaloPayId()) {
            Timber.i("Manager link account, zaloPayId is invalid");
            responseListener.onParameterError("uid");
            return;
        }
        try {
            LinkAccInfo linkAccInfo = new LinkAccInfo(bankType, linkAccType);

            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
            paymentInfo.appID = BuildConfig.ZALOPAY_APP_ID;
            paymentInfo.appTime = System.currentTimeMillis();
            paymentInfo.linkAccInfo = linkAccInfo;

            ZPApptransidLog log = new ZPApptransidLog();
            log.apptransid = paymentInfo.appTransID;
            log.appid = paymentInfo.appID;
            log.start_time = System.currentTimeMillis();
            ZPAnalytics.trackApptransidEvent(log);

            invokePayAPI(activity, paymentInfo, TransactionType.LINK_ACCOUNT);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception with number format");
            responseListener.onParameterError("exception");
        }
    }

    private boolean hasPendingOrder() {
        return (mPendingOrder != null);
    }

    private void continuePayPendingOrder() {
        if (!hasPendingOrder()) {
            Timber.d("continuePayPendingOrder but has not order");
            return;
        }

        //Require reset forceChannelIds & mappedCreditCard before continue payment
        mPendingOrder.forceChannelIds = null;
        mPendingOrder.mapBank = null;

        Timber.d("Continue pay pending order : userInfo [%s] zalopayId [%s] accessToken [%s]", mPendingOrder.userInfo, mPendingOrder.userInfo.zaloPayUserId, mPendingOrder.userInfo.accessToken);

        invokePayAPI(mActivity, mPendingOrder, mPendingTransaction);
    }

    private void onUpdateProfileAndLinkAcc(int resultCode) {
        if (resultCode == Activity.RESULT_CANCELED) {
            if (responseListener != null) {
                responseListener.onResponseError(PaymentError.ERR_CODE_USER_CANCEL);
            }
            clearPendingOrder();
        } else {
            if (mRedirectListener != null) {
                mRedirectListener.startLinkAccountActivity();
            } else {
                startLinkAccountActivity();
            }
        }
    }

    /**
     * Handle fragment/activity result
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult: requestCode[%s] resultCode[%s]", requestCode, resultCode);
        boolean shouldProcessPendingOrder = false;
        if (resultCode != Activity.RESULT_OK && resultCode != Activity.RESULT_CANCELED) {
            if (resultCode == Constants.RESULT_END_PAYMENT) {
                responseListener.onResponseError(PaymentError.ERR_CODE_USER_CANCEL);
            }
            return;
        }

        if (requestCode == Constants.REQUEST_CODE_DEPOSIT
                || requestCode == Constants.REQUEST_CODE_UPDATE_PROFILE_LEVEL_2
                || requestCode == Constants.REQUEST_CODE_LINK_BANK) {
            shouldProcessPendingOrder = true;
        } else if (requestCode == Constants.REQUEST_CODE_UPDATE_PROFILE_LEVEL_BEFORE_LINK_ACC) {
            if (resultCode == Activity.RESULT_CANCELED) {
                shouldProcessPendingOrder = true;
            } else {
                onUpdateProfileAndLinkAcc(requestCode);
                return;
            }
        }

        if (shouldProcessPendingOrder && hasPendingOrder()) {
            continuePayPendingOrder();
        }
    }

    void cleanup() {
        Timber.i("PaymentWrapper is cleaning up");
        mActivity = null;
        clearPendingOrder();
        mCompositeSubscription.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Timber.d("PaymentWrapper is finalize [%s]", this);
    }

    private UserInfo createUserInfo(String displayName, String avatar, String phoneNumber, String zaloPayName) {
        UserInfo mUserInfo = new UserInfo();
        mUserInfo.phoneNumber = phoneNumber;
        mUserInfo.userName = displayName;
        mUserInfo.zaloPayName = zaloPayName;
        mUserInfo.avatar = avatar;
        return mUserInfo;
    }

    private UserInfo assignBaseUserInfo(@NonNull UserInfo userInfo) {
        if (mCurrentUser == null) {
            return userInfo;
        }

        userInfo.zaloUserId = String.valueOf(mCurrentUser.zaloId);
        userInfo.zaloPayUserId = mCurrentUser.zaloPayId;
        userInfo.accessToken = mCurrentUser.accesstoken;
        userInfo.level = getUserProfileLevel();
        userInfo.userProfile = getUserPermission();
        userInfo.phoneNumber = getPhoneNumber();
        return userInfo;
    }

    private void invokePayAPI(@NonNull Activity owner, ZPWPaymentInfo paymentInfo, @TransactionType int transactionType) {
        mActivity = owner;
        if (paymentInfo == null || owner == null) {
            mActivity = null;
            return;
        }

        paymentInfo.userInfo = assignBaseUserInfo(paymentInfo.userInfo);
        if (paymentInfo.userInfo.level < 0 || TextUtils.isEmpty(paymentInfo.userInfo.userProfile)) {
            mWalletListener.onError(new CError(DATA_INVALID, owner.getString(R.string.please_update_profile)));
            mActivity = null;
            return;
        }
        if (balanceRepository != null) {
            paymentInfo.userInfo.balance = balanceRepository.currentBalance();
        }

        if (transactionType != TransactionType.LINK_CARD
                && transactionType != TransactionType.LINK_ACCOUNT
                && !validPaymentInfo(paymentInfo)) {
            responseListener.onAppError(owner.getString(R.string.data_invalid_try_again));
            Exception e = new Exception(
                    String.format("PaymentInfo is invalid, appId[%s] transId[%s] amount[%s] appTime[%s]  mac[%s]",
                            paymentInfo.appID,
                            paymentInfo.appTransID,
                            paymentInfo.amount,
                            paymentInfo.appTime,
                            paymentInfo.mac));
            Timber.e(e, e.getMessage());
            return;
        }

        Timber.d("Call Pay to sdk activity [%s] transactionType [%s] paymentInfo [%s]",
                owner, transactionType, paymentInfo);
        mPendingOrder = paymentInfo;
        mPendingTransaction = transactionType;

        ZPApptransidLog log = new ZPApptransidLog(paymentInfo.appTransID, ZPPaymentSteps.OrderStep_SDKInit, ZPPaymentSteps.OrderStepResult_None, System.currentTimeMillis());
        Timber.d("add log");
        ZPAnalytics.trackApptransidEvent(log);

        SDKPayment.pay(owner, transactionType, paymentInfo, mWalletListener, new PaymentFingerPrint(AndroidApplication.instance()), new PaymentFeedBackCollector());
    }

    private boolean validPaymentInfo(ZPWPaymentInfo paymentInfo) {
        return ((paymentInfo.amount > 0) &&
                (paymentInfo.appID > 0) &&
                !TextUtils.isEmpty(paymentInfo.appTransID) &&
                (paymentInfo.appTime > 0) &&
                !TextUtils.isEmpty(paymentInfo.mac));
    }

    private String getPhoneNumber() {
        if (mCurrentUser == null) {
            return "";
        }

        return PhoneUtil.formatPhoneNumber(mCurrentUser.phonenumber);
    }

    private int getUserProfileLevel() {
        if (mCurrentUser != null) {
            return mCurrentUser.profilelevel;
        }
        return -1;
    }

    private UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    private String getUserPermission() {
        if (mCurrentUser == null) {
            return null;
        }

        String permissionsStr = "{\"profilelevelpermisssion\":";
        permissionsStr += mCurrentUser.profilePermissions;
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
        paymentInfo.mLocation = transform(LocationProvider.getLocation());
        return paymentInfo;
    }

    private PaymentLocation transform(AppLocation appLocation) {
        if (appLocation == null) {
            return null;
        }

        PaymentLocation location = new PaymentLocation();
        location.latitude = appLocation.latitude;
        location.longitude = appLocation.longitude;
        return location;
    }

    void startDepositForResult() {
        if (mActivity == null) {
            return;
        }

        mNavigator.startDepositForResultActivity(mActivity);
    }

    void startUpdateProfile2ForResult() {
        if (mActivity == null) {
            return;
        }

        mNavigator.startUpdateProfile2ForResult(mActivity);
    }

    void startUpdateProfileBeforeLinkAcc() {
        if (mActivity == null) {
            return;
        }
        mNavigator.startUpdateProfileLevelBeforeLinkAcc(mActivity);
    }

    void startLinkCardActivity() {
        if (mActivity == null) {
            return;
        }

        mNavigator.startLinkCardActivityForResult(mActivity);
    }

    void startLinkAccountActivity() {
        if (mActivity == null) {
            return;
        }

        mNavigator.startLinkAccountActivityForResult(mActivity);
    }

    public interface ILinkCardListener {
        void onErrorLinkCardButInputBankAccount(DBaseMap bankInfo);
    }

    public interface IRedirectListener {
        void startUpdateProfileLevel();

        void startDepositForResult();

        void startLinkCardActivity();

        void startLinkAccountActivity();

        void startUpdateProfileBeforeLinkAcc();
    }

    public interface IResponseListener {
        void onParameterError(String param);

        void onResponseError(PaymentError status);

        void onResponseSuccess(ZPPaymentResult zpPaymentResult);

        void onResponseTokenInvalid();

        void onResponseAccountSuspended();

        void onAppError(String msg);

        void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId);
    }

    private final class GetOrderSubscriber extends DefaultSubscriber<Order> {
        int mSource;

        GetOrderSubscriber(int source) {
            this.mSource = source;
        }

        @Override
        public void onNext(Order order) {
            Timber.d("getOrder response : item [%s]", order.item);
            payWithOrder(mActivity, order, mSource);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Get order error");
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

    boolean shouldClearPendingOrder(@PaymentStatus int resultStatus) {

        switch (resultStatus) {
            case PaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH:
                return false;
            case PaymentStatus.ZPC_TRANXSTATUS_UPGRADE:
                return false;
            case PaymentStatus.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT_BEFORE_PAYMENT:
                return false;
            case PaymentStatus.ZPC_TRANXSTATUS_NEED_LINKCARD_BEFORE_PAYMENT:
                return false;
            case PaymentStatus.ZPC_TRANXSTATUS_UPLEVEL_AND_LINK_BANKACCOUNT_CONTINUE_PAYMENT:
                return false;
            default:
                return true;
        }
    }

    void clearPendingOrder() {
        Timber.d("clearPendingOrder");
        mPendingOrder = null;
    }

    public void setShowNotificationLinkCard(boolean showNotificationLinkCard) {
        mShowNotificationLinkCard = showNotificationLinkCard;
    }
}
