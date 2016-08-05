package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.JsonUtil;
import vn.com.zalopay.wallet.application.ZingMobilePayApplication;
import vn.com.zalopay.wallet.application.ZingMobilePayService;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.entity.user.UserInfo;
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


    private ZPPaymentListener zpPaymentListener = new ZPPaymentListener() {
        @Override
        public void onComplete(ZPPaymentResult pPaymentResult) {
            Timber.d("onComplete");
            if (pPaymentResult == null) {
                if (NetworkHelper.isNetworkAvailable(viewListener.getActivity())) {
                    responseListener.onResponseError(PaymentError.ERR_CODE_SYSTEM);
                } else {
                    responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
                }
            } else {
                int resultStatus = pPaymentResult.paymentStatus.getNum();
                if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {

                    responseListener.onResponseSuccess(pPaymentResult);

                    updateBalance();
                    updateTransactionSuccess();

                } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()) {
                    responseListener.onResponseTokenInvalid();
                } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_UPGRADE.getNum()) {
                    //Hien update profile level 2
                    startUpdateProfileLevel(null);
                } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_UPGRADE_SAVECARD.getNum()) {
                    String walletTransId = null;
                    if (pPaymentResult.paymentInfo != null) {
                        walletTransId = pPaymentResult.paymentInfo.walletTransID;
                    }
                    //Hien update profile level 2
                    startUpdateProfileLevel(walletTransId);
                } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH.getNum()) {
                    responseListener.onResponseError(resultStatus);
                    responseListener.onNotEnoughMoney();
                } else {
                    if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_CLOSE.getNum()) {
                        responseListener.onResponseError(PaymentError.ERR_CODE_USER_CANCEL);
                    } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_INPUT_INVALID.getNum()) {
                        responseListener.onResponseError(PaymentError.ERR_CODE_INPUT);
                    } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_FAIL.getNum()) {
                        responseListener.onResponseError(PaymentError.ERR_CODE_FAIL);
                    } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_PROCESSING.getNum()) {
                        responseListener.onResponseError(PaymentError.ERR_CODE_PROCESSING);
                    } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_SERVICE_MAINTENANCE.getNum()) {
                        responseListener.onResponseError(PaymentError.ERR_CODE_SERVICE_MAINTENANCE);
                    } else if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_NO_INTERNET.getNum()) {
                        responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
                    } else {
                        responseListener.onResponseError(PaymentError.ERR_CODE_UNKNOWN);
                    }

                    updateBalance();
                    updateTransctionFail();
                }
            }
        }

        @Override
        public void onCancel() {
            Timber.d("onCancel");

            responseListener.onResponseCancel();
        }

        @Override
        public void onSMSCallBack(String appTransID) {
            Timber.d("onSMSCallBack");
        }
    };

    public PaymentWrapper(BalanceStore.Repository balanceRepository, ZaloPayRepository zaloPayRepository, TransactionStore.Repository transactionRepository,
                          IViewListener viewListener, IResponseListener responseListener) {
        this.balanceRepository = balanceRepository;
        this.zaloPayRepository = zaloPayRepository;
        this.viewListener = viewListener;
        this.responseListener = responseListener;
        this.transactionRepository = transactionRepository;
    }

    public void payWithToken(long appId, String transactionToken) {
        zaloPayRepository.getOrder(appId, transactionToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetOrderSubscriber());
    }

    private UserInfo getUserInfo(String displayName, String avatar, String phoneNumber) {
        UserInfo mUserInfo = getUserInfo();
        mUserInfo.phoneNumber = phoneNumber;
        mUserInfo.userName = displayName;
//        mUserInfo.userimage = avatar;
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
            mUserInfo.zaloPayUserId = user.uid;
            mUserInfo.accessToken = user.accesstoken;
            mUserInfo.level = getUserProfileLevel();
            mUserInfo.userProfile = getUserPermission();
        }
        return mUserInfo;
    }

    public void transfer(Order order, String displayName, String avatar, String phoneNumber) {

        EPaymentChannel forcedPaymentChannel = EPaymentChannel.WALLET_TRANSFER;
        ZPWPaymentInfo paymentInfo = transform(order);
        paymentInfo.userInfo = getUserInfo(displayName, avatar, phoneNumber);
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

        if (TextUtils.isEmpty(user.uid)) {
            Timber.i("payWithOrder: Uid is invalid");
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

    public void linkCard(Order order) {
        Timber.d("payWithOrder: Start");
        if (order == null) {
            Timber.i("payWithOrder: order is invalid");
            responseListener.onParameterError("order");
//            showErrorView(mView.getContext().getString(R.string.order_invalid));
            return;
        }
        Timber.d("payWithOrder: Order is valid");
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (TextUtils.isEmpty(user.uid)) {
            Timber.i("payWithOrder: Uid is invalid");
            responseListener.onParameterError("uid");
//            showErrorView(mView.getContext().getString(R.string.user_invalid));
            return;
        }
        try {
            ZPWPaymentInfo paymentInfo = transform(order);

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
        ZingMobilePayApplication.saveCardMap(viewListener.getActivity(), paymentInfo, listener);
    }

    private void callPayAPI(ZPWPaymentInfo paymentInfo, EPaymentChannel paymentChannel) {
        if (paymentInfo == null) {
            return;
        }
        if (paymentInfo.userInfo == null) {
            paymentInfo.userInfo = getUserInfo();
        }
        if (paymentInfo.userInfo.level < 0 || TextUtils.isEmpty(paymentInfo.userInfo.userProfile)) {
            zpPaymentListener.onCancel();
            return;
        }
        if (balanceRepository != null) {
            paymentInfo.userInfo.balance = balanceRepository.currentBalance();
        }


        Timber.d("Call Pay to sdk");
        ZingMobilePayService.pay(viewListener.getActivity(), paymentChannel, paymentInfo, zpPaymentListener);
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
        permissionsStr += JsonUtil.toJsonArrayString(user.profilePermissions);
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

        paymentInfo.appID = order.getAppid();
        paymentInfo.userInfo = getUserInfo();
        paymentInfo.appTime = order.getApptime();
        paymentInfo.appTransID = order.getApptransid();
        Timber.d("paymentInfo.appTransID: %s", paymentInfo.appTransID);
        paymentInfo.itemName = order.getItem();
        paymentInfo.amount = order.getAmount();
        paymentInfo.description = order.getDescription();
        paymentInfo.embedData = order.getEmbeddata();
        //lap vao ví appId = appUser = 1
        paymentInfo.appUser = order.getAppuser();
        paymentInfo.mac = order.getMac();
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

        void onResponseError(int status);

        void onResponseSuccess(ZPPaymentResult zpPaymentResult);

        void onResponseTokenInvalid();

        void onResponseCancel();

        void onNotEnoughMoney();
    }

    private final class GetOrderSubscriber extends DefaultSubscriber<Order> {
        public GetOrderSubscriber() {
        }

        @Override
        public void onNext(Order order) {
            Timber.d("getOrder response: %s", order.getItem());
            payWithOrder(order);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }

            Timber.w(e, "onError " + e);
            if (e instanceof NetworkConnectionException) {
                responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
            } else {
                responseListener.onParameterError("token");
            }
        }
    }


    private void updateTransactionSuccess() {
        Subscription subscription = transactionRepository.updateTransactionSuccess()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    private void updateTransctionFail() {
        Subscription subscription = transactionRepository.updateTransactionFail()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    private void updateBalance() {
        // update balance
        Subscription subscription = balanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }
}
