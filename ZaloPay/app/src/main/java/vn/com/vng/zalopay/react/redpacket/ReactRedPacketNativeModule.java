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
import com.facebook.react.bridge.WritableMap;
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
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.zpc.ZPCStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.MiniApplicationBaseActivity;
import vn.com.vng.zalopay.react.error.PaymentError;

/**
 * Created by longlv on 17/07/2016.
 * define methods that had been called by React Native
 */
public class ReactRedPacketNativeModule extends ReactContextBaseJavaModule
        implements ActivityEventListener, LifecycleEventListener {

    protected final RedPacketStore.Repository mRedPacketRepository;
    private final User mUser;
    private final ZPCStore.Repository mFriendRepository;
    private final BalanceStore.Repository mBalanceRepository;
    private final IRedPacketPayService mPaymentService;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private CountDownTimer mTimerGetStatus;
    private boolean isRunningGetStatus;
    private AlertDialogProvider mDialogProvider;

    public ReactRedPacketNativeModule(ReactApplicationContext reactContext,
                                      RedPacketStore.Repository redPackageRepository,
                                      ZPCStore.Repository friendRepository,
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
                            public void onNext(BundleOrder bundleOrder) {
                                Timber.d("Create bundle order : bundleOrder [%s]", bundleOrder);
                                if (bundleOrder == null) {
                                    Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), "Có lỗi xảy ra trong quá trình xử lý. Vui lòng thử lại sau.");
                                } else {
                                    pay(bundleOrder, promise);
                                }
                            }
                        });
        mCompositeSubscription.add(subscription);
    }

    void pay(final BundleOrder bundleOrder, final Promise promise) {
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

                Subscription subscription = updateBalance()
                        .subscribe(new DefaultSubscriber<>());
                mCompositeSubscription.add(subscription);

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
        });
    }

    void stopTaskGetStatus() {
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

    void onGetBundleStatusFinish(Promise promise, long bundleId, boolean result, int status) {
        Timber.d("Get bundle status finish : bundleId [%s] result [%s] status [%s]", bundleId, result, status);
        promiseResolveGetBundleStatus(promise, result);
        stopTaskGetStatus();
//        mRedPacketRepository.setBundleStatus(bundleId, status).subscribe(new DefaultSubscriber<Void>());
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
        mCompositeSubscription.add(subscription);
    }

    Observable<Boolean> updateBalance() {
        return mBalanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .map(aLong -> Boolean.TRUE)
                .onErrorResumeNext(throwable -> Observable.empty());
    }

    void startTaskGetTransactionStatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("startTaskGetTransactionStatus packetId [%s] transId [%s]", packageId, zpTransId);
        stopTaskGetStatus();
        mTimerGetStatus = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Timber.d("GetTranStatus onTick");
                getPackageStatus(packageId, zpTransId, promise);
            }

            @Override
            public void onFinish() {
                Timber.d("GetTranStatus onFinish");
                showDialogRetryGetTranStatus(packageId, zpTransId, promise);
            }
        }.start();
    }

    void showDialogRetryGetTranStatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("showDialogRetryGetTranStatus packageId [%s] zpTransId [%s]", packageId, zpTransId);
        if (getCurrentActivity() == null) {
            return;
        }

        DialogInterface.OnCancelListener onCancelListener = dialog -> {
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_USER_CANCEL.value(), PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
            dialog.dismiss();
        };

        DialogInterface.OnClickListener onConfirmListener = (dialog, which) -> {
            startTaskGetTransactionStatus(packageId, zpTransId, promise);
            dialog.dismiss();
        };

        mDialogProvider.showWarningAlertDialog(getCurrentActivity(),
                "Giao dịch vẫn còn đang xử lý. Bạn có muốn tiếp tục?",
                "Đóng", "Thử lại",
                onCancelListener, onConfirmListener);
        Timber.d("showDialogRetryGetTranStatus end");
    }

    void getPackageStatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("Get package status : isRunningGetStatus [%s]", isRunningGetStatus);
        if (isRunningGetStatus) {
            return;
        }
        isRunningGetStatus = true;
        Subscription subscription = mRedPacketRepository.getPackageStatus(packageId, zpTransId, "")
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<PackageStatus>() {
                    @Override
                    public void onError(Throwable e) {
                        Timber.d("getpackagestatus onError");
                        isRunningGetStatus = false;
                    }

                    @Override
                    public void onNext(PackageStatus packageStatus) {
                        Timber.d("Get package status success : mTimerGetStatus [%s]", mTimerGetStatus);
                        stopTaskGetStatus();
                        Helpers.promiseResolveSuccess(promise, DataMapper.transform(packageStatus));
                        Timber.d("set open status 1 for packet: %s with amount: [%s]", packageId, packageStatus.amount);
                        onGetPackageStatus(packageId, packageStatus);
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    void onGetPackageStatus(long packageId, PackageStatus packageStatus) {
        Subscription subscription = mRedPacketRepository.setPacketStatus(packageId, packageStatus.amount, RedPacketStatus.Opened.getValue(), null)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);

        Subscription balanceSub = updateBalance()
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(balanceSub);
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
                                    .subscribe(new DefaultSubscriber<>());
                        }
                    }

                    @Override
                    public void onNext(SubmitOpenPackage submitOpenPackage) {
                        Timber.d("openPacket onNext [%s]", submitOpenPackage);
                        startTaskGetTransactionStatus(submitOpenPackage.packageID, submitOpenPackage.zpTransID, promise);
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getAllFriend(Promise promise) {
        Timber.d("getAllFriend promise [%s]", promise);
        Subscription subscription = mFriendRepository.getZaloFriendList()
                .map(DataMapper::transform)

                .subscribe(new GetAllFriendSubscriber(promise, getReactApplicationContext()));
        mCompositeSubscription.add(subscription);
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
            Subscription subscription = mRedPacketRepository.getPacketStatus(packetId)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new RedPacketSubscriber<ReceivePackageGD>(promise) {
                        @Override
                        public void onNext(ReceivePackageGD receivePackage) {
                            Timber.d("received packet: %s", receivePackage.id);
                            promise.resolve(DataMapper.transform(receivePackage));
                        }
                    });
            mCompositeSubscription.add(subscription);
        } catch (Exception ex) {
            promise.reject("EXCEPTION", ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        }
    }

    @ReactMethod
    public void getPacketStatus(final String packetId, final Promise promise) {
        Timber.d("query open status for packet : packetId %s", packetId);
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

        mCompositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getCurrentUserInfo(Promise promise) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("displayname", mUser.displayName);
        writableMap.putString("avatar", mUser.avatar);
        Helpers.promiseResolveSuccess(promise, writableMap);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
        mPaymentService.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Called when a new intent is passed to the activity
     *
     * @param intent income intent
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
                    .subscribe(new DefaultSubscriber<>());
            mCompositeSubscription.add(subscription);
        }
    }

    @Override
    public void onHostPause() {
        Timber.d("onPause");
    }

    @Override
    public void onHostDestroy() {

        unSubscribeIfNotNull(mCompositeSubscription);

        getReactApplicationContext().removeActivityEventListener(this);
        getReactApplicationContext().removeLifecycleEventListener(this);
        Timber.d("onDestroy");
    }

    private void unSubscribeIfNotNull(CompositeSubscription subscription) {
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
        return !TextUtils.isEmpty(moduleName) && moduleName.equals(ModuleName.RED_PACKET);

    }
}
