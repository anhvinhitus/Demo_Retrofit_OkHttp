package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AnhHieu on 5/23/16.
 */
public class AppResource extends AbstractData {

    public long appid;

    public String appname;

    public String checksum;

    public int status;

    public String urlImage;

    public AppResource(Parcel source) {
        appid = source.readLong();
        appname = source.readString();
        checksum = source.readString();
    }

    public AppResource(long appid, String appname, String urlImage) {
        this.appid = appid;
        this.appname = appname;
        this.urlImage = urlImage;
    }

    public AppResource() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(appid);
        dest.writeString(appname);
        dest.writeString(checksum);
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
