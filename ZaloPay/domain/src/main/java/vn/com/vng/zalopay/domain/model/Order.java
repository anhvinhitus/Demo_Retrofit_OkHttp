package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by longlv on 09/05/2016.
 */
public class Order extends AbstractData {

    private String apptransid;
    private String appuser;
    public String apptime;
    public String embeddata;
    private String item;
    private String amount;
    private String description;
    private String payoption;
    private String mac;

    public Order(String apptransid, String appuser, String apptime, String embeddata, String item, String amount, String description, String payoption, String mac) {
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

    public Order(Parcel in) {
        apptransid = in.readString();
        appuser = in.readString();
        apptime = in.readString();
        embeddata = in.readString();
        item = in.readString();
        amount = in.readString();
        description = in.readString();
        payoption = in.readString();
        mac = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(apptransid);
        dest.writeString(appuser);
        dest.writeString(apptime);
        dest.writeString(embeddata);
        dest.writeString(item);
        dest.writeString(amount);
        dest.writeString(description);
        dest.writeString(payoption);
        dest.writeString(mac);
    }

    public final Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public String getApptransid() {
        return apptransid;
    }

    public String getAppuser() {
        return appuser;
    }

    public String getItem() {
        return item;
    }

    public String getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getPayoption() {
        return payoption;
    }

    public String getMac() {
        return mac;
    }

    public void setApptransid(String apptransid) {
        this.apptransid = apptransid;
    }

    public void setAppuser(String appuser) {
        this.appuser = appuser;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPayoption(String payoption) {
        this.payoption = payoption;
    }

    public String getApptime() {
        return apptime;
    }

    public String getEmbeddata() {
        return embeddata;
    }

    public void setApptime(String apptime) {
        this.apptime = apptime;
    }

    public void setEmbeddata(String embeddata) {
        this.embeddata = embeddata;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(apptransid);
        stringBuilder.append(", ");
        stringBuilder.append(appuser);
        stringBuilder.append(", ");
        stringBuilder.append(apptime);
        stringBuilder.append(", ");
        stringBuilder.append(embeddata);
        stringBuilder.append(", ");
        stringBuilder.append(item);
        stringBuilder.append(", ");
        stringBuilder.append(amount);
        stringBuilder.append(", ");;
        stringBuilder.append(description);
        stringBuilder.append(", ");
        stringBuilder.append(payoption);
        stringBuilder.append(", ");
        stringBuilder.append(mac);
        stringBuilder.append(", ");
        return stringBuilder.toString();
    }
}
