package vn.zalopay.promotion;

import android.content.Context;

public interface IPromotionResult {
    void onReceiverNotAvailable();

    void onNavigateToAction(Context pContext, PromotionEvent pPromotionEvent);
}
