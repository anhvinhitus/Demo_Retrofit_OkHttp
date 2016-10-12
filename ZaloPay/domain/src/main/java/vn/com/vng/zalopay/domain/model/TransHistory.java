package vn.com.vng.zalopay.domain.model;

/**
 * Created by AnhHieu on 5/4/16.
 */

@org.parceler.Parcel
public class TransHistory extends AbstractData {

    public TransHistory() {
    }

    public TransHistory(long transid) {
        this.transid = transid;
    }

    public String userid;

    public long transid;

    public long appid;

    public String appuser;

    public String platform;

    public String description;

    public int pmcid;

    public long reqdate;

    public int userchargeamt;

    public int userfeeamt;

    public int amount;

    public int type;

    public int sign;

    public String username;

    public String appusername;

}
