package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AppResource implements Parcelable {

    public long appid;

    public String appname;

    public String checksum;

    public long status;

    public String urlImage;

    public long appType;

    public String webUrl;

    public String iconUrl;

    public AppResource(long appid) {
        this.appid = appid;
    }

    public AppResource(long appid, long appType, String appname) {
        this(appid, appType, appname, "");
    }

    public AppResource(long appid, long appType, String appname, String urlImage) {
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

        return appid == that.appid && appType == that.appType;

    }

    @Override
    public int hashCode() {
        int result = (int) (appid ^ (appid >>> 32));
        result = 31 * result + (appname != null ? appname.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (int) (status ^ (status >>> 32));
        result = 31 * result + (urlImage != null ? urlImage.hashCode() : 0);
        result = 31 * result + (int) (appType ^ (appType >>> 32));
        result = 31 * result + (webUrl != null ? webUrl.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.appid);
        dest.writeString(this.appname);
        dest.writeString(this.checksum);
        dest.writeLong(this.status);
        dest.writeString(this.urlImage);
        dest.writeLong(this.appType);
        dest.writeString(this.webUrl);
        dest.writeString(this.iconUrl);
    }

    protected AppResource(Parcel in) {
        this.appid = in.readInt();
        this.appname = in.readString();
        this.checksum = in.readString();
        this.status = in.readInt();
        this.urlImage = in.readString();
        this.appType = in.readInt();
        this.webUrl = in.readString();
        this.iconUrl = in.readString();
    }

    public static final Parcelable.Creator<AppResource> CREATOR = new Parcelable.Creator<AppResource>() {
        @Override
        public AppResource createFromParcel(Parcel source) {
            return new AppResource(source);
        }

        @Override
        public AppResource[] newArray(int size) {
            return new AppResource[size];
        }
    };
}
