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

import java.lang.ref.WeakReference;
import java.util.List;

import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by huuhoa on 6/10/16.
 * Notification for react native
 */
public class ReactNotificationNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private ZaloPayRepository repository;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ReactNotificationNativeModule(ReactApplicationContext reactContext, ZaloPayRepository repository) {
        super(reactContext);
        this.repository = repository;
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayNotification";
    }

    @ReactMethod
    public void getNotification(int pageIndex, int count, Promise promise) {

        Timber.d("get transaction index %s count %s", pageIndex, count);

        WritableArray result = Arguments.createArray();
        for (int i = 0;i < 20; i ++) {
            WritableMap item = Arguments.createMap();
            item.putBoolean("read", (i % 2 == 0));
            item.putString("title", "Mua thẻ điện thoại");
            item.putString("desc", "Bạn đã mua thẻ Mobifone thành công mệnh giá 50.000 VND");
            item.putInt("time", (int)System.currentTimeMillis() / 1000);
            item.putInt("type", i % 4 + 1);

            result.pushMap(item);
        }

        promise.resolve(result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
    }

    @Override
    public void onHostResume() {
        Timber.d(" Actvity `onResume`");
    }

    @Override
    public void onHostPause() {
        Timber.d(" Actvity `onPause`");
    }

    @Override
    public void onHostDestroy() {

        unsubscribeIfNotNull(compositeSubscription);

        getReactApplicationContext().removeActivityEventListener(this);
        getReactApplicationContext().removeActivityEventListener(this);
        Timber.d("Actvity `onDestroy");
    }

    public void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

}
