package vn.zalopay.promotion;

import android.content.Context;

import vn.zalopay.promotion.model.PromotionEvent;

public interface IPromotionResult {
    void onReceiverNotAvailable();

    void onNavigateToAction(Context pContext, PromotionEvent pPromotionEvent);
}
