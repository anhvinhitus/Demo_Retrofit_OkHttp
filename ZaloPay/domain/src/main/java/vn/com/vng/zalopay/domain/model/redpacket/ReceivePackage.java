package vn.com.vng.zalopay.domain.model.redpacket;

/**
 * Created by longlv on 15/07/2016.
 *
 */
@org.parceler.Parcel
public class ReceivePackage {

    public long packageID;
    public long bundleID;
    public String revZaloPayID;
    public String sendZaloPayID;
    public String sendFullName;
    public long amount;
    public long openedTime;

    public ReceivePackage() {

    }

    public ReceivePackage(long packageID, long bundleID, String revZaloPayID, String sendZaloPayID, String sendFullName, long amount, long openedTime) {
        this.packageID = packageID;
        this.bundleID = bundleID;
        this.revZaloPayID = revZaloPayID;
        this.sendZaloPayID = sendZaloPayID;
        this.sendFullName = sendFullName;
        this.amount = amount;
        this.openedTime = openedTime;
    }
}
