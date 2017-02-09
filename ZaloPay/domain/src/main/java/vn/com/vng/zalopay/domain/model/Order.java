package vn.com.vng.zalopay.domain.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import vn.com.vng.zalopay.domain.Constants;


/**
 * Created by longlv on 09/05/2016.
 * *
 */
public class Order extends AbstractData {

    public final long appid;
    public final String zptranstoken;
    public final String apptransid;
    public final String appuser;
    public long apptime;
    public final String embeddata;
    public final String item;
    public long amount;
    public final String description;
    public final String payoption;
    public final String mac;

    public Order(long appid, String zptranstoken, String apptransid, String appuser, long apptime,
                 String embeddata, String item, long amount, String description, String payoption, String mac) {
        this.appid = appid;
        this.zptranstoken = zptranstoken;
        this.apptransid = apptransid;
        this.appuser = appuser;
        this.apptime = apptime;
        this.embeddata = embeddata;
        this.item = item;
        this.amount = amount;
        this.description = description;
        this.payoption = payoption;
        this.mac = mac;
    }

    public Order(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }

    public Order(JSONObject jsonObject) {
        appid = jsonObject.optLong(Constants.APPID);
        zptranstoken = jsonObject.optString(Constants.ZPTRANSTOKEN);
        apptransid = jsonObject.optString(Constants.APPTRANSID);
        appuser = jsonObject.optString(Constants.APPUSER);
        apptime = jsonObject.optLong(Constants.APPTIME);
        amount = jsonObject.optLong(Constants.AMOUNT);
        item = jsonObject.optString(Constants.ITEM);
        description = jsonObject.optString(Constants.DESCRIPTION);
        embeddata = jsonObject.optString(Constants.EMBEDDATA);
        mac = jsonObject.optString(Constants.MAC);
        payoption = jsonObject.optString(Constants.CHARGEINFO);
    }

    public boolean isValid() {
        return this.appid >= 0
                && !TextUtils.isEmpty(this.apptransid)
                && !TextUtils.isEmpty(this.appuser)
                && this.apptime > 0
                && !TextUtils.isEmpty(this.item)
                && this.amount >= 0
                && !TextUtils.isEmpty(this.description)
                && !TextUtils.isEmpty(this.mac);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (appid != order.appid) return false;
        if (apptime != order.apptime) return false;
        if (amount != order.amount) return false;
        if (zptranstoken != null ? !zptranstoken.equals(order.zptranstoken) : order.zptranstoken != null)
            return false;
        if (apptransid != null ? !apptransid.equals(order.apptransid) : order.apptransid != null)
            return false;
        if (appuser != null ? !appuser.equals(order.appuser) : order.appuser != null) return false;
        if (embeddata != null ? !embeddata.equals(order.embeddata) : order.embeddata != null)
            return false;
        if (item != null ? !item.equals(order.item) : order.item != null) return false;
        if (description != null ? !description.equals(order.description) : order.description != null)
            return false;
        if (payoption != null ? !payoption.equals(order.payoption) : order.payoption != null)
            return false;
        return mac != null ? mac.equals(order.mac) : order.mac == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (appid ^ (appid >>> 32));
        result = 31 * result + (zptranstoken != null ? zptranstoken.hashCode() : 0);
        result = 31 * result + (apptransid != null ? apptransid.hashCode() : 0);
        result = 31 * result + (appuser != null ? appuser.hashCode() : 0);
        result = 31 * result + (int) (apptime ^ (apptime >>> 32));
        result = 31 * result + (embeddata != null ? embeddata.hashCode() : 0);
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (int) (amount ^ (amount >>> 32));
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (payoption != null ? payoption.hashCode() : 0);
        result = 31 * result + (mac != null ? mac.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("appid: ");
        stringBuilder.append(appid);
        stringBuilder.append(", ");
        stringBuilder.append("zptranstoken: ");
        stringBuilder.append(zptranstoken);
        stringBuilder.append(", ");
        stringBuilder.append("apptransid: ");
        stringBuilder.append(apptransid);
        stringBuilder.append(", ");
        stringBuilder.append("appuser: ");
        stringBuilder.append(appuser);
        stringBuilder.append(", ");
        stringBuilder.append("apptime: ");
        stringBuilder.append(apptime);
        stringBuilder.append(", ");
        stringBuilder.append("embeddata: ");
        stringBuilder.append(embeddata);
        stringBuilder.append(", ");
        stringBuilder.append("item: ");
        stringBuilder.append(item);
        stringBuilder.append(", ");
        stringBuilder.append("amount: ");
        stringBuilder.append(amount);
        stringBuilder.append(", ");
        stringBuilder.append("description: ");
        stringBuilder.append(description);
        stringBuilder.append(", ");
        stringBuilder.append("payoption: ");
        stringBuilder.append(payoption);
        stringBuilder.append(", ");
        stringBuilder.append("mac: ");
        stringBuilder.append(mac);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
