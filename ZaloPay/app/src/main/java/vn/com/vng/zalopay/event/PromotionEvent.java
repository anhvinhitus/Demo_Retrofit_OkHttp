package vn.com.vng.zalopay.event;

import java.util.List;

import vn.com.vng.zalopay.promotion.PromotionAction;
import vn.com.vng.zalopay.promotion.PromotionType;

public class PromotionEvent {
    @PromotionType
    public int type;

    public String title;
    public long amount;
    public String campaign;
    public List<PromotionAction> actions;
    public long transid;

    public PromotionEvent(int pType, String pTitle, long pAmount, String pCampain, List<PromotionAction> pAction, long pTransId) {
        type = pType;
        title = pTitle;
        amount = pAmount;
        campaign = pCampain;
        actions = pAction;
        transid = pTransId;
    }
}
