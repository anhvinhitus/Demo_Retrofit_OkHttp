package vn.com.zalopay.wallet.voucher;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import vn.com.zalopay.utility.GsonUtils;

/**
 * Created by chucvv on 7/28/17.
 */

public class VoucherInfo {
    @Expose
    public String vouchercode;

    @SerializedName("campaignID")
    public int campaignid;
    @SerializedName("discountAmount")
    public long discountamount;
    @SerializedName("useVoucherTime")
    public long usevouchertime;
    @SerializedName("voucherSig")
    public String vouchersig;

    @Override
    public String toString() {
        return GsonUtils.toJsonString(this);
    }
}
