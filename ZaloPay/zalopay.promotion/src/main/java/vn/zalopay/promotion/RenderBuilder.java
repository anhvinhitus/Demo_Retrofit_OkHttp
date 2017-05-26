package vn.zalopay.promotion;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public class RenderBuilder implements IBuilder {
    protected PromotionEvent promotionEvent;
    protected IPromotionListener promotionListener;
    protected View mView;

    @Override
    public RenderBuilder setPromotionEvent(PromotionEvent promotionEvent) {
        this.promotionEvent = promotionEvent;
        return this;
    }

    @Override
    public RenderBuilder setPromotionListener(IPromotionListener promotionListener) {
        this.promotionListener = promotionListener;
        return this;
    }

    @Override
    public RenderBuilder setView(View pView) {
        this.mView = pView;
        return this;
    }

    @Override
    public UIBottomSheetDialog.IRender build() {
        return new CashBackRender(this);
    }

    @Override
    public void release() {
        promotionEvent = null;
        promotionListener = null;
        mView = null;
    }
}
