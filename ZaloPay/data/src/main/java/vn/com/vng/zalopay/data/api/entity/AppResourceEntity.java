package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/18/16.
 */
public class AppResourceEntity {

    @SerializedName("appid")
    public long appid;

    @SerializedName("appname")
    public String appname;

    @SerializedName("needdownloadrs")
    public long needdownloadrs;

    @SerializedName("imageurl")
    public String imageurl;

    @SerializedName("jsurl")
    public String jsurl;

    @SerializedName("status")
    public long status;

    @SerializedName("checksum")
    public String checksum;

    @SerializedName("apptype")
    public long apptype;

    @SerializedName("weburl")
    public String weburl;

    @SerializedName("iconname")
    public String iconName;

    @SerializedName("iconcolor")
    public String iconColor;

    public long sortOrder;

    @Expose(deserialize = false, serialize = false)
    public long stateDownload;

    @Expose(deserialize = false, serialize = false)
    public long timeDownload;

    @Expose(deserialize = false, serialize = false)
    public long numRetry;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AppResourceEntity)) {
            return false;
        }

        if (((AppResourceEntity) o).appid != appid) {
            return false;
        }

        if (((AppResourceEntity) o).apptype != apptype) {
            return false;
        }

        return true;
    }
}
