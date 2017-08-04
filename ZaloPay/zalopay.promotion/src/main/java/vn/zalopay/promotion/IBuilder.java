package vn.zalopay.promotion;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

import vn.zalopay.promotion.model.PromotionEvent;

public interface IBuilder {

    IResourceLoader getResourceProvider();

    IBuilder setResourceProvider(IResourceLoader resourceProvider);

    PromotionEvent getPromotion();

    IBuilder setPromotion(PromotionEvent promotionEvent);

    IInteractPromotion getInteractPromotion();

    IBuilder setInteractPromotion(IInteractPromotion promotionListener);

    View getView();

    IBuilder setView(View pView);

    UIBottomSheetDialog.IRender build();

    void release();
}
