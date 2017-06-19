package vn.com.zalopay.wallet.helper;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.wallet.R;

/**
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
            if (nameValuePair != null) {
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
        }
        return views;
    }
}
