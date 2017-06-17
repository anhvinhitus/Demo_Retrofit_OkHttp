package vn.com.zalopay.wallet.paymentinfo;

import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;

/**
 * Created by chucvv on 6/5/17.
 */

public class AbstractOrder {
    public long appid;
    public String apptransid;
    public String embeddata;
    public String item;
    public String description;
    public String mac;
    public long apptime;
    public long amount;
    public String appuser;
    public int ordersource;
    public double amount_total;
    public double fee;

    public void populateFee(MiniPmcTransType pmcTransType) {
        if (pmcTransType != null) {
            fee = pmcTransType.totalfee;
            amount_total = amount + fee;
            Log.d(this, "order fee " + fee);
        }
    }
}
