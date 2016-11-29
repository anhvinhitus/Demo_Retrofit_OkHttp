package vn.com.vng.zalopay.react.qrcode;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by hieuvm on 11/29/16.
 */

public class ReactReceiveMoneyPackage implements ReactPackage {

    private final User mUser;
    private final EventBus mEventBus;

    public ReactReceiveMoneyPackage(User user, EventBus eventBus) {
        this.mUser = user;
        this.mEventBus = eventBus;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new ReactReceiveMoneyModule(reactContext, mUser,mEventBus));
        return modules;
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
        return Collections.emptyList();
    }
}
