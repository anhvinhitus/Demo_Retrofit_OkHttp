package vn.com.vng.zalopay.ui.widget;

import android.text.TextPaint;
import android.text.style.ClickableSpan;

/**
 * Created by AnhHieu on 8/21/16.
 */
public abstract class ClickableSpanNoUnderline extends ClickableSpan {

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}
