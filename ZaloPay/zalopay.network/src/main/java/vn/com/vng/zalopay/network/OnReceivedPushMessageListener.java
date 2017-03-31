package vn.com.vng.zalopay.network;

import vn.com.vng.zalopay.network.PushMessage;

/**
 * Created by AnhHieu on 6/15/16.
 */
public interface OnReceivedPushMessageListener {
    void onReceivedPushMessage(PushMessage pushMessage);

    void onError(Throwable t);
}
