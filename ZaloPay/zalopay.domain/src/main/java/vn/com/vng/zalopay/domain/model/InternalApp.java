package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by datnt10 on 7/28/17.
 */

public class InternalApp {
    @SerializedName("appId")
    private long appId;

    @SerializedName("order")
    private int position;

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public InternalApp() {

    }

    public InternalApp(long appId, int position) {
        this.appId = appId;
        this.position = position;
    }
}
