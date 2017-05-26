package vn.zalopay.promotion;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public interface IBuilder {

    RenderBuilder setPromotionEvent(PromotionEvent promotionEvent);

    RenderBuilder setPromotionListener(IPromotionListener promotionListener);

    RenderBuilder setView(View pView);

    UIBottomSheetDialog.IRender build();

    void release();
}
