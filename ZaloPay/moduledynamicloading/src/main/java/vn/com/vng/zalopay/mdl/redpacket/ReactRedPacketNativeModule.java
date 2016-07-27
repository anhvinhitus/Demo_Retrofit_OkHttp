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

import java.util.Arrays;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;
import vn.com.vng.zalopay.mdl.AlertDialogProvider;
import vn.com.vng.zalopay.mdl.error.PaymentError;

/**
 * Created by longlv on 17/07/2016.
 * define methods that had been called by React Native
 */
public class ReactRedPacketNativeModule extends ReactContextBaseJavaModule
        implements ActivityEventListener, LifecycleEventListener {

    private UserConfig mUserConfig;
    private RedPacketStore.Repository mRedPackageRepository;
    private FriendStore.Repository mFriendRepository;
    private BalanceStore.Repository mBalanceRepository;
    private final IRedPacketPayService mPaymentService;
    private AlertDialogProvider mDialogProvider;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private CountDownTimer mTimerGetTranStatus;
    private boolean isRunningGetTranStatus;

    public ReactRedPacketNativeModule(ReactApplicationContext reactContext,
                                      RedPacketStore.Repository redPackageRepository,
                                      FriendStore.Repository friendRepository,
                                      BalanceStore.Repository balanceRepository,
                                      IRedPacketPayService payService,
                                      UserConfig userConfig,
                                      AlertDialogProvider sweetAlertDialog) {
        super(reactContext);
        this.mRedPackageRepository = redPackageRepository;
        this.mFriendRepository = friendRepository;
        this.mBalanceRepository = balanceRepository;
        this.mPaymentService = payService;
        this.mDialogProvider = sweetAlertDialog;
        this.mUserConfig = userConfig;
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
                        .subscribe(new RedPacketSubscriber<BundleOrder>(promise) {

                            @Override
                            public void onError(Throwable e) {
                                Timber.w(e, "error on getting CreateBundleOrderSubscriber");
                                super.onError(e);
                            }

                            @Override
                            public void onNext(BundleOrder bundleOrder) {
                                Timber.d("createBundleOrder onNext bundleOrder [%s]", bundleOrder);
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
                Timber.w("pay onParameterError");
                errorCallback(promise, PaymentError.ERR_CODE_INPUT, param);
            }

            @Override
            public void onResponseError(int status) {
                Timber.d("pay onResponseError status [%s]", status);
                if (!NetworkHelper.isNetworkAvailable(getCurrentActivity())) {
                    errorCallback(promise, PaymentError.ERR_CODE_INTERNET,
                            PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
                } else {
                    errorCallback(promise, status, PaymentError.getErrorMessage(status));
                }
            }

            @Override
            public void onResponseSuccess(Bundle bundle) {
                Timber.d("pay onResponseSuccess bundle [%s]", bundle);
                WritableMap data = Arguments.createMap();
                data.putString("bundleid", String.valueOf(bundleOrder.bundleId));
                mBalanceRepository.updateBalance();
                successCallback(promise, data);
            }

            @Override
            public void onResponseTokenInvalid() {
                Timber.d("pay onResponseTokenInvalid");
            }

            @Override
            public void onResponseCancel() {
                Timber.d("pay onResponseCancel");
                errorCallback(promise, PaymentError.ERR_CODE_USER_CANCEL,
                        PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
            }

            @Override
            public void onNotEnoughMoney() {
                Timber.d("pay onNotEnoughMoney");
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
        List<Long> friendList = DataMapper.transform(friends);
        Subscription subscription = mRedPackageRepository.sendBundle(bundleID, friendList)
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean;
                    }
                })
                .subscribe(new RedPacketSubscriber<Boolean>(promise) {

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on getting SubmitToSendSubscriber");
                        super.onError(e);
                    }

                    @Override
                    public void onNext(Boolean result) {
                        Timber.d("SubmitToSendSubscriber onNext result [%s]", result);
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
                Timber.d("GetTranStatus onTick");
                getpackagestatus(packageId, zpTransId, promise);
            }

            @Override
            public void onFinish() {
                Timber.d("GetTranStatus onFinish");
                showDialogRetryGetTranStatus(packageId, zpTransId, promise);
            }
        }.start();
    }

    private void showDialogRetryGetTranStatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("showDialogRetryGetTranStatus packageId [%s] zpTransId [%s]", packageId, zpTransId);
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
                        successCallback(promise, DataMapper.transform(packageStatus));
                        Timber.d("set open status 1 for packet: %s with amount: [%s]", packageId, packageStatus.amount);
                        mRedPackageRepository.setPacketIsOpen(packageId, packageStatus.amount).subscribe(new DefaultSubscriber<Void>());
                        mBalanceRepository.updateBalance();
                        isRunningGetTranStatus = false;
                    }
                });
    }

    @ReactMethod
    public void openPacket(String strPackageID, String strBundleID, final Promise promise) {
        Timber.d("openPacket strPackageID [%s] strBundleID [%s]", strBundleID, strBundleID);
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
        Timber.d("openPacket after cast packageID [%s] bundleID [%s]", packageID, bundleID);
        if (packageID <= 0 || bundleID <= 0) {
            errorCallback(promise, PaymentError.ERR_CODE_INPUT, PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
            return;
        }
        Subscription subscription = mRedPackageRepository.submitOpenPackage(packageID, bundleID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RedPacketSubscriber<SubmitOpenPackage>(promise) {

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on openPacket");
                        super.onError(e);
                    }

                    @Override
                    public void onNext(SubmitOpenPackage submitOpenPackage) {
                        Timber.d("openPacket onNext [%s]", submitOpenPackage);
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
                        return DataMapper.transform(zaloFriendGDs);
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
                    .subscribe(new RedPacketSubscriber<ReceivePackage>(promise) {

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                        }

                        @Override
                        public void onNext(ReceivePackage receivePackage) {
                            Timber.d("received packet: %s", receivePackage.packageID);
                            promise.resolve(DataMapper.transform(receivePackage));
                        }
                    });
            compositeSubscription.add(subscription);
        } catch (Exception ex) {
            promise.reject("EXCEPTION", ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        }
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
                    .subscribe(new RedPacketSubscriber<List<PackageInBundle>>(promise) {

                        @Override
                        public void onError(Throwable e) {
                            Timber.w(e, "Exception while fetching packets");
                            super.onError(e);
                        }

                        @Override
                        public void onNext(List<PackageInBundle> packageInBundles) {
                            WritableArray array = DataMapper.transform(packageInBundles);
                            promise.resolve(array);
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
        Subscription subscription = mRedPackageRepository.isPacketOpen(packetId)
                .subscribe(new RedPacketSubscriber<Boolean>(promise) {

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
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
    public void getSendBundleHistoryWithTimeStamp(final double createTime, final double count, final Promise promise) {
        Timber.d("getSendBundleHistoryWithTimeStamp createTime [%s] count [%s]", createTime, count);
        Subscription subscription = mRedPackageRepository.getSentBundleList((long) createTime, (int) count)
                .subscribe(new RedPacketSubscriber<GetSentBundle>(promise) {

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("getSendBundleHistoryWithTimeStamp onError");
                        super.onError(e);
                    }

                    @Override
                    public void onNext(GetSentBundle getSentBundle) {
                        Timber.d("getSendBundleHistoryWithTimeStamp onNext getSentBundle [%s]", getSentBundle);
                        WritableMap writableMap = DataMapper.transform(getSentBundle);
                        successCallback(promise, writableMap);
                    }
                });
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getReceivePacketHistoryWithTimeStamp(final double createTime, final double count, final Promise promise) {
        Timber.d("getReceivePacketHistoryWithTimeStamp createTime [%s] count [%s]", createTime, count);
        Subscription subscription = mRedPackageRepository.getReceivePacketList((long) createTime, (int) count)
                .subscribe(new RedPacketSubscriber<GetReceivePacket>(promise) {

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }

                    @Override
                    public void onNext(GetReceivePacket getReceivePacket) {
                        WritableMap writableMap = DataMapper.transform(getReceivePacket);
                        successCallback(promise, writableMap);
                    }
                });
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getCurrentUserInfo(Promise promise) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("displayname", mUserConfig.getDisPlayName());
        writableMap.putString("avatar", mUserConfig.getAvatar());
        successCallback(promise, writableMap);
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

}
