package vn.zalopay.promotion.model;

import com.google.gson.annotations.SerializedName;

public class CashBackEvent extends PromotionEvent {
    @SerializedName("amount")
    public long amount;
}
