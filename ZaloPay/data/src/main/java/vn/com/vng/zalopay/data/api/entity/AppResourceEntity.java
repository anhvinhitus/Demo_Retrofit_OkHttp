package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/18/16.
 *
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

    @SerializedName("iconurl")
    public String iconurl;

    public long sortOrder;

    @Expose(deserialize = false, serialize = false)
    public long stateDownload;

    @Expose(deserialize = false, serialize = false)
    public long timeDownload;

    @Expose(deserialize = false, serialize = false)
    public long numRetry;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppResourceEntity entity = (AppResourceEntity) o;

        if (appid != entity.appid) return false;
        if (needdownloadrs != entity.needdownloadrs) return false;
        if (status != entity.status) return false;
        if (apptype != entity.apptype) return false;
        if (sortOrder != entity.sortOrder) return false;
        if (stateDownload != entity.stateDownload) return false;
        if (timeDownload != entity.timeDownload) return false;
        if (numRetry != entity.numRetry) return false;
        if (appname != null ? !appname.equals(entity.appname) : entity.appname != null)
            return false;
        if (imageurl != null ? !imageurl.equals(entity.imageurl) : entity.imageurl != null)
            return false;
        if (jsurl != null ? !jsurl.equals(entity.jsurl) : entity.jsurl != null) return false;
        if (checksum != null ? !checksum.equals(entity.checksum) : entity.checksum != null)
            return false;
        if (weburl != null ? !weburl.equals(entity.weburl) : entity.weburl != null) return false;
        return iconurl != null ? iconurl.equals(entity.iconurl) : entity.iconurl == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (appid ^ (appid >>> 32));
        result = 31 * result + (appname != null ? appname.hashCode() : 0);
        result = 31 * result + (int) (needdownloadrs ^ (needdownloadrs >>> 32));
        result = 31 * result + (imageurl != null ? imageurl.hashCode() : 0);
        result = 31 * result + (jsurl != null ? jsurl.hashCode() : 0);
        result = 31 * result + (int) (status ^ (status >>> 32));
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (int) (apptype ^ (apptype >>> 32));
        result = 31 * result + (weburl != null ? weburl.hashCode() : 0);
        result = 31 * result + (iconurl != null ? iconurl.hashCode() : 0);
        result = 31 * result + (int) (sortOrder ^ (sortOrder >>> 32));
        result = 31 * result + (int) (stateDownload ^ (stateDownload >>> 32));
        result = 31 * result + (int) (timeDownload ^ (timeDownload >>> 32));
        result = 31 * result + (int) (numRetry ^ (numRetry >>> 32));
        return result;
    }
}
