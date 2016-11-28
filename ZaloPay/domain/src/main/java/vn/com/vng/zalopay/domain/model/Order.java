package vn.com.vng.zalopay.domain.model;

import org.json.JSONException;
import org.json.JSONObject;

import vn.com.vng.zalopay.domain.Constants;


/**
 * Created by longlv on 09/05/2016.
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

    public Order(JSONObject jsonObject) throws JSONException {
        appid = (long)jsonObject.getDouble(Constants.APPID);
        if (jsonObject.has(Constants.ZPTRANSTOKEN)) {
            zptranstoken = jsonObject.getString(Constants.ZPTRANSTOKEN);
        } else {
            zptranstoken = "";
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
        } else {
            payoption = "";
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
}
