package vn.zalopay.promotion;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public abstract class PromotionRender implements UIBottomSheetDialog.IRender {
    protected IBuilder mBuilder;

    public PromotionRender(PromotionBuilder pBuilder) {
        mBuilder = pBuilder;
    }

    public static PromotionBuilder getBuilder() {
        return new PromotionBuilder();
    }

    @Override
    public void render(Context context) {

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
        IPromotionListener promotionListener = mBuilder.getPromotionListener();
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
