package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/4/16.
 * *
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
    public long pmcid;

    @SerializedName("reqdate")
    public long reqdate;

    @SerializedName("userchargeamt")
    public long userchargeamt;

    @SerializedName("amount")
    public long amount;

    @SerializedName("userfeeamt")
    public long userfeeamt;

    @SerializedName("type")
    public long type;

    @SerializedName("sign")
    public long sign;

    @SerializedName("username")
    public String username;

    @SerializedName("appusername")
    public String appusername;

    @SerializedName("transstatus")
    public long transstatus;

    @SerializedName("isretry")
    public boolean isretry;

    @Expose(serialize = false, deserialize = false)
    public long statustype = 1;

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
        if (userid != null ? !userid.equals(that.userid) : that.userid != null) return false;
        if (appuser != null ? !appuser.equals(that.appuser) : that.appuser != null) return false;
        if (platform != null ? !platform.equals(that.platform) : that.platform != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (username != null ? !username.equals(that.username) : that.username != null)
            return false;
        return appusername != null ? appusername.equals(that.appusername) : that.appusername == null;

    }

    @Override
    public int hashCode() {
        int result = userid != null ? userid.hashCode() : 0;
        result = 31 * result + (int) (transid ^ (transid >>> 32));
        result = 31 * result + (int) (appid ^ (appid >>> 32));
        result = 31 * result + (appuser != null ? appuser.hashCode() : 0);
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (int) (pmcid ^ (pmcid >>> 32));
        result = 31 * result + (int) (reqdate ^ (reqdate >>> 32));
        result = 31 * result + (int) (userchargeamt ^ (userchargeamt >>> 32));
        result = 31 * result + (int) (amount ^ (amount >>> 32));
        result = 31 * result + (int) (userfeeamt ^ (userfeeamt >>> 32));
        result = 31 * result + (int) (type ^ (type >>> 32));
        result = 31 * result + (int) (sign ^ (sign >>> 32));
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (appusername != null ? appusername.hashCode() : 0);
        result = 31 * result + (int) (transstatus ^ (transstatus >>> 32));
        result = 31 * result + (isretry ? 1 : 0);
        result = 31 * result + (int) (statustype ^ (statustype >>> 32));
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
