package vn.com.vng.zalopay.domain.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by longlv on 09/05/2016.
 */
public class Order extends AbstractData {

    private String apptransid;
    private String appuser;
    private String item;
    private String amount;
    private String description;
    private String payoption;
    private Mac mac;
    private AppInfo appinfo;

    public Order(String apptransid, String appuser, String item, String amount, String description, String payoption, Mac mac, AppInfo appinfo) {
        this.apptransid = apptransid;
        this.appuser = appuser;
        this.item = item;
        this.amount = amount;
        this.description = description;
        this.payoption = payoption;
        this.mac = mac;
        this.appinfo = appinfo;
    }

    public Order(Parcel in) {
        apptransid = in.readString();
        appuser = in.readString();
        item = in.readString();
        amount = in.readString();
        description = in.readString();
        payoption = in.readString();
        mac = in.readParcelable(Mac.class.getClassLoader());
        appinfo = in.readParcelable(AppInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(apptransid);
        dest.writeString(appuser);
        dest.writeString(item);
        dest.writeString(amount);
        dest.writeString(description);
        dest.writeString(payoption);
        dest.writeParcelable(mac, flags);
        dest.writeParcelable(appinfo, flags);
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

    public static class Mac extends AbstractData {
        private String orderinfo;
        private String mac;

        public Mac(String orderinfo, String mac) {
            this.orderinfo = orderinfo;
            this.mac = mac;
        }

        public Mac(Parcel in) {
            orderinfo = in.readString();
            mac = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(orderinfo);
            dest.writeString(mac);
        }

        public final Parcelable.Creator<Mac> CREATOR = new Parcelable.Creator<Mac>() {
            @Override
            public Mac createFromParcel(Parcel source) {
                return new Mac(source);
            }

            @Override
            public Mac[] newArray(int size) {
                return new Mac[size];
            }
        };

        public String getOrderinfo() {
            return orderinfo;
        }

        public String getMac() {
            return mac;
        }

        public void setOrderinfo(String orderinfo) {
            this.orderinfo = orderinfo;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }
    }

    public static class AppInfo extends AbstractData {
        private String appname;
        private String logourl;
        private String status;

        public AppInfo(String appname, String logourl, String status) {
            this.appname = appname;
            this.logourl = logourl;
            this.status = status;
        }

        public AppInfo(Parcel in) {
            appname = in.readString();
            logourl = in.readString();
            status = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(appname);
            dest.writeString(logourl);
            dest.writeString(status);
        }

        public final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
            @Override
            public AppInfo createFromParcel(Parcel source) {
                return new AppInfo(source);
            }

            @Override
            public AppInfo[] newArray(int size) {
                return new AppInfo[size];
            }
        };

        public String getAppname() {
            return appname;
        }

        public String getLogourl() {
            return logourl;
        }

        public String getStatus() {
            return status;
        }

        public void setAppname(String appname) {
            this.appname = appname;
        }

        public void setLogourl(String logourl) {
            this.logourl = logourl;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

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

    public Mac getMac() {
        return mac;
    }

    public AppInfo getAppinfo() {
        return appinfo;
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

    public void setMac(Mac mac) {
        this.mac = mac;
    }

    public void setAppinfo(AppInfo appinfo) {
        this.appinfo = appinfo;
    }
}
