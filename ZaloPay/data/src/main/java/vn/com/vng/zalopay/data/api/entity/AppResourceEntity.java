package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/18/16.
 *
 */
public class AppResourceEntity {

    @SerializedName("appid")
    public int appid;

    @SerializedName("appname")
    public String appname;

    @SerializedName("needdownloadrs")
    public int needdownloadrs;

    @SerializedName("imageurl")
    public String imageurl;

    @SerializedName("jsurl")
    public String jsurl;

    @SerializedName("status")
    public int status;

    @SerializedName("checksum")
    public String checksum;

    @SerializedName("apptype")
    public String apptype;

    @SerializedName("weburl")
    public String weburl;

    @SerializedName("iconurl")
    public String iconurl;

    @Expose(deserialize = false, serialize = false)
    public int stateDownload;

    @Expose(deserialize = false, serialize = false)
    public long timeDownload;

    @Expose(deserialize = false, serialize = false)
    public int numRetry;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppResourceEntity that = (AppResourceEntity) o;

        if (appid != that.appid) return false;
        if (needdownloadrs != that.needdownloadrs) return false;
        if (status != that.status) return false;
        if (stateDownload != that.stateDownload) return false;
        if (timeDownload != that.timeDownload) return false;
        if (numRetry != that.numRetry) return false;
        if (appname != null ? !appname.equals(that.appname) : that.appname != null) return false;
        if (imageurl != null ? !imageurl.equals(that.imageurl) : that.imageurl != null)
            return false;
        if (jsurl != null ? !jsurl.equals(that.jsurl) : that.jsurl != null) return false;
        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null)
            return false;
        if (apptype != null ? !apptype.equals(that.apptype) : that.apptype != null) return false;
        if (weburl != null ? !weburl.equals(that.weburl) : that.weburl != null) return false;
        return iconurl != null ? iconurl.equals(that.iconurl) : that.iconurl == null;

    }

    @Override
    public int hashCode() {
        int result = appid;
        result = 31 * result + (appname != null ? appname.hashCode() : 0);
        result = 31 * result + needdownloadrs;
        result = 31 * result + (imageurl != null ? imageurl.hashCode() : 0);
        result = 31 * result + (jsurl != null ? jsurl.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (apptype != null ? apptype.hashCode() : 0);
        result = 31 * result + (weburl != null ? weburl.hashCode() : 0);
        result = 31 * result + (iconurl != null ? iconurl.hashCode() : 0);
        result = 31 * result + stateDownload;
        result = 31 * result + (int) (timeDownload ^ (timeDownload >>> 32));
        result = 31 * result + numRetry;
        return result;
    }
}
