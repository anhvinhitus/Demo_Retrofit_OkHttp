package vn.com.vng.zalopay.react.redpacket;

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
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;

/**
 * Created by longlv on 17/07/2016.
 * define methods that had been called by React Native
 */
public class RedPacketNativeModule extends ReactContextBaseJavaModule
        implements ActivityEventListener, LifecycleEventListener {

    private UserConfig mUserConfig;
    private RedPacketStore.Repository mRedPackageRepository;
    private FriendStore.Repository mFriendRepository;
    private BalanceStore.Repository mBalanceRepository;
    private final IRedPacketPayService mPaymentService;
    private AlertDialogProvider mDialogProvider;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private CountDownTimer mTimerGetStatus;
    private boolean isRunningGetStatus;

    public RedPacketNativeModule(ReactApplicationContext reactContext,
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
                                    Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), "bundleOrder null");
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
        mPaymentService.pay(getCurrentActivity(), bundleOrder, new RedPacketPayListener() {
            @Override
            public void onParameterError(String param) {
                Timber.w("pay onParameterError");
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), param);
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                Timber.d("pay onResponseError status [%s]", paymentError.value());
                if (getCurrentActivity() != null && !NetworkHelper.isNetworkAvailable(getCurrentActivity())) {
                    Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INTERNET.value(),
                            PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
                } else if (paymentError == PaymentError.ERR_CODE_FAIL) {
                    //PaymentSDK đã hiển thị lỗi -> ko care lỗi này nữa
                    Helpers.promiseResolveError(promise, paymentError.value(), null);
                } else if (paymentError == PaymentError.ERR_CODE_USER_CANCEL) {
                    //User ấn back -> ko care lỗi này nữa
                    Helpers.promiseResolveError(promise, paymentError.value(), null);
                } else {
                    Helpers.promiseResolveError(promise, paymentError.value(), PaymentError.getErrorMessage(paymentError));
                }
            }

            @Override
            public void onResponseSuccess(Bundle bundle) {
                Timber.d("pay onResponseSuccess bundle [%s]", bundle);
                WritableMap data = Arguments.createMap();
                data.putString("bundleid", String.valueOf(bundleOrder.bundleId));
                mBalanceRepository.updateBalance();
                Helpers.promiseResolveSuccess(promise, data);
            }

            @Override
            public void onResponseTokenInvalid() {
                Timber.d("pay onResponseTokenInvalid");
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_TOKEN_INVALID.value(), null);
            }

            @Override
            public void onAppError(String msg) {
                Timber.d("pay onAppError [%s]", msg);
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_SYSTEM.value(),
                        PaymentError.getErrorMessage(PaymentError.ERR_CODE_SYSTEM));
            }

            @Override
            public void onNotEnoughMoney() {
                Timber.d("pay onNotEnoughMoney");
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_MONEY_NOT_ENOUGH.value(), null);
            }
        });
    }

    private void stopTaskGetStatus() {
        if (mTimerGetStatus != null) {
            mTimerGetStatus.cancel();
        }
        isRunningGetStatus = false;
    }

    private void promiseResolveGetBundleStatus(Promise promise, boolean result) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putBoolean("result", result);
        Helpers.promiseResolveSuccess(promise, writableMap);
    }

    private void onGetBundleStatusFinish(Promise promise, long bundleId, boolean result, int status) {
        Timber.d("onGetBundleStatusFinish bundleId [%s] result [%s] status [%s]", bundleId, result, status);
        promiseResolveGetBundleStatus(promise, result);
        stopTaskGetStatus();
        Timber.d("onGetBundleStatusFinish, set status: [%s] for bundle: [%s] result [%s]", status, bundleId, result);
        mRedPackageRepository.setBundleStatus(bundleId, status).subscribe(new DefaultSubscriber<Void>());
    }

    @ReactMethod
    public void submitToSendBundle(String strBundleID, ReadableArray friends, final Promise promise) {
        final long bundleID;
        try {
            bundleID = Long.valueOf(strBundleID);
        } catch (NumberFormatException e) {
            Timber.e(e, "submitToSendBundle throw NumberFormatException");
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), "Invalid bundleId");
            return;
        }
        if (bundleID <= 0) {
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), "Invalid bundleId");
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
                        mBalanceRepository.updateBalance();
                        onGetBundleStatusFinish(promise, bundleID, true, 1);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void startTaskGetTransactionStatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("startTaskGetTransactionStatus packetId [%s] transId [%s]", packageId, zpTransId);
        stopTaskGetStatus();
        mTimerGetStatus = new CountDownTimer(30000, 1000) {
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
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_USER_CANCEL.value(), PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
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
        Timber.d("getpackagestatus isRunningGetStatus [%s]", isRunningGetStatus);
        if (isRunningGetStatus) {
            return;
        }
        isRunningGetStatus = true;
        mRedPackageRepository.getpackagestatus(packageId, zpTransId, "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<PackageStatus>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("getpackagestatus onError");
                        isRunningGetStatus = false;
                    }

                    @Override
                    public void onNext(PackageStatus packageStatus) {
                        Timber.d("getpackagestatus onNext, mTimerGetStatus [%s]", mTimerGetStatus);
                        stopTaskGetStatus();
                        Helpers.promiseResolveSuccess(promise, DataMapper.transform(packageStatus));
                        Timber.d("set open status 1 for packet: %s with amount: [%s]", packageId, packageStatus.amount);
                        mRedPackageRepository.setPacketStatus(packageId, packageStatus.amount, RedPacketStatus.Opened.getValue()).subscribe(new DefaultSubscriber<Void>());
                        mBalanceRepository.updateBalance();
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
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
            return;
        }
        Timber.d("openPacket after cast packageID [%s] bundleID [%s]", packageID, bundleID);
        if (packageID <= 0 || bundleID <= 0) {
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
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
                        if (e instanceof BodyException) {
                            mRedPackageRepository.setPacketStatus(packageID, 0, RedPacketStatus.Invalid.getValue()).subscribe(new DefaultSubscriber<Void>());
                        }
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
    public void getPacketStatus(final String packetId, final Promise promise) {
        Timber.d("query open status for packet: %s", packetId);
        Subscription subscription = mRedPackageRepository.getPacketStatus(packetId)
                .subscribe(new RedPacketSubscriber<Integer>(promise) {
                    @Override
                    public void onNext(Integer status) {
                        WritableMap writableMap = Arguments.createMap();
                        writableMap.putInt("code", status);
                        Timber.d("open status [%s] for packet: %s", status, packetId);
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
                    public void onNext(GetSentBundle getSentBundle) {
                        Timber.d("getSendBundleHistoryWithTimeStamp onNext getSentBundle [%s]", getSentBundle);
                        WritableMap writableMap = DataMapper.transform(getSentBundle);
                        Helpers.promiseResolveSuccess(promise, writableMap);
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
                    public void onNext(GetReceivePacket getReceivePacket) {
                        Helpers.promiseResolveSuccess(promise, DataMapper.transform(getReceivePacket));
                    }
                });
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getCurrentUserInfo(Promise promise) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("displayname", mUserConfig.getDisPlayName());
        writableMap.putString("avatar", mUserConfig.getAvatar());
        Helpers.promiseResolveSuccess(promise, writableMap);
    }

    @ReactMethod
    public void getAppInfo(final Promise promise) {
        Subscription subscription = mRedPackageRepository.getRedPacketAppInfo()
                .subscribe(new RedPacketSubscriber<RedPacketAppInfo>(promise) {
                    @Override
                    public void onNext(RedPacketAppInfo redPacketAppInfo) {
                        Helpers.promiseResolveSuccess(promise, DataMapper.transform(redPacketAppInfo));
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
    }

    /**
     * Called when a new intent is passed to the activity
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        Timber.d("onNewIntent called from based");
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
}
