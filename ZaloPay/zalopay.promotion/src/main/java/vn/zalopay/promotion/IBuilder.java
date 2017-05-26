package vn.zalopay.promotion;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public interface IBuilder {

    PromotionEvent getPromotion();

    IInteractPromotion getInteractPromotion();

    IBuilder setInteractPromotion(IInteractPromotion promotionListener);

    View getView();

    IBuilder setView(View pView);

    IBuilder setPromotion(PromotionEvent promotionEvent);

    UIBottomSheetDialog.IRender build();

    void release();
}
