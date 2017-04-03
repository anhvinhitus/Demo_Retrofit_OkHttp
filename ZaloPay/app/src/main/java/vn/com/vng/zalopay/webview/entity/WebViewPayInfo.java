package vn.com.vng.zalopay.webview.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class WebViewPayInfo implements Parcelable {

    public String uid;
    public String accessToken;
    public long appId;
    public String apptransid;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.accessToken);
        dest.writeLong(this.appId);
        dest.writeString(this.apptransid);
    }

    public WebViewPayInfo() {
    }

    protected WebViewPayInfo(Parcel in) {
        this.uid = in.readString();
        this.accessToken = in.readString();
        this.appId = in.readInt();
        this.apptransid = in.readString();
    }

    public static final Parcelable.Creator<WebViewPayInfo> CREATOR = new Parcelable.Creator<WebViewPayInfo>() {
        @Override
        public WebViewPayInfo createFromParcel(Parcel source) {
            return new WebViewPayInfo(source);
        }

        @Override
        public WebViewPayInfo[] newArray(int size) {
            return new WebViewPayInfo[size];
        }
    };
}
