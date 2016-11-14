package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class TransHistoryEntity {

    @SerializedName("userid")
    public String userid;

    @SerializedName("transid")
    public long transid;

    @SerializedName("appid")
    public long appid;

    @SerializedName("appuser")
    public String appuser;

    @SerializedName("platform")
    public String platform;

    @SerializedName("description")
    public String description;

    @SerializedName("pmcid")
    public int pmcid;

    @SerializedName("reqdate")
    public long reqdate;

    @SerializedName("userchargeamt")
    public int userchargeamt;

    @SerializedName("amount")
    public int amount;

    @SerializedName("userfeeamt")
    public int userfeeamt;

    @SerializedName("type")
    public int type;

    @SerializedName("sign")
    public int sign;

    @SerializedName("username")
    public String username;

    @SerializedName("appusername")
    public String appusername;

    @SerializedName("transstatus")
    public int transstatus;

    @SerializedName("isretry")
    public boolean isretry;

    @Expose(serialize = false, deserialize = false)
    public int statustype = 1;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransHistoryEntity that = (TransHistoryEntity) o;

        if (transid != that.transid) return false;
        if (appid != that.appid) return false;
        if (pmcid != that.pmcid) return false;
        if (reqdate != that.reqdate) return false;
        if (userchargeamt != that.userchargeamt) return false;
        if (amount != that.amount) return false;
        if (userfeeamt != that.userfeeamt) return false;
        if (type != that.type) return false;
        if (sign != that.sign) return false;
        if (transstatus != that.transstatus) return false;
        if (isretry != that.isretry) return false;
        if (statustype != that.statustype) return false;
        if (!userid.equals(that.userid)) return false;
        if (!appuser.equals(that.appuser)) return false;
        if (!platform.equals(that.platform)) return false;
        if (!description.equals(that.description)) return false;
        if (!username.equals(that.username)) return false;
        return appusername.equals(that.appusername);

    }

    @Override
    public int hashCode() {
        int result = userid.hashCode();
        result = 31 * result + (int) (transid ^ (transid >>> 32));
        result = 31 * result + (int) (appid ^ (appid >>> 32));
        result = 31 * result + appuser.hashCode();
        result = 31 * result + platform.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + pmcid;
        result = 31 * result + (int) (reqdate ^ (reqdate >>> 32));
        result = 31 * result + userchargeamt;
        result = 31 * result + amount;
        result = 31 * result + userfeeamt;
        result = 31 * result + type;
        result = 31 * result + sign;
        result = 31 * result + username.hashCode();
        result = 31 * result + appusername.hashCode();
        result = 31 * result + transstatus;
        result = 31 * result + (isretry ? 1 : 0);
        result = 31 * result + statustype;
        return result;
    }
}
