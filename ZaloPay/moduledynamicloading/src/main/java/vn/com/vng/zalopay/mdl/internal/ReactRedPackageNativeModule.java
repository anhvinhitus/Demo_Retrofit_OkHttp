package vn.com.vng.zalopay.mdl.internal;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

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
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.SubmitOpenPackage;
import vn.com.vng.zalopay.domain.model.redpackage.PackageStatus;
import vn.com.vng.zalopay.mdl.BuildConfig;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.vng.zalopay.mdl.internal.subscriber.GetAllFriendSubscriber;
import vn.com.vng.zalopay.mdl.internal.subscriber.OpenPackageSubscriber;
import vn.com.vng.zalopay.mdl.redpackage.IRedPacketPayListener;
import vn.com.vng.zalopay.mdl.redpackage.IRedPacketPayService;
import vn.com.vng.zalopay.mdl.sweetalertdialog.ISweetAlertDialog;
import vn.com.vng.zalopay.mdl.sweetalertdialog.ISweetAlertDialogListener;

/**
 * Created by longlv on 17/07/2016.
 * define methods that had been called by React Native
 */
public class ReactRedPackageNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private RedPacketStore.Repository mRedPackageRepository;
    private FriendStore.Repository mFriendRepository;
    private final IRedPacketPayService mPaymentService;
    private ISweetAlertDialog sweetAlertDialog;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private CountDownTimer mTimerGetTranStatus;
    private boolean isRunningGetTranStatus;

    public ReactRedPackageNativeModule(ReactApplicationContext reactContext, RedPacketStore.Repository redPackageRepository, FriendStore.Repository friendRepository, IRedPacketPayService payService, ISweetAlertDialog sweetAlertDialog) {
        super(reactContext);
        this.mRedPackageRepository = redPackageRepository;
        this.mFriendRepository = friendRepository;
        this.mPaymentService = payService;
        this.sweetAlertDialog = sweetAlertDialog;
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayRedPacketApi";
    }

    @ReactMethod
    public void createRedPacketBundleOrder(int quantity, double totalLuck, double amountEach, int type, String sendMessage, final Promise promise) {
        Subscription subscription = mRedPackageRepository.createBundleOrder(quantity, (long) totalLuck, (long) amountEach, type, sendMessage)
                .subscribe(new Subscriber<BundleOrder>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on getting CreateBundleOrderSubscriber");
                        if (promise == null) {
                            return;
                        }
                        promise.reject(e.getMessage());
                    }

                    @Override
                    public void onNext(BundleOrder bundleOrder) {
                        Timber.d("onNext bundleOrder [%s]", bundleOrder);
                        if (bundleOrder == null) {
                            promise.reject(String.valueOf(PaymentError.ERR_CODE_INPUT), "bundleOrder null");
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
                if (promise == null) {
                    return;
                }
                promise.reject(String.valueOf(PaymentError.ERR_CODE_INPUT), param);
            }

            @Override
            public void onResponseError(int status) {
                if (promise == null) {
                    return;
                }
                WritableMap item = Arguments.createMap();
                item.putInt("code", status);
                item.putString("message", PaymentError.getErrorMessage(status));
                promise.resolve(item);
            }

            @Override
            public void onResponseSuccess(Bundle bundle) {
                if (promise == null || bundleOrder == null) {
                    return;
                }
                WritableMap item = Arguments.createMap();
                item.putString("bundleid", String.valueOf(bundleOrder.bundleId));
                promise.resolve(item);
            }

            @Override
            public void onResponseTokenInvalid() {

            }

            @Override
            public void onResponseCancel() {
                if (promise == null) {
                    return;
                }
                promise.reject(String.valueOf(PaymentError.ERR_CODE_USER_CANCEL), PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
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
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on getting SubmitToSendSubscriber");
                        if (promise == null) {
                            return;
                        }

                        promise.reject(e);
                    }

                    @Override
                    public void onNext(Boolean result) {
                        Timber.d("onNext result [%s]", result);
                        if (promise == null) {
                            return;
                        }
                        promise.resolve(result);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void startTaskGetTransactionStatus(final long packageId, final long zpTransId, final Promise promise) {
        if (mTimerGetTranStatus == null) mTimerGetTranStatus = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                getTransactionStatus(packageId, zpTransId, promise);
            }

            @Override
            public void onFinish() {
                showDialogRetryGetTranStatus(packageId, zpTransId, promise);
            }
        };
        else {
            mTimerGetTranStatus.cancel();
        }
        mTimerGetTranStatus.start();
    }

    private void showDialogRetryGetTranStatus(final long packageId, final long zpTransId, final Promise promise) {
        Timber.d("showDialogRetryGetTranStatus start");
        if (getCurrentActivity() == null) {
            return;
        }

        ISweetAlertDialogListener onCancelListener = new ISweetAlertDialogListener() {
            @Override
            public void onClick(Dialog dialog) {
                promise.reject(String.valueOf(PaymentError.ERR_CODE_USER_CANCEL), PaymentError.getErrorMessage(PaymentError.ERR_CODE_USER_CANCEL));
                dialog.dismiss();
            }
        };

        ISweetAlertDialogListener onConfirmListener = new ISweetAlertDialogListener() {
            @Override
            public void onClick(Dialog dialog) {
                startTaskGetTransactionStatus(packageId, zpTransId, promise);
                dialog.dismiss();
            }
        };

        sweetAlertDialog.showWarningAlertDialog(getCurrentActivity(), "Giao dịch vẫn còn đang xử lý. Bạn có muốn tiếp ?", "Đóng",
                onCancelListener, "Thử lại", onConfirmListener);
        Timber.d("showDialogRetryGetTranStatus end");
    }

    private void getTransactionStatus(final long packageId, final long zpTransId, final Promise promise) {
        if (isRunningGetTranStatus) {
            return;
        }
        isRunningGetTranStatus = true;
        mRedPackageRepository.getpackagestatus(packageId, zpTransId)
                .subscribe(new Subscriber<PackageStatus>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(PackageStatus packageStatus) {
                        if (promise == null) {
                            return;
                        }
                        WritableMap writableMap = Arguments.createMap();
                        writableMap.putBoolean("isProcessing", packageStatus.isProcessing);
                        writableMap.putDouble("amount", packageStatus.amount);
                        writableMap.putString("zpTransID", packageStatus.zpTransID);
                        writableMap.putString("nextAction", packageStatus.nextAction);
                        writableMap.putString("data", packageStatus.data);
                        writableMap.putDouble("balance", packageStatus.balance);
                        promise.resolve(writableMap);
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
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
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
        final long packageID;
        final long bundleID;
        try {
            packageID = Long.valueOf(strPackageID);
            bundleID = Long.valueOf(strBundleID);
        } catch (NumberFormatException e) {
            Timber.e(e, "openPacket throw NumberFormatException");
            return;
        }
        if (packageID <= 0 || bundleID <= 0) {
            return;
        }
        Subscription subscription = mRedPackageRepository.submitOpenPackage(packageID, bundleID)
                .subscribe(new Subscriber<SubmitOpenPackage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.w(e, "error on openPacket");
                        if (promise == null) {
                            return;
                        }

                        promise.reject(e);
                    }

                    @Override
                    public void onNext(SubmitOpenPackage submitOpenPackage) {
                        Timber.d("openPacket %s", submitOpenPackage);
                        if (promise == null) {
                            return;
                        }
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
                        return transforListFriend(zaloFriendGDs);
                    }
                })
                .subscribe(new GetAllFriendSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    private WritableArray transforListFriend(List<ZaloFriendGD> zaloFriendGDs) {
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

    @ReactMethod
    public void requestStatusWithTransId(String strTransid, String strPackageId, Promise promise) {
        long transid = 0;
        long packageId = 0;
        try {
            transid = Long.valueOf(strTransid);
            packageId = Long.valueOf(strPackageId);
        } catch (NumberFormatException e) {
            Timber.e(e, "requestStatusWithTransId throw NumberFormatException");
        }
        Subscription subscription = mRedPackageRepository.getpackagestatus(packageId, transid)
                .map(new Func1<PackageStatus, WritableMap>() {
                    @Override
                    public WritableMap call(PackageStatus packageStatus) {
                        return transform(packageStatus);
                    }
                })
                .subscribe(new OpenPackageSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    private WritableMap transform(PackageStatus packageStatus) {
        if (packageStatus == null) {
            return null;
        }
        WritableMap item = Arguments.createMap();
        item.putBoolean("isProcessing", packageStatus.isProcessing);
        item.putDouble("amount", packageStatus.amount);
        item.putString("zpTransID", packageStatus.zpTransID);
        item.putString("nextAction", packageStatus.nextAction);
        item.putString("data", packageStatus.data);
        item.putDouble("balance", packageStatus.balance);
        return item;
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
}
