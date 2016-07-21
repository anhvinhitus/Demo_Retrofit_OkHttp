package vn.com.vng.zalopay.data.cache.model;

import android.os.Parcel;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AbstractData;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;

/**
 * Created by longlv on 21/07/2016.
 */
public class GetReceivePacket extends AbstractData{
    public long totalofrevamount;
    public int totalofrevpackage;
    public int numofluckiestdraw;
    public List<ReceivePackage> revpackageList;

    public GetReceivePacket(long totalofrevamount, int totalofrevpackage, int numofluckiestdraw, List<ReceivePackage> revpackageList) {
        this.totalofrevamount = totalofrevamount;
        this.totalofrevpackage = totalofrevpackage;
        this.numofluckiestdraw = numofluckiestdraw;
        this.revpackageList = revpackageList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.totalofrevamount);
        dest.writeInt(this.totalofrevpackage);
        dest.writeInt(this.numofluckiestdraw);
        dest.writeTypedList(this.revpackageList);
    }

    protected GetReceivePacket(Parcel in) {
        this.totalofrevamount = in.readLong();
        this.totalofrevpackage = in.readInt();
        this.numofluckiestdraw = in.readInt();
        this.revpackageList = in.createTypedArrayList(ReceivePackage.CREATOR);
    }

    public static final Creator<GetReceivePacket> CREATOR = new Creator<GetReceivePacket>() {
        @Override
        public GetReceivePacket createFromParcel(Parcel source) {
            return new GetReceivePacket(source);
        }

        @Override
        public GetReceivePacket[] newArray(int size) {
            return new GetReceivePacket[size];
        }
    };
}
