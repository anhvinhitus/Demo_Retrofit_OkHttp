package vn.zalopay.promotion;

import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public class PromotionBuilder implements IBuilder {
    protected PromotionEvent promotionEvent;
    protected IPromotionListener promotionListener;
    protected View mView;

    @Override
    public PromotionBuilder setPromotionEvent(PromotionEvent promotionEvent) {
        this.promotionEvent = promotionEvent;
        return this;
    }

    @Override
    public PromotionEvent getPromotion() {
        return promotionEvent;
    }

    @Override
    public IPromotionListener getPromotionListener() {
        return promotionListener;
    }

    @Override
    public PromotionBuilder setPromotionListener(IPromotionListener promotionListener) {
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
