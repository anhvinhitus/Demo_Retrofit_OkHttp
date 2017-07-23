package vn.com.zalopay.wallet.helper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;

/*
 * Created by chucvv on 6/18/17.
 */

public class RenderHelper {
    public static Spanned getHtml(String html) {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY); // for 24 api and more
        } else {
            return Html.fromHtml(html); // or for older api
        }
    }

    public static List<View> genDynamicItemDetail(Context context, List<NameValuePair> nameValuePairList) {
        if (nameValuePairList == null) {
            return null;
        }
        List<View> views = new ArrayList<>();
        for (int i = 0; i < nameValuePairList.size(); i++) {
            NameValuePair nameValuePair = nameValuePairList.get(i);
            if(nameValuePair == null){
                continue;
            }
            RelativeLayout relativeLayout = new RelativeLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = (int) context.getResources().getDimension(R.dimen.sdk_margin_left_right);
            relativeLayout.setLayoutParams(params);
            if (!TextUtils.isEmpty(nameValuePair.key)) {
                TextView name_txt = new TextView(context);
                name_txt.setTextColor(ContextCompat.getColor(context, (R.color.text_color_grey)));
                name_txt.setText(nameValuePair.key);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                name_txt.setLayoutParams(layoutParams);
                relativeLayout.addView(name_txt);
            }
            if (!TextUtils.isEmpty(nameValuePair.value)) {
                TextView value_txt = new TextView(context);
                value_txt.setTextColor(ContextCompat.getColor(context, (R.color.text_color_grey)));
                value_txt.setText(nameValuePair.value);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                value_txt.setLayoutParams(layoutParams);
                relativeLayout.addView(value_txt);
            }
            views.add(relativeLayout);
        }
        return views;
    }

    public static void setTextInputLayoutHint(EditText pEditext, String pMessage, Context pContext) {
        if (pEditext == null) {
            return;
        }
        if(( !(pEditext instanceof VPaymentEditText) || ((VPaymentEditText) pEditext).getTextInputLayout() == null)){
            return;
        }

        try {
            TextInputLayout textInputLayout = ((VPaymentEditText) pEditext).getTextInputLayout();
            if(textInputLayout == null){
                return;
            }

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
            Timber.d(e);
        }
    }

    public static void setTextInputLayoutHintError(EditText pEditext, String pError, Context pContext) {
        if (pEditext == null) {
            return;
        }
        if(( !(pEditext instanceof VPaymentEditText) || ((VPaymentEditText) pEditext).getTextInputLayout() == null)){
            return;
        }
        try {
            TextInputLayout textInputLayout = ((VPaymentEditText) pEditext).getTextInputLayout();
            if(textInputLayout == null){
                return;
            }
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
            Timber.d(e);
        }
    }
}
