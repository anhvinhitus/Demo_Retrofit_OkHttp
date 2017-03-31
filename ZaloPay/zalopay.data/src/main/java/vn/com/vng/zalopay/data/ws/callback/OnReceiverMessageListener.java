package vn.com.vng.zalopay.data.ws.callback;

import vn.com.vng.zalopay.network.PushMessage;

/**
 * Created by AnhHieu on 6/15/16.
 */
public interface OnReceiverMessageListener {
    void onReceiverEvent(PushMessage pushMessage);

    void onError(Throwable t);
}
