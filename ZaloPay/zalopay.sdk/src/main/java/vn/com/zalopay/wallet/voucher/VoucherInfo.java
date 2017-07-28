package vn.com.zalopay.wallet.voucher;

import vn.com.zalopay.utility.GsonUtils;

/**
 * Created by chucvv on 7/28/17.
 */

public class VoucherInfo {
    public String vouchercode;
    public int campaignid;
    public long discountamount;
    public long usevouchertime;
    public String vouchersig;

    @Override
    public String toString() {
        return GsonUtils.toJsonString(this);
    }
}
