package vn.com.vng.zalopay.promotion;

public class PromotionAction {
    public String title;

    @ActionType
    public int action;

    public PromotionAction(String pTitle, int pAction) {
        title = pTitle;
        action = pAction;
    }
}
