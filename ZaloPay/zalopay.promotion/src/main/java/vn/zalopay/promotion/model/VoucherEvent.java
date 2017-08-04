package vn.zalopay.promotion.model;

import com.google.gson.annotations.SerializedName;

public class VoucherEvent extends PromotionEvent {
    @SerializedName("vouchercode")
    public String vouchercode;
}
