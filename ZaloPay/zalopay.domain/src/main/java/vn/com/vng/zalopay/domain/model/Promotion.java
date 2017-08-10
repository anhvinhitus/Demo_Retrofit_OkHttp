package vn.com.vng.zalopay.domain.model;

/*
 * Created by chucvv on 8/10/17.
 */

import com.google.gson.annotations.SerializedName;

public class Promotion {

    @SerializedName("voucher")
    public Voucher mVoucher;

    public final class Voucher {
        @SerializedName("allow_payment_use_voucher")
        public int mAllowPaymentVoucher = 0;
    }
}
