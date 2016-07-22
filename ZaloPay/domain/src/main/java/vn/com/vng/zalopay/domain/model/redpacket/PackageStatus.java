package vn.com.vng.zalopay.domain.model.redpacket;

import org.parceler.Parcel;

/**
 * Created by longlv on 18/07/2016.
 *
 */
@Parcel
public class PackageStatus {
    public boolean isProcessing;
    public String zpTransID;
    public long reqdate;
    public long amount;
    public long balance;
    public String data;

    public PackageStatus() {

    }

    public PackageStatus(boolean isProcessing, String zpTransID, long reqdate, long amount, long balance, String data) {
        this.isProcessing = isProcessing;
        this.zpTransID = zpTransID;
        this.reqdate = reqdate;
        this.amount = amount;
        this.balance = balance;
        this.data = data;
    }

}
