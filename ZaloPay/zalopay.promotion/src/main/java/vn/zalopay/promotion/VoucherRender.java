package vn.zalopay.promotion;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import timber.log.Timber;
import vn.zalopay.promotion.model.PromotionEvent;
import vn.zalopay.promotion.model.VoucherEvent;

public class VoucherRender extends PromotionRender {
    public VoucherRender(IBuilder pBuilder) {
        super(pBuilder);
    }

    public static IBuilder getBuilder() {
        return new VoucherBuilder();
    }

    @Override
    public void render(final Context pContext) {
        if (mBuilder == null) {
            return;
        }
        final PromotionEvent promotionEvent = mBuilder.getPromotion();
        if (!(promotionEvent instanceof VoucherEvent)) {
            return;
        }
        View view = mBuilder.getView();
        if (view == null) {
            return;
        }
        try {
            renderView(pContext, view, promotionEvent);
            TextView tvVoucherCode = (TextView) view.findViewById(R.id.promotion_cash_back_tv_amount);
            tvVoucherCode.setTextSize(pContext.getResources().getDimension(R.dimen.textsize_normal));
            tvVoucherCode.setText(((VoucherEvent) promotionEvent).vouchercode);
            view.findViewById(R.id.currency_unit_tv).setVisibility(View.GONE);
        } catch (Exception e) {
            Timber.w(e, "Exception render voucher view");
        }
    }
}
