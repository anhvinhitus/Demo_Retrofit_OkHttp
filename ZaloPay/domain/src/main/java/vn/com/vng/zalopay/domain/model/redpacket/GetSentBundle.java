package vn.com.vng.zalopay.domain.model.redpacket;


import android.os.Parcel;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 21/07/2016.
 *
 */
public class GetSentBundle extends AbstractData {

    public long totalofsentamount;
    public long totalofsentbundle;
    public List<SentBundle> sentbundlelist;

    public GetSentBundle(long totalofsentamount, long totalofsentbundle, List<SentBundle> sentbundlelist) {
        this.totalofsentamount = totalofsentamount;
        this.totalofsentbundle = totalofsentbundle;
        this.sentbundlelist = sentbundlelist;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.totalofsentamount);
        dest.writeLong(this.totalofsentbundle);
        dest.writeTypedList(this.sentbundlelist);
    }

    protected GetSentBundle(Parcel in) {
        this.totalofsentamount = in.readLong();
        this.totalofsentbundle = in.readLong();
        this.sentbundlelist = in.createTypedArrayList(SentBundle.CREATOR);
    }

    public static final Creator<GetSentBundle> CREATOR = new Creator<GetSentBundle>() {
        @Override
        public GetSentBundle createFromParcel(Parcel source) {
            return new GetSentBundle(source);
        }

        @Override
        public GetSentBundle[] newArray(int size) {
            return new GetSentBundle[size];
        }
    };
}