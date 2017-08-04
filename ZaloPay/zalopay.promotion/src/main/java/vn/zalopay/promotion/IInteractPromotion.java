package vn.zalopay.promotion;

import vn.zalopay.promotion.model.PromotionEvent;

public interface IInteractPromotion {

    void onUserInteract(PromotionEvent pPromotionEvent);

    void onClose();
}
