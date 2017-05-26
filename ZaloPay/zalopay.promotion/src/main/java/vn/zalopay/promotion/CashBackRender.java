package vn.zalopay.promotion;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zalopay.ui.widget.UIBottomSheetDialog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;

public class CashBackRender implements UIBottomSheetDialog.IRender {
    private RenderBuilder mBuilder;

    public CashBackRender(RenderBuilder pBuilder) {
        mBuilder = pBuilder;
    }

    public static RenderBuilder getBuilder() {
        return new RenderBuilder();
    }

    @Override
    public void render(final Context pContext) {
        if (mBuilder == null) {
            return;
        }
        View view = mBuilder.mView;
        final PromotionEvent promotionEvent = mBuilder.promotionEvent;
        final IPromotionListener promotionListener = mBuilder.promotionListener;
        if (view == null || promotionEvent == null) {
            Log.d(getClass().getSimpleName(), "view or promotion is null");
            return;
        }
        TextView tvCashBackTitle = (TextView) view.findViewById(R.id.promotion_cash_back_tv_title);
        TextView tvCashBackAmount = (TextView) view.findViewById(R.id.promotion_cash_back_tv_amount);
        TextView tvCashBackCampaign = (TextView) view.findViewById(R.id.promotion_cash_back_tv_campaign);
        TextView tvCashBackAction = (TextView) view.findViewById(R.id.promotion_cash_back_tv_action);
        View promotion_cash_back_ll_submit = view.findViewById(R.id.promotion_cash_back_ll_submit);
        tvCashBackTitle.setText(promotionEvent.title);
        tvCashBackAmount.setText(formatVnCurrence(String.valueOf(promotionEvent.amount)));
        if (!TextUtils.isEmpty(promotionEvent.campaign)) {
            tvCashBackCampaign.setText(Html.fromHtml(promotionEvent.campaign));
        }
        promotion_cash_back_ll_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBuilder == null) {
                    return;
                }
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View) mBuilder.mView.getParent());
                mBottomSheetBehavior.setState(STATE_HIDDEN);
            }
        });
        if (promotionEvent.actions != null && !promotionEvent.actions.isEmpty() && !TextUtils.isEmpty(promotionEvent.actions.get(0).title)) {
            tvCashBackAction.setText(Html.fromHtml(promotionEvent.actions.get(0).title));
            tvCashBackAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (promotionListener != null && mBuilder != null) {
                        promotionListener.onPromotionAction(pContext, mBuilder.promotionEvent);
                    }
                }
            });
        }

    }

    @Override
    public View getView() {
        return mBuilder != null ? mBuilder.mView : null;
    }

    @Override
    public void OnDismiss() {
        Log.d(getClass().getSimpleName(),"OnDismiss");
        if (mBuilder == null) {
            return;
        }
        IPromotionListener promotionListener = mBuilder.promotionListener;
        if (promotionListener != null) {
            promotionListener.onClose();
        }
    }

    public String formatVnCurrence(String price) {
        NumberFormat format = new DecimalFormat("#,##0.00");
        format.setCurrency(Currency.getInstance(Locale.US));//Or default locale
        price = (!TextUtils.isEmpty(price)) ? price : "0";
        price = price.trim();
        price = format.format(Math.ceil(Double.parseDouble(price)));
        price = price.replaceAll(",", "\\.");
        if (price.endsWith(".00")) {
            int centsIndex = price.lastIndexOf(".00");
            if (centsIndex != -1) {
                price = price.substring(0, centsIndex);
            }
        }
        price = String.format("%s", price);
        return price;
    }
}
