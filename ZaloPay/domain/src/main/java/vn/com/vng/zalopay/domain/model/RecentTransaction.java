package vn.com.vng.zalopay.domain.model;

import org.parceler.Parcel;

/**
 * Created by longlv on 11/06/2016.
 */
@Parcel
public class RecentTransaction {

    public long zaloId;
    public String zaloPayId;
    public String zaloPayName;
    public String displayName;
    public String avatar;
    public String phoneNumber;
    public long amount;
    public String message;

    public RecentTransaction() {
    }

    public RecentTransaction(long zaloId, String zaloPayId, String userName, String displayName, String avatar, String phoneNumber, long amount, String message) {
        this.zaloId = zaloId;
        this.zaloPayId = zaloPayId;
        this.zaloPayName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.message = message;
    }
}
