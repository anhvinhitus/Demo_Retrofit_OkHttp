package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.AndroidUtils;
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

    public interface IViewListener {
        Activity getActivity();
    }

    public interface IResponseListener {
        void onParameterError(String param);

        void onResponseError(int status);

        void onResponseSuccess(ZPPaymentResult zpPaymentResult);

        void onResponseTokenInvalid();

        void onResponseCancel();
    }

    private final IViewListener viewListener;
    private final IResponseListener responseListener;
    private final ZaloPayRepository zaloPayRepository;

    public PaymentWrapper(ZaloPayRepository zaloPayRepository, IViewListener viewListener, IResponseListener responseListener) {
        this.zaloPayRepository = zaloPayRepository;
        this.viewListener = viewListener;
        this.responseListener = responseListener;
    }

    public void payWithToken(long appId, String transactionToken) {
        zaloPayRepository.getOrder(appId, transactionToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetOrderSubscriber());
    }

    public void transfer(Order order, String displayName, String avatar, String phoneNumber) {
        UserInfo mUserInfo = new UserInfo();
        mUserInfo.phonenumber = phoneNumber;
        mUserInfo.userimage = avatar;
        mUserInfo.username = displayName;
        EPaymentChannel forcedPaymentChannel = EPaymentChannel.WALLET_TRANSFER;
        ZPWPaymentInfo paymentInfo = transform(order);

        callPayAPI(paymentInfo, forcedPaymentChannel, mUserInfo);
    }

    public void payWithDetail(long appID, String appTransID, String appUser, long appTime, long amount, String itemName, String description, String embedData, String mac) {
        Timber.d("payWithDetail start.......");
        Timber.d("appID %s appTransId: %s appUser:%s appTime:%s amount:%s itemName:%s itemNam:%s description:%s embedData:%s mac:%s", appID, appTransID, appUser, appTime, amount, itemName, amount, itemName, description, embedData, mac);
        if (appID < 0) {
            responseListener.onParameterError(Constants.APPID);
            return;
        }
        if (TextUtils.isEmpty(appTransID)) {
            responseListener.onParameterError(Constants.APPTRANSID);
            return;
        }
        if (TextUtils.isEmpty(appUser)) {
            responseListener.onParameterError(Constants.APPUSER);
            return;
        }
        if (appTime <= 0) {
            responseListener.onParameterError(Constants.APPTIME);
            return;
        }
        if (amount <= 0) {
            responseListener.onParameterError(Constants.AMOUNT);
            return;
        }
        if (TextUtils.isEmpty(itemName)) {
            responseListener.onParameterError(Constants.ITEM);
            return;
        }
        if (TextUtils.isEmpty(embedData)) {
            responseListener.onParameterError(Constants.DESCRIPTION);
            return;
        }
        if (TextUtils.isEmpty(mac)) {
            responseListener.onParameterError(Constants.MAC);
            return;
        }

        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (user == null || TextUtils.isEmpty(user.uid) || TextUtils.isEmpty(user.accesstoken)) {
            responseListener.onParameterError("uid");
            return;
        }

        ZPWPaymentInfo paymentInfo = createPaymentInfo(appID, appTransID, appUser, appTime, amount, itemName, description, embedData, mac, user);
        callPayAPI(paymentInfo);
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
        if (viewListener == null) {
            return;
        }
        User user = AndroidApplication.instance().getUserComponent().currentUser();

        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.zaloUserID = user.uid;
        paymentInfo.zaloPayAccessToken = user.accesstoken;
        paymentInfo.walletTransID = walletTransId;

        ZingMobilePayApplication.saveCardMap(viewListener.getActivity(), paymentInfo, listener);
    }

    private void callPayAPI(ZPWPaymentInfo paymentInfo, EPaymentChannel paymentChannel) {
        EPaymentChannel forcedPaymentChannel = paymentChannel;
        int profileLevel = getUserProfileLevel();
        String permissionsStr = getUserPermission();
        if (profileLevel < 0 || TextUtils.isEmpty(permissionsStr)) {
            return;
        }
        ZingMobilePayService.pay(viewListener.getActivity(), forcedPaymentChannel, paymentInfo, profileLevel, permissionsStr, zpPaymentListener);
    }

    private void callPayAPI(ZPWPaymentInfo paymentInfo, EPaymentChannel paymentChannel, UserInfo userInfo) {
        EPaymentChannel forcedPaymentChannel = paymentChannel;
        int profileLevel = getUserProfileLevel();
        String permissionsStr = getUserPermission();
        if (profileLevel < 0 || TextUtils.isEmpty(permissionsStr)) {
            return;
        }
        ZingMobilePayService.pay(viewListener.getActivity(), forcedPaymentChannel, paymentInfo, profileLevel, permissionsStr, userInfo, zpPaymentListener);
    }

    private int getUserProfileLevel() {
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (user == null) {
            return -1;
        }
        return user.profilelevel;
    }

    private String getUserPermission() {
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        if (user == null) {
            return null;
        }
        int profileLevel = user.profilelevel;
        String permissionsStr = "{\"profilelevelpermisssion\":";
        permissionsStr += JsonUtil.toJsonArrayString(user.profilePermisssions);
        permissionsStr += "}";
        Timber.d("permissionsStr====%s", permissionsStr);
        return permissionsStr;
    }

    private void callPayAPI(ZPWPaymentInfo paymentInfo) {
        callPayAPI(paymentInfo, null);
    }

    @NonNull
    private ZPWPaymentInfo createPaymentInfo(long appID, String appTransID, String appUser, long appTime, long amount, String itemName, String description, String embedData, String mac, User user) {
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.appID = appID;
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;
        paymentInfo.appTime = appTime;
        paymentInfo.appTransID = appTransID;
        paymentInfo.itemName = itemName;
        paymentInfo.amount = amount;
        paymentInfo.description = description;
        paymentInfo.embedData = embedData;
        //lap vao ví appId = appUser = 1
        paymentInfo.appUser = appUser;
        paymentInfo.mac = mac;
        return paymentInfo;
    }

    @NonNull
    private ZPWPaymentInfo transform(Order order) {
        User user = AndroidApplication.instance().getUserComponent().currentUser();
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

        paymentInfo.appID = order.getAppid();
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;
        paymentInfo.appTime = Long.valueOf(order.getApptime());
        paymentInfo.appTransID = order.getApptransid();
        Timber.d("paymentInfo.appTransID: %s", paymentInfo.appTransID);
        paymentInfo.itemName = order.getItem();
        paymentInfo.amount = Long.parseLong(order.getAmount());
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
        navigator.startPreProfileActivity(viewListener.getActivity(), walletTransID);
    }

    private ZPPaymentListener zpPaymentListener = new ZPPaymentListener() {
        @Override
        public void onComplete(ZPPaymentResult pPaymentResult) {
            if (pPaymentResult == null) {
                if (!AndroidUtils.isNetworkAvailable(viewListener.getActivity())) {
                    responseListener.onResponseError(PaymentError.ERR_CODE_INTERNET);
                } else {
                    responseListener.onResponseError(PaymentError.ERR_CODE_SYSTEM);
                }
            } else {
                int resultStatus = pPaymentResult.paymentStatus.getNum();
                if (resultStatus == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
                    responseListener.onResponseSuccess(pPaymentResult);
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
                } else {
                    responseListener.onResponseError(resultStatus);
                }
            }
        }

        @Override
        public void onCancel() {
            responseListener.onResponseCancel();
        }

        @Override
        public void onSMSCallBack(String appTransID) {

        }
    };

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
            Timber.w(e, "onError " + e);
            responseListener.onParameterError("token");
        }
    }
}
