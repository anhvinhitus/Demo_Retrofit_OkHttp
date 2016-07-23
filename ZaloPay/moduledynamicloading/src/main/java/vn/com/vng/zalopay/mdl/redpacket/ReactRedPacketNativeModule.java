package vn.com.vng.zalopay.mdl.redpacket;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;
import vn.com.vng.zalopay.mdl.AlertDialogProvider;
import vn.com.vng.zalopay.mdl.error.PaymentError;

/**
 * Created by longlv on 17/07/2016.
 * define methods that had been called by React Native
 */
public class ReactRedPacketNativeModule extends ReactContextBaseJavaModule
        implements ActivityEventListener, LifecycleEventListener {

    private RedPacketStore.Repository mRedPackageRepository;
    private FriendStore.Repository mFriendRepository;
    private final IRedPacketPayService mPaymentService;
    private AlertDialogProvider mDialogProvider;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private CountDownTimer mTimerGetTranStatus;
    private boolean isRunningGetTranStatus;

    public ReactRedPacketNativeModule(ReactApplicationContext reactContext,
                                      RedPacketStore.Repository redPackageRepository,
                                      FriendStore.Repository friendRepository,
                                      IRedPacketPayService payService,
                                      AlertDialogProvider sweetAlertDialog) {
        super(reactContext);
        this.mRedPackageRepository = redPackageRepository;
        this.mFriendRepository = friendRepository;
        this.mPaymentService = payService;
        this.mDialogProvider = sweetAlertDialog;
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayRedPacketApi";
    }

    @ReactMethod
    public void createRedPacketBundleOrder(int quantity,
                                           double totalLuck,
                                           double amountEach,
                                           int type,
                                           String sendMessage,
                                           final Promise promise) {
        Subscription subscription =
                mRedPackageRepository.createBundleOrder(quantity, (long) totalLuck, (long) amountEach, type, sendMessage)
                .subscribe(new DefaultSubscriber<BundleOrder>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on getting CreateBundleOrderSubscriber");
                        if (e instanceof BodyException) {
                            int errorCode = ((BodyException) e).errorCode;
                            String message = ((BodyException) e).message;
                            errorCallback(promise, errorCode, message);
                        }
                    }

                    @Override
                    public void onNext(BundleOrder bundleOrder) {
                        Timber.d("onNext bundleOrder [%s]", bundleOrder);
                        if (bundleOrder == null) {
                            errorCallback(promise, PaymentError.ERR_CODE_INPUT, "bundleOrder null");
                        } else {
                            pay(bundleOrder, promise);
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void pay(final BundleOrder bundleOrder, final Promise promise) {
        if (bundleOrder == null) {
            return;
        }
        mPaymentService.pay(getCurrentActivity(), bundleOrder, new IRedPacketPayListener() {
            @Override
            public void onParameterError(String param) {
                Timber.d("onParameterError");
                errorCallback(promise, PaymentError.ERR_CODE_INPUT, param);
            }

            @Override
            public void onResponseError(int status) {
                Timber.d("onResponseError status [%s]", status);
                errorCallback(promise, status, PaymentError.getErrorMessage(status));
            }

            @Override
            public void onResponseSuccess(Bundle bundle) {
                Timber.d("onResponseSuccess bundle [%s]", bundle);
                WritableMap data = Arguments.createMap();
                if (bundleOrder != null) {
                    data.putString("bundleid", String.valueOf(bundleOrder.bundleId));
                }
                successCallback(promise, data);
            }

            @Override
            public void onResponseTokenInvalid() {

            }

            @Override
            public void onResponseCancel() {
                Timber.d("onResponseCancel");
                errorCallback(promise, PaymentError.ERR_CODE_USER_CANCEL, PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
            }

            @Override
            public void onNotEnoughMoney() {

            }
        });
    }

    @ReactMethod
    public void submitToSendBundle(String strBundleID, ReadableArray friends, final Promise promise) {
        final long bundleID;
        try {
            bundleID = Long.valueOf(strBundleID);
        } catch (NumberFormatException e) {
            Timber.e(e, "submitToSendBundle throw NumberFormatException");
            return;
        }
        if (bundleID <= 0) {
            return;
        }
        List<Long> friendList = transform(friends);
        Subscription subscription = mRedPackageRepository.sendBundle(bundleID, friendList)
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean;
                    }
                })
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on getting SubmitToSendSubscriber");
                        if (e instanceof BodyException) {
                            int errorCode = ((BodyException) e).errorCode;
                            String message = ((BodyException) e).message;
                            errorCallback(promise, errorCode, message);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        Timber.d("onNext result [%s]", result);
                        WritableMap writableMap = Arguments.createMap();
                        writableMap.putBoolean("result", result);
                        successCallback(promise, writableMap);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void startTaskGetTransactionStatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("startTaskGetTransactionStatus packetId [%s] transId [%s]", packageId, zpTransId);
        //showLoading();
        if (mTimerGetTranStatus != null) {
            mTimerGetTranStatus.cancel();
        }
        isRunningGetTranStatus = false;
        mTimerGetTranStatus = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Timber.d("onTick");
                getpackagestatus(packageId, zpTransId, promise);
            }

            @Override
            public void onFinish() {
                Timber.d("onFinish");
                //hideLoading();
                showDialogRetryGetTranStatus(packageId, zpTransId, promise);
            }
        }.start();
    }

    private void showLoading() {
        mDialogProvider.showLoading(getCurrentActivity());
    }

    private void hideLoading() {
        mDialogProvider.hideLoading();
    }

    private void showDialogRetryGetTranStatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("showDialogRetryGetTranStatus start");
        if (getCurrentActivity() == null) {
            return;
        }

        DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                errorCallback(promise, PaymentError.ERR_CODE_USER_CANCEL, PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
                dialog.dismiss();
            }
        };

        DialogInterface.OnClickListener onConfirmListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startTaskGetTransactionStatus(packageId, zpTransId, promise);
                dialog.dismiss();
            }
        };

        mDialogProvider.showWarningAlertDialog(getCurrentActivity(),
                "Giao dịch vẫn còn đang xử lý. Bạn có muốn tiếp tục?",
                "Đóng", "Thử lại",
                onCancelListener, onConfirmListener);
        Timber.d("showDialogRetryGetTranStatus end");
    }

    private void getpackagestatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("getpackagestatus isRunningGetTranStatus [%s]", isRunningGetTranStatus);
        if (isRunningGetTranStatus) {
            return;
        }
        isRunningGetTranStatus = true;
        mRedPackageRepository.getpackagestatus(packageId, zpTransId, "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<PackageStatus>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("getpackagestatus onError");
                        isRunningGetTranStatus = false;
                    }

                    @Override
                    public void onNext(PackageStatus packageStatus) {
                        Timber.d("getpackagestatus onNext, mTimerGetTranStatus [%s]", mTimerGetTranStatus);
                        if (mTimerGetTranStatus != null) {
                            mTimerGetTranStatus.cancel();
                        }
                        //hideLoading();
                        successCallback(promise, transform(packageStatus));
                        Timber.d("set open status 1 for packet: %s with amount: [%s]", packageId, packageStatus.amount);
                        mRedPackageRepository.setPacketIsOpen(packageId, packageStatus.amount).subscribe(new DefaultSubscriber<Void>());
                        isRunningGetTranStatus = false;
                    }
                });
    }

    private List<Long> transform(ReadableArray friends) {
        List<Long> friendList = new ArrayList<>();
        if (friends == null || friends.size() <= 0) {
            return friendList;
        }
        for (int i = 0; i < friends.size(); i++) {
            long friendId = 0;
            try {
                friendId = Long.valueOf(friends.getString(i));
            } catch (NumberFormatException e) {
                Timber.e(e, "transform friends from react native");
            }
            if (friendId <= 0) {
                continue;
            }
            friendList.add(friendId);
        }
        return friendList;
    }

    @ReactMethod
    public void openPacket(String strPackageID, String strBundleID, final Promise promise) {
        Timber.d("openPacket start");
        final long packageID;
        final long bundleID;
        try {
            packageID = Long.valueOf(strPackageID);
            bundleID = Long.valueOf(strBundleID);
        } catch (NumberFormatException e) {
            Timber.e(e, "openPacket throw NumberFormatException");
            errorCallback(promise, PaymentError.ERR_CODE_INPUT, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
            return;
        }
        Timber.d("openPacket packageID [%s] bundleID [%s]", packageID, bundleID);
        if (packageID <= 0 || bundleID <= 0) {
            errorCallback(promise, PaymentError.ERR_CODE_INPUT, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
            return;
        }
        Subscription subscription = mRedPackageRepository.submitOpenPackage(packageID, bundleID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<SubmitOpenPackage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on openPacket");
                        //hideLoading();
                        if (e instanceof BodyException) {
                            int errorCode = ((BodyException) e).errorCode;
                            String message = ((BodyException) e).message;
                            errorCallback(promise, errorCode, message);
                        } else {
                            errorCallback(promise, PaymentError.ERR_CODE_UNKNOWN, null);
                        }
                    }

                    @Override
                    public void onNext(SubmitOpenPackage submitOpenPackage) {
                        Timber.d("openPacket %s", submitOpenPackage);
                        startTaskGetTransactionStatus(submitOpenPackage.packageID, submitOpenPackage.zpTransID, promise);
                    }
                });
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getAllFriend(Promise promise) {
        Timber.d("getAllFriend promise [%s]", promise);
        Subscription subscription = mFriendRepository.listZaloFriendFromDb()
                .map(new Func1<List<ZaloFriendGD>, WritableArray>() {
                    @Override
                    public WritableArray call(List<ZaloFriendGD> zaloFriendGDs) {
                        return transformListFriend(zaloFriendGDs);
                    }
                })
                .subscribe(new GetAllFriendSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getReceivedPacket(String packetId, final Promise promise) {
        Timber.d("get received packet detail: [%s]", packetId);
        try {
            if (TextUtils.isEmpty(packetId)) {
                promise.reject("EMPTY PACKETID", "Invalid argument");
                return;
            }

            long packetIdValue = Long.parseLong(packetId);
            Timber.d("packetId: %d", packetIdValue);
            Subscription subscription = mRedPackageRepository.getReceivedPacket(packetIdValue)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<ReceivePackage>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            promise.reject("EXCEPTION", e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                        }

                        @Override
                        public void onNext(ReceivePackage receivePackage) {
                            Timber.d("received packet: %s", receivePackage.packageID);
                            promise.resolve(transform(receivePackage));
                        }
                    });
            compositeSubscription.add(subscription);
        } catch (Exception ex) {
            promise.reject("EXCEPTION", ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        }
    }

    private WritableArray transformListFriend(List<ZaloFriendGD> zaloFriendGDs) {
        if (Lists.isEmptyOrNull(zaloFriendGDs))
            return null;
        WritableArray listFriends = Arguments.createArray();
        for (ZaloFriendGD zaloFriendGD : zaloFriendGDs) {
            if (zaloFriendGD == null) {
                continue;
            }
            WritableMap friendItem = Arguments.createMap();
            friendItem.putString("displayName", zaloFriendGD.getDisplayName());
            friendItem.putString("ascciDisplayName", zaloFriendGD.getFulltextsearch());
            friendItem.putString("userId", String.valueOf(zaloFriendGD.getId()));
            friendItem.putInt("userGender", zaloFriendGD.getUserGender());
            friendItem.putBoolean("usingApp", zaloFriendGD.getUsingApp());
            friendItem.putString("avatar", zaloFriendGD.getAvatar());
            listFriends.pushMap(friendItem);
        }
        return listFriends;
    }

    private WritableMap transform(PackageStatus packageStatus) {
        if (packageStatus == null) {
            return null;
        }
        WritableMap writableMap = Arguments.createMap();
        writableMap.putBoolean("isprocessing", packageStatus.isProcessing);
        writableMap.putString("zpTransid", packageStatus.zpTransID);
        writableMap.putDouble("reqdate", packageStatus.reqdate);
        writableMap.putDouble("amount", packageStatus.amount);
        writableMap.putDouble("balance", packageStatus.balance);
        writableMap.putString("data", packageStatus.data);
        return writableMap;
    }

    private WritableMap transform(ReceivePackage packet) {
        if (packet == null) {
            return null;
        }
        WritableMap writableMap = Arguments.createMap();
        writableMap.putDouble("packetId", packet.packageID);
        writableMap.putDouble("bundleId", packet.bundleID);
        writableMap.putString("senderName", packet.senderFullName);
        writableMap.putString("senderAvatar", packet.senderAvatar);
        writableMap.putString("message", packet.message);
        writableMap.putDouble("amount", packet.amount);
        return writableMap;
    }

    @ReactMethod
    public void getPacketsFromBundle(String strBundleID, final Promise promise) {
        Timber.d("getPackageInBundle strBundleID [%s]", strBundleID);
        if (TextUtils.isEmpty(strBundleID)) {
            promise.reject("EMPTY BUNDLEID", "Invalid argument");
            return;
        }
        try {
            long bundleId = Long.parseLong(strBundleID);
            mRedPackageRepository.getPacketsInBundle(bundleId)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<List<PackageInBundle>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.w(e, "Exception while fetching packets");
                            promise.reject("EXCEPTION", e.getMessage());
                        }

                        @Override
                        public void onNext(List<PackageInBundle> packageInBundles) {
                            WritableArray array = transform(packageInBundles);
                            promise.resolve(array);
                        }

                        private WritableArray transform(List<PackageInBundle> list) {
                            WritableArray array = Arguments.createArray();
                            if (list == null) {
                                Timber.d("transform: Null parameter");
                                return array;
                            }

                            for (PackageInBundle packet : list) {
                                WritableMap map = Arguments.createMap();
                                map.putDouble("amount", packet.amount);
                                map.putBoolean("isLuckiest", packet.isLuckiest);
                                map.putString("revAvatarURL", packet.revAvatarURL);
                                map.putString("revFullName", packet.revFullName);
                                map.putDouble("openTime", packet.openTime);

                                array.pushMap(map);
                            }

                            return array;
                        }
                    });
        } catch (Exception e) {
            Timber.w(e, "Exception while fetching packets");
            promise.reject("EXCEPTION", e.getMessage());
        }
    }

    @ReactMethod
    public void isPacketOpen(final String packetId, final Promise promise) {
        Timber.d("query open status for packet: %s", packetId);
        Subscription subscription = mRedPackageRepository.isPacketOpen(packetId).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putInt("code", 0);
                Timber.d(e, "Error while query packet open status");
                promise.reject("-1", e.getMessage());
            }

            @Override
            public void onNext(Boolean aBoolean) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putInt("code", aBoolean ? 1 : 0);
                Timber.d("open status [%s] for packet: %s", aBoolean, packetId);
                promise.resolve(writableMap);
            }
        });

        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getSentBundleList(final Promise promise) {
        Subscription subscription = mRedPackageRepository.getSentBundleList()
                .subscribe(new Observer<List<SentBundle>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        WritableMap writableMap = Arguments.createMap();
                        writableMap.putInt("code", 0);
                        Timber.d(e, "Error while get sent packet");
                        promise.reject("-1", e.getMessage());
                    }

                    @Override
                    public void onNext(List<SentBundle> sentBundles) {
                        WritableArray writableArray = transform(sentBundles);
                        successCallback(promise, writableArray);
                    }
                });
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getReceivePacketList(final Promise promise) {

    }

    private WritableArray transform(List<SentBundle> sentBundles) {
        WritableArray writableArray = Arguments.createArray();
        if (sentBundles == null || sentBundles.size() <= 0) {
            return  writableArray;
        }
        for (SentBundle sentBundle : sentBundles) {
            WritableMap writableMap = transform(sentBundle);
            writableArray.pushMap(writableMap);
        }
        return null;
    }

    private WritableMap transform(SentBundle sentBundle) {
        WritableMap writableMap = Arguments.createMap();
        if (sentBundle == null) {
            return writableMap;
        }
        writableMap.putDouble("bundleid", sentBundle.bundleID);
        writableMap.putString("sendzalopayid", sentBundle.sendZaloPayID);
        writableMap.putDouble("createtime", sentBundle.createTime);
        writableMap.putDouble("lastOpenTime", sentBundle.lastOpenTime);
        writableMap.putDouble("totalluck", sentBundle.totalLuck);
        writableMap.putDouble("type", sentBundle.type);
        writableMap.putDouble("numofpackages", sentBundle.numOfPackages);
        writableMap.putDouble("numofopenedpakages", sentBundle.numOfOpenedPakages);
        return writableMap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
    }

    @Override
    public void onHostResume() {
        Timber.d("onResume");
    }

    @Override
    public void onHostPause() {
        Timber.d("onPause");
    }

    @Override
    public void onHostDestroy() {

        unsubscribeIfNotNull(compositeSubscription);

        getReactApplicationContext().removeActivityEventListener(this);
        getReactApplicationContext().removeLifecycleEventListener(this);
        Timber.d("onDestroy");
    }

    public void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    private void successCallback(Promise promise, WritableMap object) {
        Timber.d("successCallback promise [%s]", promise);
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", PaymentError.ERR_CODE_SUCCESS);
        if (object != null) {
            item.putMap("data", object);
        }
        promise.resolve(item);
    }

    private void successCallback(Promise promise, WritableArray array) {
        Timber.d("successCallback promise [%s]", promise);
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", PaymentError.ERR_CODE_SUCCESS);
        if (array != null) {
            item.putArray("data", array);
        }
        promise.resolve(item);
    }

    private void errorCallback(Promise promise, int errorCode, String message) {
        Timber.d("errorCallback start errorCode [%s] message [%s]", errorCode, message);
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", errorCode);
        if (!TextUtils.isEmpty(message)) {
            item.putString("message", message);
        }
        promise.resolve(item);
    }

    private class GetPacketInBundleSub extends DefaultSubscriber<List<PackageInBundle>> {
        Promise mPromise;
        public GetPacketInBundleSub(Promise promise) {
            mPromise = promise;
        }

        @Override
        public void onError(Throwable e) {
            if (mPromise != null) {
                mPromise.reject(e);
            }
        }

        @Override
        public void onNext(List<PackageInBundle> packageInBundles) {
            if (mPromise == null) {
                return;
            }
            WritableArray writableArray = Arguments.createArray();
            for (PackageInBundle packageInBundle : packageInBundles) {
                if (packageInBundle == null) {
                    continue;
                }
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("packageid", packageInBundle.bundleID);
                writableMap.putDouble("packageid", packageInBundle.packageID);
                writableMap.putDouble("amount", packageInBundle.amount);
                writableMap.putDouble("opentime", packageInBundle.openTime);
                writableMap.putDouble("revzaloid", packageInBundle.revZaloID);
                writableMap.putString("revzalopayid", packageInBundle.revZaloPayID);
                writableMap.putBoolean("isluckiest", packageInBundle.isLuckiest);
                writableMap.putString("revavatarurl", packageInBundle.revAvatarURL);
                writableMap.putString("revfullname", packageInBundle.revFullName);
                writableMap.putString("sendmessage", packageInBundle.sendMessage);
                writableArray.pushMap(writableMap);
            }
            successCallback(mPromise, writableArray);
        }
    }
}
