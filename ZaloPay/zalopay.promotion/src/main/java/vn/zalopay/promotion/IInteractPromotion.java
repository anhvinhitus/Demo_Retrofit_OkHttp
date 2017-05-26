package vn.zalopay.promotion;

public interface IInteractPromotion {

    void onUserInteract(PromotionEvent pPromotionEvent);

    void onClose();
}
