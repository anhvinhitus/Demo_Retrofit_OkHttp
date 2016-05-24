package vn.com.vng.zalopay.mdl.internal;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.mdl.ZaloPayIAPNativeModule;

/**
 * Created by AnhHieu on 5/16/16.
 */
public class ReactIAPPackage implements ReactPackage {
    final ZaloPayIAPRepository zaloPayIAPRepository;
    final User user;
    private final long appId;

    public ReactIAPPackage(ZaloPayIAPRepository zaloPayIAPRepository,
                           User user, long appId) {
        this.zaloPayIAPRepository = zaloPayIAPRepository;
        this.user = user;
        this.appId = appId;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new ZaloPayIAPNativeModule(reactContext, zaloPayIAPRepository, user, appId));
        return modules;
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}
