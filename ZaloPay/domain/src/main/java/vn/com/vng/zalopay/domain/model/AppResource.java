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

    public String appType;

    public String webUrl;

    public String iconUrl;

    public AppResource(int appid, String appname, String urlImage) {
        this.appid = appid;
        this.appname = appname;
        this.urlImage = urlImage;
    }

    public AppResource(int appid, String appname, String urlImage, int status) {
        this.appid = appid;
        this.appname = appname;
        this.urlImage = urlImage;
        this.status = status;
    }

    public AppResource(int appid, String appname, String checksum, int status, String urlImage, String appType, String webUrl, String iconUrl) {
        this.appid = appid;
        this.appname = appname;
        this.checksum = checksum;
        this.status = status;
        this.urlImage = urlImage;
        this.appType = appType;
        this.webUrl = webUrl;
        this.iconUrl = iconUrl;
    }

    public AppResource() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppResource that = (AppResource) o;

        if (appid != that.appid) return false;
        if (status != that.status) return false;
        if (appname != null ? !appname.equals(that.appname) : that.appname != null) return false;
        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null)
            return false;
        if (urlImage != null ? !urlImage.equals(that.urlImage) : that.urlImage != null)
            return false;
        if (appType != null ? !appType.equals(that.appType) : that.appType != null) return false;
        if (webUrl != null ? !webUrl.equals(that.webUrl) : that.webUrl != null) return false;
        return iconUrl != null ? iconUrl.equals(that.iconUrl) : that.iconUrl == null;

    }

    @Override
    public int hashCode() {
        int result = appid;
        result = 31 * result + (appname != null ? appname.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + (urlImage != null ? urlImage.hashCode() : 0);
        result = 31 * result + (appType != null ? appType.hashCode() : 0);
        result = 31 * result + (webUrl != null ? webUrl.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        return result;
    }
}
