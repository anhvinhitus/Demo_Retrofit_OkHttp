package vn.zalopay.promotion;

import com.google.gson.annotations.SerializedName;

public class PromotionAction {
    @SerializedName("title")
    public String title;

    @SerializedName("action")
    @ActionType
    public int action;
}
