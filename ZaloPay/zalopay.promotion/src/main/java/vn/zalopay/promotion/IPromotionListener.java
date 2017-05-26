package vn.zalopay.promotion;

import android.content.Context;

public interface IPromotionListener {
    void onReceiverNotAvailable();

    void onPromotionAction(Context pContext, PromotionEvent pPromotionEvent);

    void onClose();
}
