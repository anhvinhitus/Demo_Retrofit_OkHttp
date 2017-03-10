package vn.com.vng.zalopay.data.ws.connection;

import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;

/**
 * Created by hieuvm on 3/8/17.
 */

public interface NotificationService {
    void start();

    void stop();

    void addReceiverListener(OnReceiverMessageListener listener);

    void send(NotificationApiMessage message);

    boolean isConnected();
}
