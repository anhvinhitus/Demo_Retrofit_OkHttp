package vn.com.zalopay.wallet.message;

import org.greenrobot.eventbus.EventBus;

import vn.com.zalopay.wallet.business.behavior.gateway.PlatformInfoLoader;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;

public class PaymentEventBus<T> extends SingletonBase {

    private static PaymentEventBus _object;

    public PaymentEventBus() {
        super();
    }

    public static PaymentEventBus shared() {
        if (PaymentEventBus._object == null) {
            PaymentEventBus._object = new PaymentEventBus();
        }
        return PaymentEventBus._object;
    }

    protected EventBus getBus() {
        return EventBus.getDefault();
    }

    public void register(BasePaymentActivity activity) {
        getBus().register(activity);
        Log.d(this, "registered activity to bus");
    }

    public void register(PlatformInfoLoader gatewayLoader) {
        getBus().register(gatewayLoader);
        Log.d(this, "registered plaforminfo loader to bus");
    }

    public void unregister(BasePaymentActivity activity) {
        getBus().unregister(activity);
        Log.d(this, "unregistered activity out of bus");
    }

    public void unregister(PlatformInfoLoader gatewayLoader) {
        getBus().unregister(gatewayLoader);
        Log.d(this, "unregistered plaforminfo loader out of bus");
    }

    public void post(T eventMessage) {
        getBus().post(eventMessage);
    }

    public void postSticky(T eventMessage) {
        getBus().postSticky(eventMessage);
    }

    public boolean removeStickyEvent(Class<T> clazz) {
        T stickyEvent = getBus().getStickyEvent(clazz);
        // Better check that an event was actually posted before
        if (stickyEvent != null) {
            getBus().removeStickyEvent(stickyEvent);
            return true;
        }
        return false;
    }
}
