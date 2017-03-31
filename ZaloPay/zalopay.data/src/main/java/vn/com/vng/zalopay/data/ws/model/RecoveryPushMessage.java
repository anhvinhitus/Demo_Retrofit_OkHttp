package vn.com.vng.zalopay.data.ws.model;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.network.PushMessage;

/**
 * Created by AnhHieu on 10/4/16.
 * *
 */

public class RecoveryPushMessage extends PushMessage {
    public List<NotificationData> listNotify;

    public RecoveryPushMessage() {
        this.listNotify = new ArrayList<>();
    }

    public void addRecoveryMessage(NotificationData data) {
        listNotify.add(data);
    }
}
