package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by datnt10 on 7/28/17.
 */

public class InternalApp {
    @SerializedName("appId")
    public long appId;

    @SerializedName("order")
    public int position;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("icon_name")
    public String iconName;

    @SerializedName("icon_color")
    public String iconColor;

    public InternalApp() {

    }

    public InternalApp(long appId, int position, String displayName, String iconName, String iconColor) {
        this.appId = appId;
        this.position = position;
        this.displayName = displayName;
        this.iconName = iconName;
        this.iconColor = iconColor;
    }
}
