package vn.zalopay.promotion;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;

public class CashBackRender extends PromotionRender {
    public CashBackRender(IBuilder pBuilder) {
        super(pBuilder);
    }

    public static IBuilder getBuilder() {
        return new CashBackBuilder();
    }

    @Override
    public void render(final Context pContext) {
        if (mBuilder == null) {
            return;
        }
        View view = mBuilder.getView();
        final PromotionEvent promotionEvent = mBuilder.getPromotion();
        final IInteractPromotion promotionListener = mBuilder.getInteractPromotion();
        final IResourceLoader resourceProvider = mBuilder.getResourceProvider();
        if (view == null || promotionEvent == null) {
            Log.d(getClass().getSimpleName(), "view or promotion is null");
            return;
        }
        TextView tvCashBackTitle = (TextView) view.findViewById(R.id.promotion_cash_back_tv_title);
        TextView tvCashBackAmount = (TextView) view.findViewById(R.id.promotion_cash_back_tv_amount);
        TextView tvCashBackCampaign = (TextView) view.findViewById(R.id.promotion_cash_back_tv_campaign);
        TextView tvCashBackAction = (TextView) view.findViewById(R.id.promotion_cash_back_tv_action);
        View promotion_cash_back_ll_submit = view.findViewById(R.id.promotion_cash_back_ll_submit);
        ImageView imCashBackTopIcon = (ImageView) view.findViewById(R.id.promotion_cash_back_iv_top);
        ImageView imCashBackFire = (ImageView) view.findViewById(R.id.promotion_cash_back_iv_fire_popup);
        tvCashBackTitle.setText(promotionEvent.title);
        tvCashBackAmount.setText(formatVnCurrence(String.valueOf(promotionEvent.amount)));
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
            resourceProvider.loadImage(pContext,imCashBackTopIcon, R.string.img_top_cashback);
            resourceProvider.loadImage(pContext,imCashBackFire, R.string.img_fire_popup);
        }
    }
}
