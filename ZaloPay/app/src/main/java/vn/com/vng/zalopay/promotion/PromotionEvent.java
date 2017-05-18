package vn.com.vng.zalopay.promotion;

import java.util.List;

public class PromotionEvent {
    @PromotionType
    public int type;

    public String title;
    public long amount;
    public String campaign;
    public List<PromotionAction> actions;

    public PromotionEvent(int pType, String pTitle, long pAmount, String pCompaign, List<PromotionAction> pActions) {
        type = pType;
        title = pTitle;
        amount = pAmount;
        campaign = pCompaign;
        actions = pActions;
    }
}
