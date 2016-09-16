package vn.com.vng.zalopay.domain.model;

import org.parceler.Parcel;

@Parcel
public final class MappingZaloAndZaloPay {

    public long zaloId;
    public String zaloPayId;
    public String zaloPayName;
    public long phonenumber;

    public MappingZaloAndZaloPay() {
        this.zaloId = -1;
        this.zaloPayId = "";
        this.zaloPayName = "";
        this.phonenumber = 0;
    }

    public MappingZaloAndZaloPay(long zaloId, String zaloPayId, String zaloPayName, long phonenumber) {
        this.zaloId = zaloId;
        this.zaloPayId = zaloPayId;
        this.zaloPayName = zaloPayName;
        this.phonenumber = phonenumber;
    }

/*
    public long getZaloId() {
        return zaloId;
    }
*/

    public String getZaloPayId() {
        return zaloPayId;
    }

    public String getZaloPayName() {
        return zaloPayName;
    }

    public long getPhonenumber() {
        return phonenumber;
    }

    public void setZaloId(long zaloId) {
        this.zaloId = zaloId;
    }

    public void setZaloPayId(String zaloPayId) {
        this.zaloPayId = zaloPayId;
    }

    public void setPhonenumber(long phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setZaloPayName(String zaloPayName) {
        this.zaloPayName = zaloPayName;
    }
}
