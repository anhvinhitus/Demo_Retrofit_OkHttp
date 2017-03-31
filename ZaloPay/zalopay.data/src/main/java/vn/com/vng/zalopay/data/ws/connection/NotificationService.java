package vn.com.vng.zalopay.data.ws.connection;

import vn.com.vng.zalopay.network.OnReceivedPushMessageListener;

/**
 * Created by hieuvm on 3/8/17.
 */

public interface NotificationService {
    void start();

    void stop();

    void addReceiverListener(OnReceivedPushMessageListener listener);

    void send(NotificationApiMessage message);

    boolean isConnected();
}
