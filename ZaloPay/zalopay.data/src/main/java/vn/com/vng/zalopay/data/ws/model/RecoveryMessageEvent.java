package vn.com.vng.zalopay.data.ws.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AnhHieu on 10/4/16.
 * *
 */

public class RecoveryMessageEvent extends Event {
    public List<NotificationData> listNotify;

    public RecoveryMessageEvent() {
        this.listNotify = new ArrayList<>();
    }

    public void addRecoveryMessage(NotificationData data) {
        listNotify.add(data);
    }
}
