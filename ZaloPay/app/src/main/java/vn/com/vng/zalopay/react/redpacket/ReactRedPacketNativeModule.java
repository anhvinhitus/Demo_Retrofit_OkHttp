package vn.com.vng.zalopay.react.redpacket;

import android.app.Activity;
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
import com.zalopay.apploader.MiniApplicationBaseActivity;
import com.zalopay.apploader.ReactBasedActivity;
import com.zalopay.apploader.internal.ModuleName;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by longlv on 17/07/2016.
 * define methods that had been called by React Native
 */
public class ReactRedPacketNativeModule extends ReactContextBaseJavaModule
        implements ActivityEventListener, LifecycleEventListener {

    private User mUser;
    private RedPacketStore.Repository mRedPacketRepository;
    private FriendStore.Repository mFriendRepository;
    private BalanceStore.Repository mBalanceRepository;
    private final IRedPacketPayService mPaymentService;
    private AlertDialogProvider mDialogProvider;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private CountDownTimer mTimerGetStatus;
    private boolean isRunningGetStatus;

    public ReactRedPacketNativeModule(ReactApplicationContext reactContext,
                                      RedPacketStore.Repository redPackageRepository,
                                      FriendStore.Repository friendRepository,
                                      BalanceStore.Repository balanceRepository,
                                      IRedPacketPayService payService,
                                      User user,
                                      AlertDialogProvider sweetAlertDialog) {
        super(reactContext);
        this.mRedPacketRepository = redPackageRepository;
        this.mFriendRepository = friendRepository;
        this.mBalanceRepository = balanceRepository;
        this.mPaymentService = payService;
        this.mDialogProvider = sweetAlertDialog;
        this.mUser = user;
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
                mRedPacketRepository.createBundleOrder(quantity, (long) totalLuck, (long) amountEach, type, sendMessage)
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
                                    Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), "Có lỗi xảy ra trong quá trình xử lý. Vui lòng thử lại sau.");
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
                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                    Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INTERNET.value(),
                            PaymentError.getErrorMessage(PaymentError.ERR_CODE_INTERNET));
                } else if (paymentError == PaymentError.ERR_CODE_MONEY_NOT_ENOUGH
                        || paymentError == PaymentError.ERR_CODE_UPGRADE_PROFILE_LEVEL
                        || paymentError == PaymentError.ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT_BEFORE_PAYMENT) {
                    //not return error to react
                } else {
                    //PaymentSDK đã hiển thị lỗi -> ko care lỗi này nữa
                    Helpers.promiseResolveError(promise, paymentError.value(), null);
                }
            }

            @Override
            public void onResponseSuccess(Bundle bundle) {
                Timber.d("pay onResponseSuccess bundleid [%s]", bundleOrder.bundleId);
                WritableMap data = Arguments.createMap();
                data.putString("bundleid", String.valueOf(bundleOrder.bundleId));


                if (AndroidUtils.isMainThead()) {
                    Timber.d("Kiểm tra lại phần này thôi. main thread rùi.");
                }

                Subscription subscription = updateBalance()
                        .subscribe(new DefaultSubscriber<Boolean>());
                compositeSubscription.add(subscription);

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
        mRedPacketRepository.setBundleStatus(bundleId, status).subscribe(new DefaultSubscriber<Void>());
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
        Subscription subscription = mFriendRepository.getListUserZaloPay(friendList)
                .flatMap(new Func1<List<RedPacketUserEntity>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(List<RedPacketUserEntity> entities) {
                        Timber.d("User red package [%s]", entities.size());
                        return mRedPacketRepository.sendBundle(bundleID, entities);
                    }
                }).flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean aBoolean) {
                        return updateBalance();
                    }
                })
                .subscribe(new RedPacketSubscriber<Boolean>(promise) {
                    @Override
                    public void onNext(Boolean result) {
                        Timber.d("SubmitToSendSubscriber onNext result [%s]", result);
                        onGetBundleStatusFinish(promise, bundleID, true, 1);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private Observable<Boolean> updateBalance() {
        return mBalanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .map(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long aLong) {
                        return Boolean.TRUE;
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Boolean>>() {
                    @Override
                    public Observable<? extends Boolean> call(Throwable throwable) {
                        return Observable.empty();
                    }
                });
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
        Subscription subscription = mRedPacketRepository.getpackagestatus(packageId, zpTransId, "")
                .subscribeOn(Schedulers.io())
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
                        onGetPackageStatus(packageId, packageStatus);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void onGetPackageStatus(long packageId, PackageStatus packageStatus) {
        Subscription subscription = mRedPacketRepository.setPacketStatus(packageId, packageStatus.amount, RedPacketStatus.Opened.getValue(), null)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Void>());
        compositeSubscription.add(subscription);

        Subscription balanceSub = updateBalance()
                .subscribe(new DefaultSubscriber<Boolean>());
        compositeSubscription.add(balanceSub);
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
        Subscription subscription = mRedPacketRepository.submitOpenPackage(packageID, bundleID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RedPacketSubscriber<SubmitOpenPackage>(promise) {

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on openPacket");
                        super.onError(e);
                        if (e instanceof BodyException) {
                            int errorCode = ((BodyException) e).errorCode;
                            String errorMsg = ((BodyException) e).message;
                            mRedPacketRepository.setPacketStatus(packageID, 0, errorCode, errorMsg)
                                    .subscribe(new DefaultSubscriber<Void>());
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
        Subscription subscription = mFriendRepository.getZaloFriendList()
                .map(new Func1<List<ZaloFriend>, WritableArray>() {
                    @Override
                    public WritableArray call(List<ZaloFriend> friends) {
                        return DataMapper.transform(friends);
                    }
                })

                .subscribe(new GetAllFriendSubscriber(promise, getReactApplicationContext()));
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
            Subscription subscription = mRedPacketRepository.getReceivedPacket(packetIdValue)
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
            Subscription subscription = mRedPacketRepository.getPacketsInBundle(bundleId)
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
                            Helpers.promiseResolveSuccess(promise, array);
                        }

                    });
            compositeSubscription.add(subscription);
        } catch (Exception e) {
            Timber.w(e, "Exception while fetching packets");
            promise.reject("EXCEPTION", e.getMessage());
        }
    }

    @ReactMethod
    public void getPacketStatus(final String packetId, final Promise promise) {
        Timber.d("query open status for packet: %s", packetId);
        Subscription subscription = mRedPacketRepository.getPacketStatus(packetId)
                .subscribe(new RedPacketSubscriber<ReceivePackageGD>(promise) {
                    @Override
                    public void onNext(ReceivePackageGD receivePackageGD) {
                        WritableMap writableMap = Arguments.createMap();
                        if (receivePackageGD != null) {
                            writableMap.putDouble("code", receivePackageGD.status);
                            writableMap.putString("message", receivePackageGD.messageStatus);
                            Timber.d("open status [%s][%s] for packet: %s", receivePackageGD.status,
                                    receivePackageGD.messageStatus, packetId);
                        } else {
                            writableMap.putDouble("code", RedPacketStatus.CanOpen.getValue());
                            writableMap.putString("message", "");
                        }

                        promise.resolve(writableMap);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getSendBundleHistoryWithTimeStamp(final double createTime, final double count, final Promise promise) {
        Timber.d("getSendBundleHistoryWithTimeStamp createTime [%s] count [%s]", createTime, count);
        Subscription subscription = mRedPacketRepository.getSentBundleList((long) createTime, (int) count)
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
        Subscription subscription = mRedPacketRepository.getReceivePacketList((long) createTime, (int) count)
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
        writableMap.putString("displayname", mUser.displayName);
        writableMap.putString("avatar", mUser.avatar);
        Helpers.promiseResolveSuccess(promise, writableMap);
    }

    @ReactMethod
    public void getAppInfo(final Promise promise) {
        Subscription subscription = mRedPacketRepository.getRedPacketAppInfo()
                .subscribeOn(Schedulers.io())
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
        mPaymentService.onActivityResult(requestCode, resultCode, data);
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

        if (isRedPacketComponent()) {
            Timber.d("check list zaloid for client");
            Subscription subscription = mFriendRepository.checkListZaloIdForClient()
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<Boolean>());
            compositeSubscription.add(subscription);
        }
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

    private boolean isRedPacketComponent() {
        Activity activity = getCurrentActivity();

        if (!(activity instanceof MiniApplicationBaseActivity)) {
            return false;
        }

        String moduleName = ((MiniApplicationBaseActivity) activity).getMainComponentName();
        if (TextUtils.isEmpty(moduleName)) {
            return false;
        }

        return moduleName.equals(ModuleName.RED_PACKET);
    }
}
