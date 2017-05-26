package vn.zalopay.promotion;

import java.util.List;

import vn.zalopay.promotion.PromotionAction;
import vn.zalopay.promotion.PromotionType;

public class PromotionEvent {
    @PromotionType
    public int type;

    public String title;
    public long amount;
    public String campaign;
    public List<PromotionAction> actions;
    public long transid;
    public long notificationId;

    public PromotionEvent(int pType, String pTitle, long pAmount, String pCampain, List<PromotionAction> pAction, long pTransId, long pNotificationId) {
        type = pType;
        title = pTitle;
        amount = pAmount;
        campaign = pCampain;
        actions = pAction;
        transid = pTransId;
        notificationId = pNotificationId;
    }
}
