package vn.com.vng.zalopay.ui.widget;

import android.text.TextPaint;
import android.text.style.ClickableSpan;

/**
 * Created by AnhHieu on 8/21/16.
 * *
 */
public abstract class ClickableSpanNoUnderline extends ClickableSpan {

    private int mLinkColor = 0;

    protected ClickableSpanNoUnderline() {
    }

    protected ClickableSpanNoUnderline(int mLinkColor) {
        super();
        this.mLinkColor = mLinkColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        if (mLinkColor != 0) {
            ds.setColor(mLinkColor);
        } else {
            super.updateDrawState(ds);
        }
        ds.setUnderlineText(false);
    }
}
