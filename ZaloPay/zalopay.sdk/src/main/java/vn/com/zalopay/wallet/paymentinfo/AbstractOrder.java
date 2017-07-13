package vn.com.zalopay.wallet.paymentinfo;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.vng.zalopay.data.util.Strings;

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

    public AbstractOrder clone() {
        AbstractOrder order = new AbstractOrder();
        order.appid = this.appid;
        order.apptransid = this.apptransid;
        order.embeddata = this.embeddata;
        order.item = this.item;
        order.description = this.description;
        order.mac = this.mac;
        order.apptime = this.apptime;
        order.amount = this.amount;
        order.appuser = this.appuser;
        order.ordersource = this.ordersource;
        order.amount_total = this.amount_total;
        order.fee = this.fee;
        return order;
    }

    public void plusChannelFee(double channel_fee) {
        fee = channel_fee;
        amount_total = amount + fee;
        Timber.d("calculate order fee %s", fee);
    }

    public List<NameValuePair> parseItems() {
        if (TextUtils.isEmpty(item)) {
            Timber.d("item is empty - skip render item detail");
            return null;
        }
        List<NameValuePair> items = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(item);
            String itemExt = jsonObject.optString("ext");
            if (!TextUtils.isEmpty(itemExt)) {
                items = Strings.parseNameValues(itemExt);
            }
        } catch (Exception e) {
            Timber.d(e, "parse order item error");
        }
        /*List<NameValuePair> expected = new ArrayList<>();
        expected.add(new NameValuePair("Nhà mạng", "Viettel"));
        expected.add(new NameValuePair("Mệnh giá", "50.000 VND"));
        expected.add(new NameValuePair("Nạp cho", "Số của tôi - 0902167233"));*/
        return items;
    }
}
