package vn.com.vng.zalopay.mdl.internal;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;

/**
 * Created by huuhoa on 7/21/16.
 * Native module supports sending events from native to react native
 */
public class ReactEventEmitterNativeModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private final EventBus mEventBus;

    public ReactEventEmitterNativeModule(ReactApplicationContext reactContext, EventBus eventBus) {
        super(reactContext);
        mEventBus = eventBus;
    }

    @Override
    public String getName() {
        return "EventsObserver";
    }

    public void sendEvent(String eventName) {
        ReactApplicationContext reactContext = getReactApplicationContext();
        if (reactContext == null) {
            return;
        }

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, null);
    }

    @Override
    public void onHostResume() {
        mEventBus.register(this);
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        mEventBus.unregister(this);
    }

    @Subscribe
    private void onNotificationUpdated(NotificationChangeEvent event) {
        sendEvent("zalopayNotificationsAdded");
    }
}
