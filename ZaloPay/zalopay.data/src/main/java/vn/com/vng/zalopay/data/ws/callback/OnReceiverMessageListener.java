package vn.com.vng.zalopay.data.ws.callback;

import vn.com.vng.zalopay.data.ws.model.Event;

/**
 * Created by AnhHieu on 6/15/16.
 */
public interface OnReceiverMessageListener {
    void onReceiverEvent(Event event);

    void onError(Throwable t);
}
