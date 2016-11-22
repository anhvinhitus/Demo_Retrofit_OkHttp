package vn.com.vng.zalopay.domain.model.redpacket;

/**
 * Created by longlv on 15/07/2016.
 *
 */
public class ReceivePackage {

    public long packageID;
    public long bundleID;
    public String revZaloPayID;
    public String senderZaloPayID;
    public String senderFullName;
    public String senderAvatar;
    public String message;
    public long amount;
    public long openedTime;
    public int isLuckiest;
    public long createTime;
    public int status;

    public ReceivePackage() {

    }

    public ReceivePackage(long packageID, long bundleID,
                          String revZaloPayID,
                          String senderZaloPayID, String senderFullName,
                          String senderAvatar, String message,
                          long amount, long openedTime,
                          int isLuckiest, long createTime,
                          int status) {
        this.packageID = packageID;
        this.bundleID = bundleID;
        this.revZaloPayID = revZaloPayID;
        this.senderZaloPayID = senderZaloPayID;
        this.senderFullName = senderFullName;
        this.senderAvatar = senderAvatar;
        this.message = message;
        this.amount = amount;
        this.openedTime = openedTime;
        this.isLuckiest = isLuckiest;
        this.createTime = createTime;
        this.status = status;
    }
}
