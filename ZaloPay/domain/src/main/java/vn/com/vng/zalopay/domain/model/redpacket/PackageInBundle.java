package vn.com.vng.zalopay.domain.model.redpacket;

/**
 * Created by longlv on 15/07/2016.
 *
 */
public class PackageInBundle {

    public long packageID;
    public long bundleID;
    public String revZaloPayID;
    public long revZaloID;
    public String revFullName;
    public String revAvatarURL;
    public long openTime;
    public long amount;
    public String sendMessage;
    public boolean isLuckiest;

    public PackageInBundle(long packageID, long bundleID, String revZaloPayID, long revZaloID, String revFullName, String revAvatarURL, long openTime, long amount, String sendMessage, boolean isLuckiest) {
        this.packageID = packageID;
        this.bundleID = bundleID;
        this.revZaloPayID = revZaloPayID;
        this.revZaloID = revZaloID;
        this.revFullName = revFullName;
        this.revAvatarURL = revAvatarURL;
        this.openTime = openTime;
        this.amount = amount;
        this.sendMessage = sendMessage;
        this.isLuckiest = isLuckiest;
    }
}
