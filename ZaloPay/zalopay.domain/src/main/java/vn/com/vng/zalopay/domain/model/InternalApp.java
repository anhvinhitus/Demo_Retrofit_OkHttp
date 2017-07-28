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

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("icon_name")
    private String iconName;

    @SerializedName("icon_color")
    private String iconColor;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

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
