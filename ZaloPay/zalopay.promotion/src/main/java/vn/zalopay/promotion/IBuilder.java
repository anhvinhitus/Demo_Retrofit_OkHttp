package vn.zalopay.promotion;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public interface IBuilder {

    PromotionEvent getPromotion();

    IPromotionListener getPromotionListener();

    IBuilder setPromotionListener(IPromotionListener promotionListener);

    View getView();

    IBuilder setView(View pView);

    IBuilder setPromotionEvent(PromotionEvent promotionEvent);

    UIBottomSheetDialog.IRender build();

    void release();
}
