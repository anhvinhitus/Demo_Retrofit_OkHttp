package vn.com.vng.zalopay.mdl.internal;

import android.content.Intent;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import timber.log.Timber;
import vn.com.vng.zalopay.mdl.INavigator;

/**
 * Created by huuhoa on 4/25/16.
 */
public class ReactInternalNativeModule extends ReactContextBaseJavaModule {

    INavigator navigator;

    public ReactInternalNativeModule(ReactApplicationContext reactContext, INavigator navigator) {
        super(reactContext);
        this.navigator = navigator;
    }

    /// The purpose of this method is to return the string name of the NativeModule
    /// which represents this class in JavaScript. So here we will call this ZaloPayInternal
    /// so that we can access it through React.NativeModules.ZaloPayInternal in JavaScript.
    @Override
    public String getName() {
        return "ZaloPayApi";
    }

    /// To expose a method to JavaScript a Java method must be annotated using @ReactMethod.
    /// The return type of bridge methods is always void.
    /// React Native bridge is asynchronous, so the only way to pass a result to JavaScript is
    /// by using callbacks or emitting events.
    @ReactMethod
    public void show(String message, int duration) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /// Request ZaloPayInternal API
    @ReactMethod
    public void request(String methodName, ReadableMap parameters, Promise promise) {
        WritableMap result = Arguments.createMap();
        result.merge(parameters);
        result.putString("method", methodName);
        promise.resolve(result);
    }

    @ReactMethod
    public void closeModule() {
        Timber.d("close Module");
        getCurrentActivity().finish();
    }

    @ReactMethod
    public void navigateLinkCard() {
        Timber.d("navigateLinkCard");
        Intent intent = navigator.intentLinkCard(getCurrentActivity());
        getCurrentActivity().startActivity(intent);
    }

    @ReactMethod
    public void navigateProfile() {
        Timber.d("navigateProfile");
        Intent intent = navigator.intentProfile(getCurrentActivity());
        getCurrentActivity().startActivity(intent);
    }
}

