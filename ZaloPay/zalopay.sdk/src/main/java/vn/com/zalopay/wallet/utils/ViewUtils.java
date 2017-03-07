package vn.com.zalopay.wallet.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;

public class ViewUtils {
    public static void correctWidth(TextView textView, int desiredWidth) {
        Paint paint = new Paint();
        Rect bounds = new Rect();

        paint.setTypeface(textView.getTypeface());

        float textSize = textView.getTextSize();

        paint.setTextSize(textSize);
        String text = textView.getText().toString();

        paint.getTextBounds(text, 0, text.length(), bounds);

        while (bounds.width() < desiredWidth) {
            textSize++;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }

        while (bounds.width() > desiredWidth) {
            textSize--;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

    }

    /***
     * @param pContext
     * @param pView
     * @param pPercent
     */
    public static int setCardViewSize(Context pContext, View pView, float pPercent) {

        try {
            Display display = ((WindowManager) pContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            int densityDpi = display.getWidth();
            ViewGroup.LayoutParams params = pView.getLayoutParams();

            params.width = (int) (densityDpi * pPercent);
            params.height = getCardViewHeight(params.width);
            pView.setLayoutParams(params);

            return params.width;

        } catch (Exception e) {
            Log.e("===ViewUtils===setCardViewSize===", e);
        }

        return 0;
    }

    /***
     * set Height view with (width and height old)
     *
     * @param pNewWidth
     * @return
     */
    public static int getCardViewHeight(int pNewWidth) {
        float rate = 1.4f;

        try {
            rate = Float.parseFloat(GlobalData.getStringResource(RS.string.cardview_rate));

            Log.d("===getCardViewHeight===", String.valueOf((int) (pNewWidth / 1.4f)));

        } catch (Exception e) {
            Log.e("===getCardViewHeight===", e);

            rate = 1.4f;
        }


        return (int) (pNewWidth / rate);

    }

    public static void setTextInputLayoutHint(EditText pEditext, String pMessage, Context pContext) {
        if (pEditext == null) {
            Log.d("setTextInputLayoutHintError", "===pEditext=NULL===");
            return;
        }

        if (pEditext instanceof VPaymentEditText && ((VPaymentEditText) pEditext).getTextInputLayout() instanceof TextInputLayout) {

            try {
                TextInputLayout textInputLayout = ((VPaymentEditText) pEditext).getTextInputLayout();

                int color = pContext.getResources().getColor(R.color.color_primary);

                int textColor = pContext.getResources().getColor(R.color.text_color);

                Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
                fDefaultTextColor.setAccessible(true);
                fDefaultTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{textColor}));

                Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
                fFocusedTextColor.setAccessible(true);
                fFocusedTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));

                int paddingLeft = pEditext.getPaddingLeft();
                int paddingTop = pEditext.getPaddingTop();
                int paddingRight = pEditext.getPaddingRight();
                int paddingBottom = pEditext.getPaddingBottom();

                pEditext.setBackground(pContext.getResources().getDrawable(R.drawable.txt_bottom_default_style));

                //restore padding
                pEditext.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

                textInputLayout.refreshDrawableState();

                textInputLayout.setHint(!TextUtils.isEmpty(pMessage) ? pMessage : (textInputLayout.getTag() != null ? textInputLayout.getTag().toString() : null));

            } catch (Exception e) {
                Log.e(pContext, e);
            }
        }
    }

    public static void setTextInputLayoutHintError(EditText pEditext, String pError, Context pContext) {
        if (pEditext == null) {
            Log.d("setTextInputLayoutHintError", "===pEditext=NULL===");
            return;
        }

        if (pEditext instanceof VPaymentEditText && ((VPaymentEditText) pEditext).getTextInputLayout() instanceof TextInputLayout) {
            try {
                TextInputLayout textInputLayout = ((VPaymentEditText) pEditext).getTextInputLayout();

                int color = pContext.getResources().getColor(R.color.holo_red_light);

                Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
                fDefaultTextColor.setAccessible(true);
                fDefaultTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));

                Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
                fFocusedTextColor.setAccessible(true);
                fFocusedTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));

                int paddingLeft = pEditext.getPaddingLeft();
                int paddingTop = pEditext.getPaddingTop();
                int paddingRight = pEditext.getPaddingRight();
                int paddingBottom = pEditext.getPaddingBottom();

                pEditext.setBackground(pContext.getResources().getDrawable(R.drawable.txt_bottom_error_style));

                //restore padding
                pEditext.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

                textInputLayout.refreshDrawableState();

                textInputLayout.setHint(pError);

            } catch (Exception e) {
                Log.e(pContext, e);
            }
        }
    }
}
