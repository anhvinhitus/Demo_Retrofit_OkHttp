package vn.com.vng.zalopay.notification;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.data.ws.model.NotificationEmbedData;

/**
 * Created by longlv on 11/16/16.
 * Model receive from Gcm
 */

class EmbedDataGcm {
    @SerializedName("mtuid")
    long mtuid;

    @SerializedName("mtaid")
    long mtaid;

    @SerializedName("data")
    NotificationEmbedData embeddata;
}
