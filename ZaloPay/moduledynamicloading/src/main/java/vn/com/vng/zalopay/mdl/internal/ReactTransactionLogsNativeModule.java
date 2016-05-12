package vn.com.vng.zalopay.mdl.internal;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

/**
 * Created by huuhoa on 5/8/16.
 */
public class ReactTransactionLogsNativeModule extends ReactContextBaseJavaModule {

    public ReactTransactionLogsNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ZaloPayTransactionLogs";
    }

    /// Request ZaloPayIAP API
    @ReactMethod
    public void getTransactions(int pageIndex, int count, Promise promise) {
        WritableArray result = Arguments.createArray();
        for (int i = 0; i < count; i++) {
            WritableMap item = Arguments.createMap();
            item.putInt("transid", pageIndex * count + i);
            item.putDouble("reqdate", Math.random() % 10000000 + 1000000 + 1460366347);
            item.putString("description", Math.floor(Math.random() % 2) == 0 ? "Thanh toán mua thẻ điện thoại Vinaphone" : "Nhận chuyển tiền từ Nguyễn Văn Nam");
            item.putInt("amount", (int) Math.floor(Math.random() + 100) * 100);
            item.putInt("type", (int) Math.floor(Math.random() % 2));

            result.pushMap(item);
        }

        promise.resolve(result);
    }

    @ReactMethod
    public void reloadListTransaction(int count, Promise promise) {
        WritableArray result = Arguments.createArray();
        for (int i = 0; i < count; i++) {
            WritableMap item = Arguments.createMap();
            item.putInt("transid", i);
            item.putDouble("reqdate", Math.random() % 10000000 + 1000000 + 1460366347);
            item.putString("description", Math.floor(Math.random() % 2) == 0 ? "Thanh toán mua thẻ điện thoại Vinaphone" : "Nhận chuyển tiền từ Nguyễn Văn Nam");
            item.putInt("amount", (int) Math.floor(Math.random() + 100) * 100);
            item.putInt("type", (int) Math.floor(Math.random() % 2));

            result.pushMap(item);
        }

        promise.resolve(result);
    }
}
