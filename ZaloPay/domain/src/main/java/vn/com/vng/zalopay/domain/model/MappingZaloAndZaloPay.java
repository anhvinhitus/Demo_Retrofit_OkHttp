package vn.com.vng.zalopay.domain.model;

import org.parceler.Parcel;

/**
 * Created by AnhHieu on 3/25/16.
 *
 */

@Parcel
public final class MappingZaloAndZaloPay {

    public long zaloId;
    public String zaloPayId;
    public String phonenumber;

    public MappingZaloAndZaloPay() {
        this.zaloId = -1;
        this.zaloPayId = "";
        this.phonenumber = "";
    }

    public MappingZaloAndZaloPay(long zaloId, String zaloPayId, String phonenumber) {
        this.zaloId = zaloId;
        this.zaloPayId = zaloPayId;
        this.phonenumber = phonenumber;
    }

    public long getZaloId() {
        return zaloId;
    }

    public String getZaloPayId() {
        return zaloPayId;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setZaloId(long zaloId) {
        this.zaloId = zaloId;
    }

    public void setZaloPayId(String zaloPayId) {
        this.zaloPayId = zaloPayId;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
