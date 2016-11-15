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

        TransHistoryEntity entity = (TransHistoryEntity) o;

        if (transid != entity.transid) return false;
        if (appid != entity.appid) return false;
        if (pmcid != entity.pmcid) return false;
        if (reqdate != entity.reqdate) return false;
        if (userchargeamt != entity.userchargeamt) return false;
        if (amount != entity.amount) return false;
        if (userfeeamt != entity.userfeeamt) return false;
        if (type != entity.type) return false;
        if (sign != entity.sign) return false;
        if (transstatus != entity.transstatus) return false;
        if (isretry != entity.isretry) return false;
        if (!userid.equals(entity.userid)) return false;
        if (appuser != null ? !appuser.equals(entity.appuser) : entity.appuser != null)
            return false;
        if (platform != null ? !platform.equals(entity.platform) : entity.platform != null)
            return false;
        if (description != null ? !description.equals(entity.description) : entity.description != null)
            return false;
        if (!username.equals(entity.username)) return false;
        return appusername != null ? appusername.equals(entity.appusername) : entity.appusername == null;

    }

    @Override
    public int hashCode() {
        int result = userid.hashCode();
        result = 31 * result + (int) (transid ^ (transid >>> 32));
        result = 31 * result + (int) (appid ^ (appid >>> 32));
        result = 31 * result + (appuser != null ? appuser.hashCode() : 0);
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + pmcid;
        result = 31 * result + (int) (reqdate ^ (reqdate >>> 32));
        result = 31 * result + userchargeamt;
        result = 31 * result + amount;
        result = 31 * result + userfeeamt;
        result = 31 * result + type;
        result = 31 * result + sign;
        result = 31 * result + username.hashCode();
        result = 31 * result + (appusername != null ? appusername.hashCode() : 0);
        result = 31 * result + transstatus;
        result = 31 * result + (isretry ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TransHistoryEntity{" +
                "userid='" + userid + '\'' +
                ", transid=" + transid +
                ", appid=" + appid +
                ", appuser='" + appuser + '\'' +
                ", platform='" + platform + '\'' +
                ", description='" + description + '\'' +
                ", pmcid=" + pmcid +
                ", reqdate=" + reqdate +
                ", userchargeamt=" + userchargeamt +
                ", amount=" + amount +
                ", userfeeamt=" + userfeeamt +
                ", type=" + type +
                ", sign=" + sign +
                ", username='" + username + '\'' +
                ", appusername='" + appusername + '\'' +
                ", transstatus=" + transstatus +
                ", isretry=" + isretry +
                ", statustype=" + statustype +
                '}';
    }
}
