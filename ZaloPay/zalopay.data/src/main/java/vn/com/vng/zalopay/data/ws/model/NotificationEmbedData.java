package vn.com.vng.zalopay.data.ws.model;

import com.google.gson.JsonObject;

/**
 * Created by huuhoa on 8/29/16.
 * Notification embed data
 */

public class NotificationEmbedData {
    public final JsonObject object;

    public NotificationEmbedData(JsonObject object) {
        this.object = object;
    }
}
