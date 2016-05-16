package vn.com.vng.zalopay.mdl;

import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import dagger.Module;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by huuhoa on 5/16/16.
 */


public class ZaloPayIAPNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {


    ZaloPayRepository zaloPayRepository;
    User user;

    public ZaloPayIAPNativeModule(ReactApplicationContext reactContext, ZaloPayRepository zaloPayRepository, User user) {
        super(reactContext);
        this.zaloPayRepository = zaloPayRepository;
        this.user = user;

        getReactApplicationContext().addActivityEventListener(this);
        getReactApplicationContext().addLifecycleEventListener(this);

    }

    @Override
    public String getName() {
        return "ZaloPayIAP";
    }

    /**
     * Tham khảo tài liệu: https://docs.google.com/a/vng.com.vn/document/d/1dYKPBXLF9JRwExXkc5XlQJiQKRxp19Gf8x8cbXvGSvA/edit?usp=sharing
     *
     * @param params  Chứa danh sách các thuộc tính cần thiết để gọi hàm thanh toán của SDK
     * @param promise Trả về kết quả thanh toán
     */
    @ReactMethod
    public void payOrder(ReadableMap params, Promise promise) {
        // verify params parameters

        // call payment SDK

        // return result
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
        Timber.d("Actvity `onDestroy");
    }
}
