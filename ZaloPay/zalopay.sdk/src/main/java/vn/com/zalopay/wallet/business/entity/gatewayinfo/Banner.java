package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import android.os.Parcel;
import android.os.Parcelable;

public class Banner implements Parcelable {
    public int appid;
    public int bannertype;
    public String platformcode;
    public int function;
    public String dscreentypecode;
    public String logourl;
    public String webviewurl;
    public int orderindex;

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;
        if (object instanceof Banner) {
            Banner other = (Banner) object;

            String builderMe = String.valueOf(appid) +
                    bannertype +
                    platformcode +
                    function +
                    dscreentypecode +
                    logourl +
                    webviewurl +
                    orderindex;

            String builderOther = String.valueOf(other.appid) +
                    other.bannertype +
                    other.platformcode +
                    other.function +
                    other.dscreentypecode +
                    other.logourl +
                    other.webviewurl +
                    other.orderindex;

            sameSame = (builderMe.equals(builderOther));
        }
        return sameSame;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.appid);
        dest.writeInt(this.bannertype);
        dest.writeString(this.platformcode);
        dest.writeInt(this.function);
        dest.writeString(this.dscreentypecode);
        dest.writeString(this.logourl);
        dest.writeString(this.webviewurl);
        dest.writeInt(this.orderindex);
    }

    public Banner() {
    }

    protected Banner(Parcel in) {
        this.appid = in.readInt();
        this.bannertype = in.readInt();
        this.platformcode = in.readString();
        this.function = in.readInt();
        this.dscreentypecode = in.readString();
        this.logourl = in.readString();
        this.webviewurl = in.readString();
        this.orderindex = in.readInt();
    }

    public static final Parcelable.Creator<Banner> CREATOR = new Parcelable.Creator<Banner>() {
        @Override
        public Banner createFromParcel(Parcel source) {
            return new Banner(source);
        }

        @Override
        public Banner[] newArray(int size) {
            return new Banner[size];
        }
    };
}
