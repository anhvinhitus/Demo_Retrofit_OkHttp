package vn.com.zalopay.wallet.eventmessage;

import org.greenrobot.eventbus.EventBus;

import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
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
    }

    public void unregister(BasePaymentActivity activity) {
        getBus().unregister(activity);
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
