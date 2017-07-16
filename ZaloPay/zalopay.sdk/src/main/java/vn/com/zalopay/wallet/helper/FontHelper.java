package vn.com.zalopay.wallet.helper;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.dao.CFontManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;

/**
 * Created by chucvv on 6/21/17.
 */

public class FontHelper {
    /***
     * apply font for all view on screen.
     */
    public static void overrideFont(ViewGroup viewGroup) {
        overrideFonts(viewGroup, GlobalData.getStringResource(RS.string.sdk_font_regular));
    }

    public static void applyFont(View pView, String pFontName) {
        Typeface tf = CFontManager.getInstance().loadFont(pFontName);
        if (tf != null) {
            if (pView instanceof TextView)
                ((TextView) pView).setTypeface(tf);
            else if (pView instanceof VPaymentDrawableEditText)
                ((VPaymentDrawableEditText) pView).setTypeface(tf);
        }
    }

    public static void overrideFonts(final View pView, String pFontName) {
        try {
            if (pView instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) pView;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(child, pFontName);
                }
            } else if (pView.getId() != R.id.front_card_number &&
                    ((pView instanceof TextView) || pView instanceof VPaymentDrawableEditText || pView instanceof VPaymentValidDateEditText)) {
                Typeface typeFace = CFontManager.getInstance().loadFont(pFontName);
                if (typeFace != null) {
                    if (pView instanceof TextView) {
                        ((TextView) pView).setTypeface(typeFace);
                    } else {
                        ((VPaymentEditText) pView).setTypeface(typeFace);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static int getFontSizeAmount(double amount){
        if(amount < 100000){
            return R.dimen.sdk_text_size_amount_supper;
        }else if(amount < 1000000){
            return R.dimen.sdk_text_size_amount_hundered;
        }else {
            return R.dimen.sdk_text_size_amount_thousand;
        }
    }
}
