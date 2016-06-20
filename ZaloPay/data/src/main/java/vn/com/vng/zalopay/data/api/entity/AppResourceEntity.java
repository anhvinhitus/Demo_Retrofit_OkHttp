package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/18/16.
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

        AppResourceEntity entity = (AppResourceEntity) o;

        if (appid != entity.appid) return false;
        if (needdownloadrs != entity.needdownloadrs) return false;
        if (status != entity.status) return false;
        if (stateDownload != entity.stateDownload) return false;
        if (timeDownload != entity.timeDownload) return false;
        if (numRetry != entity.numRetry) return false;
        if (appname != null ? !appname.equals(entity.appname) : entity.appname != null)
            return false;
        if (imageurl != null ? !imageurl.equals(entity.imageurl) : entity.imageurl != null)
            return false;
        if (jsurl != null ? !jsurl.equals(entity.jsurl) : entity.jsurl != null) return false;
        return checksum != null ? checksum.equals(entity.checksum) : entity.checksum == null;

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
        result = 31 * result + stateDownload;
        result = 31 * result + (int) (timeDownload ^ (timeDownload >>> 32));
        result = 31 * result + numRetry;
        return result;
    }
}
