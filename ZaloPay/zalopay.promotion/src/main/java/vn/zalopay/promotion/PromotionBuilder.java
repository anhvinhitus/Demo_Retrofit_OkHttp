package vn.zalopay.promotion;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public abstract class PromotionBuilder implements IBuilder {
    protected PromotionEvent promotionEvent;
    protected IInteractPromotion promotionListener;
    protected View mView;

    @Override
    public PromotionBuilder setPromotion(PromotionEvent promotionEvent) {
        this.promotionEvent = promotionEvent;
        return this;
    }

    @Override
    public PromotionEvent getPromotion() {
        return promotionEvent;
    }

    @Override
    public IInteractPromotion getInteractPromotion() {
        return promotionListener;
    }

    @Override
    public PromotionBuilder setInteractPromotion(IInteractPromotion promotionListener) {
        this.promotionListener = promotionListener;
        return this;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public PromotionBuilder setView(View pView) {
        this.mView = pView;
        return this;
    }

    @Override
    public void release() {
        promotionEvent = null;
        promotionListener = null;
        mView = null;
    }
}
