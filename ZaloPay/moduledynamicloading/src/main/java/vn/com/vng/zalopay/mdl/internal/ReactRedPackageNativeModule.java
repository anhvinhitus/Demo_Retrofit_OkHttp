package vn.com.vng.zalopay.mdl.internal;

import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.redpacket.RedPackageStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.SubmitOpenPackage;
import vn.com.vng.zalopay.mdl.internal.subscriber.CreateBundleOrderSubscriber;
import vn.com.vng.zalopay.mdl.internal.subscriber.GetAllFriendSubscriber;
import vn.com.vng.zalopay.mdl.internal.subscriber.OpenPackageSubscriber;
import vn.com.vng.zalopay.mdl.internal.subscriber.SubmitToSendSubscriber;

/**
 * Created by longlv on 17/07/2016.
 * define methods that had been called by React Native
 */
public class ReactRedPackageNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private RedPackageStore.Repository mRedPackageRepository;
    private FriendStore.Repository mFriendRepository;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ReactRedPackageNativeModule(ReactApplicationContext reactContext, RedPackageStore.Repository redPackageRepository, FriendStore.Repository friendRepository) {
        super(reactContext);
        this.mRedPackageRepository = redPackageRepository;
        this.mFriendRepository = friendRepository;
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @ReactMethod
    public void createRedPacketBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage, Promise promise) {
        Subscription subscription = mRedPackageRepository.createBundleOrder(quantity, totalLuck, amountEach, type, sendMessage)
                .map(new Func1<BundleOrder, WritableMap>() {
                    @Override
                    public WritableMap call(BundleOrder bundleOrder) {
                        return transform(bundleOrder);
                    }
                })
                .subscribe(new CreateBundleOrderSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void submitToSendBundle(long bundleID, List<Long> friendList, Promise promise) {
        Subscription subscription = mRedPackageRepository.sendBundle(bundleID, friendList)
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean;
                    }
                })
                .subscribe(new SubmitToSendSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void openPacket(long packageID, long bundleID, Promise promise) {
        Subscription subscription = mRedPackageRepository.submitOpenPackage(packageID, bundleID)
                .map(new Func1<SubmitOpenPackage, WritableMap>() {
                    @Override
                    public WritableMap call(SubmitOpenPackage submitOpenPackage) {
                        return transform(submitOpenPackage);
                    }
                })
                .subscribe(new OpenPackageSubscriber(promise));
        compositeSubscription.add(subscription);
    }


    private WritableMap transform(SubmitOpenPackage submitOpenPackage) {
        if (submitOpenPackage == null) {
            return null;
        }
        WritableMap item = Arguments.createMap();
        item.putDouble("bundleID", submitOpenPackage.bundleID);
        item.putDouble("packageID", submitOpenPackage.packageID);
        item.putString("zpTransID", submitOpenPackage.zpTransID);
        return item;
    }

    @ReactMethod
    public void getAllFriend(Promise promise) {
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
            friendItem.putDouble("userId", zaloFriendGD.getId());
            friendItem.putInt("userGender", zaloFriendGD.getUserGender());
            friendItem.putBoolean("usingApp", zaloFriendGD.getUsingApp());
            friendItem.putString("avatar", zaloFriendGD.getAvatar());
            listFriends.pushMap(friendItem);
        }
        return listFriends;
    }

    @ReactMethod
    public void requestStatusWithTransId(long transid, long packageId, Promise promise) {

    }

    private WritableMap transform(BundleOrder bundleOrder) {
        if (bundleOrder == null) {
            return null;
        }
        WritableMap item = Arguments.createMap();
        item.putDouble("bundleId", bundleOrder.bundleId);
        item.putString("zptranstoken", bundleOrder.getZptranstoken());
        item.putString("apptransid", bundleOrder.getApptransid());
        item.putString("appuser", bundleOrder.getAppuser());
        item.putDouble("apptime", bundleOrder.apptime);
        item.putString("embeddata", bundleOrder.embeddata);
        item.putString("item", bundleOrder.getItem());
        item.putDouble("amount", bundleOrder.getAmount());
        item.putString("description", bundleOrder.getDescription());
        item.putString("payoption", bundleOrder.getPayoption());
        item.putString("mac", bundleOrder.getMac());
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
