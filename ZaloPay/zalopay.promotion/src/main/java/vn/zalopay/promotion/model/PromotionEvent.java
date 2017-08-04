package vn.zalopay.promotion.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.zalopay.promotion.PromotionAction;
import vn.zalopay.promotion.PromotionType;

public class PromotionEvent {
    @SerializedName("type")
    @PromotionType
    public int type;

    @SerializedName("title")
    public String title;

    @SerializedName("campaign")
    public String campaign;

    @SerializedName("actions")
    public List<PromotionAction> actions;

    @Expose
    public long transid;

    @Expose
    public long notificationId;
}
