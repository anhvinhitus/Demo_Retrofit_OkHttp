package vn.zalopay.promotion;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zalopay.ui.widget.UIBottomSheetDialog;

import vn.zalopay.promotion.model.PromotionEvent;

import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;

public abstract class PromotionRender implements UIBottomSheetDialog.IRender {
    IBuilder mBuilder;

    PromotionRender(IBuilder pBuilder) {
        mBuilder = pBuilder;
    }

    @Override
    public View getView() {
        return mBuilder != null ? mBuilder.getView() : null;
    }

    @Override
    public void OnDismiss() {
        if (mBuilder == null) {
            return;
        }
        IInteractPromotion promotionListener = mBuilder.getInteractPromotion();
        if (promotionListener != null) {
            promotionListener.onClose();
        }
    }

    void renderView(Context pContext, View pView, PromotionEvent promotionEvent) throws Exception {
        if (mBuilder == null || pView == null) {
            return;
        }
        final IInteractPromotion promotionListener = mBuilder.getInteractPromotion();
        final IResourceLoader resourceProvider = mBuilder.getResourceProvider();
        if (promotionEvent == null) {
            return;
        }
        TextView tvCashBackTitle = (TextView) pView.findViewById(R.id.promotion_cash_back_tv_title);
        TextView tvCashBackCampaign = (TextView) pView.findViewById(R.id.promotion_cash_back_tv_campaign);
        TextView tvCashBackAction = (TextView) pView.findViewById(R.id.promotion_cash_back_tv_action);
        View promotion_cash_back_ll_submit = pView.findViewById(R.id.promotion_cash_back_ll_submit);
        ImageView imCashBackTopIcon = (ImageView) pView.findViewById(R.id.promotion_cash_back_iv_top);
        ImageView imCashBackFire = (ImageView) pView.findViewById(R.id.promotion_cash_back_iv_fire_popup);
        tvCashBackTitle.setText(promotionEvent.title);
        if (!TextUtils.isEmpty(promotionEvent.campaign)) {
            tvCashBackCampaign.setText(Html.fromHtml(promotionEvent.campaign));
        }
        promotion_cash_back_ll_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBuilder == null || mBuilder.getView() == null) {
                    return;
                }
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View) mBuilder.getView().getParent());
                mBottomSheetBehavior.setState(STATE_HIDDEN);
            }
        });
        if (promotionEvent.actions != null && !promotionEvent.actions.isEmpty() && !TextUtils.isEmpty(promotionEvent.actions.get(0).title)) {
            tvCashBackAction.setText(Html.fromHtml(promotionEvent.actions.get(0).title));
            tvCashBackAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (promotionListener != null && mBuilder != null) {
                        promotionListener.onUserInteract(mBuilder.getPromotion());
                    }
                }
            });
        }
        //load image from resource folder app 1
        if (resourceProvider != null) {
            resourceProvider.loadImage(pContext, imCashBackTopIcon, R.string.img_top_cashback);
            resourceProvider.loadImage(pContext, imCashBackFire, R.string.img_fire_popup);
        }
    }
}
