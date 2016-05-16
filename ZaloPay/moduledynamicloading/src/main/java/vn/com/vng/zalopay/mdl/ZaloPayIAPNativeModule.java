package vn.com.vng.zalopay.mdl;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import dagger.Module;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by huuhoa on 5/16/16.
 */


public class ZaloPayIAPNativeModule extends ReactContextBaseJavaModule {


    ZaloPayRepository zaloPayRepository;
    User user;

    public ZaloPayIAPNativeModule(ReactApplicationContext reactContext, ZaloPayRepository zaloPayRepository, User user) {
        super(reactContext);
        this.zaloPayRepository = zaloPayRepository;
        this.user = user;

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
}
