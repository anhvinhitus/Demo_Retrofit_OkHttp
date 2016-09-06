package vn.com.vng.zalopay.domain.model;

import org.parceler.Parcel;

/**
 * Created by AnhHieu on 5/23/16.
 */
@Parcel
public class AppResource {

    public int appid;

    public String appname;

    public String checksum;

    public int status;

    public String urlImage;

    public int appType;

    public String webUrl;

    public String iconUrl;

    public AppResource(int appid) {
        this.appid = appid;
    }

    public AppResource(int appid, int appType, String appname, String urlImage) {
        this.appid = appid;
        this.appType = appType;
        this.appname = appname;
        this.iconUrl = urlImage;
    }

    public AppResource() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppResource that = (AppResource) o;

        return appid == that.appid;

    }

    @Override
    public int hashCode() {
        int result = appid;
        result = 31 * result + (appname != null ? appname.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + (urlImage != null ? urlImage.hashCode() : 0);
        result = 31 * result + appType;
        result = 31 * result + (webUrl != null ? webUrl.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        return result;
    }
}
