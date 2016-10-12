package vn.com.vng.zalopay.domain.model;

import org.json.JSONException;
import org.json.JSONObject;

import vn.com.vng.zalopay.domain.Constants;


/**
 * Created by longlv on 09/05/2016.
 */
@org.parceler.Parcel
public class Order extends AbstractData {
    public long appid;
    public String zptranstoken;
    public String apptransid;
    public String appuser;
    public long apptime;
    public String embeddata;
    public String item;
    public long amount;
    public String description;
    public String payoption;
    public String mac;

    public Order() {
    }

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

    public Order(JSONObject jsonObject) throws JSONException {
        appid = (long)jsonObject.getDouble(Constants.APPID);
        if (jsonObject.has(Constants.ZPTRANSTOKEN)) {
            zptranstoken = jsonObject.getString(Constants.ZPTRANSTOKEN);
        }
        apptransid = jsonObject.getString(Constants.APPTRANSID);
        appuser = jsonObject.getString(Constants.APPUSER);
        apptime = Long.parseLong(jsonObject.getString(Constants.APPTIME));
        amount = Long.parseLong(jsonObject.getString(Constants.AMOUNT));
        item = jsonObject.getString(Constants.ITEM);
        description = jsonObject.getString(Constants.DESCRIPTION);
        embeddata = jsonObject.getString(Constants.EMBEDDATA);
        mac = jsonObject.getString(Constants.MAC);
        if (jsonObject.has(Constants.ZPTRANSTOKEN)) {
            payoption = jsonObject.getString(Constants.CHARGEINFO);
        }
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


    public long getAppid() {
        return appid;
    }

    public void setAppid(long appid) {
        this.appid = appid;
    }

    public String getZptranstoken() {
        return zptranstoken;
    }

    public void setZptranstoken(String zptranstoken) {
        this.zptranstoken = zptranstoken;
    }

    public String getApptransid() {
        return apptransid;
    }

    public void setApptransid(String apptransid) {
        this.apptransid = apptransid;
    }

    public String getAppuser() {
        return appuser;
    }

    public void setAppuser(String appuser) {
        this.appuser = appuser;
    }

    public long getApptime() {
        return apptime;
    }

    public void setApptime(long apptime) {
        this.apptime = apptime;
    }

    public String getEmbeddata() {
        return embeddata;
    }

    public void setEmbeddata(String embeddata) {
        this.embeddata = embeddata;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPayoption() {
        return payoption;
    }

    public void setPayoption(String payoption) {
        this.payoption = payoption;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
