package cl.json;

import android.content.ActivityNotFoundException;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;

import cl.json.social.GenericShare;
import cl.json.social.ShareIntent;
import cl.json.social.ZaloShare;

final class RNShareModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private HashMap<String, ShareIntent> sharesExtra = new HashMap<>();

    RNShareModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        sharesExtra.put("generic", new GenericShare(this.reactContext));
        sharesExtra.put("zalo", new ZaloShare(this.reactContext));
    }

    @Override
    public String getName() {
        return "RNShare";
    }

    @ReactMethod
    public void open(ReadableMap options, @Nullable Callback failureCallback, @Nullable Callback successCallback) {
        try {
            GenericShare share = new GenericShare(this.reactContext);
            share.open(options);
            successCallback.invoke("OK");
        } catch (ActivityNotFoundException ex) {
            System.out.println("ERROR");
            System.out.println(ex.getMessage());
            failureCallback.invoke("not_available");
        }
    }

    @ReactMethod
    public void shareSingle(ReadableMap options, Callback callback) {
        if (ShareIntent.hasValidKey("social", options)) {
            try {
                this.sharesExtra.get(options.getString("social")).open(options);
                callback.invoke(1);
            } catch (ActivityNotFoundException ex) {
                callback.invoke(0);
            }
        } else {
            callback.invoke(0);
        }
    }
}
