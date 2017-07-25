package vn.zalopay.promotion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PromotionEvent {
    @SerializedName("type")
    @PromotionType
    public int type;

    @SerializedName("title")
    public String title;

    @SerializedName("amount")
    public long amount;

    @SerializedName("campaign")
    public String campaign;

    @SerializedName("actions")
    public List<PromotionAction> actions;

    @Expose
    public long transid;

    @Expose
    public long notificationId;
}
