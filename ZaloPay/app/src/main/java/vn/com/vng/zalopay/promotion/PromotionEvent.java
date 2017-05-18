package vn.com.vng.zalopay.promotion;

import java.util.List;

public class PromotionEvent {
    @PromotionType
    public int type;

    public String title;
    public long amount;
    public String campaign;
    public List<PromotionAction> actions;
    public long transid;
}
