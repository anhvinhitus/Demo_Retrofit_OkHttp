package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.TextUtils;

import com.zalopay.ui.widget.IconFontDrawable;
import com.zalopay.ui.widget.iconfont.IconFontHelper;
import com.zalopay.ui.widget.iconfont.IconFontInfo;

import timber.log.Timber;

/**
 * Created by longlv on 4/3/17.
 * Icon font drawable with new icon at top right corner.
 */

public class BottomNavigationDrawable extends IconFontDrawable {

    private String mTextSubIcon;
    private TextPaint mPaintSubIcon;
    private int mSizeSubIcon;

    public BottomNavigationDrawable(Context context) {
        super(context);
        mPaintSubIcon = new TextPaint();
        mPaintSubIcon.setStyle(Paint.Style.STROKE);
        mPaintSubIcon.setTextAlign(Paint.Align.RIGHT);
        mPaintSubIcon.setUnderlineText(false);
        mPaintSubIcon.setColor(Color.RED);
        mPaintSubIcon.setAntiAlias(true);
        mTextSubIcon = null;
    }

    public BottomNavigationDrawable setSubIcon(String iconName) {
        Timber.d("setSubIcon iconName: %s", iconName);
        if (TextUtils.isEmpty(iconName)) {
            mTextSubIcon = "";
        } else {
            IconFontInfo iconFontInfo = IconFontHelper.getInstance().getIconFontInfo(iconName);
            if (iconFontInfo == null) {
                Timber.w("Set sub icon fail, not found info of iconName: %s", iconName);
                mTextSubIcon = "";
            } else {
                mTextSubIcon = iconFontInfo.code;
            }
        }
        return this;
    }

    public IconFontDrawable setPxSizeSubIcon(int dimenRes) {
        this.mSizeSubIcon = mContext.getResources().getDimensionPixelSize(dimenRes);
        return this;
    }

    @Override
    public BottomNavigationDrawable setIcon(Integer iconResource) {
        super.setIcon(iconResource);
        return this;
    }

    @Override
    public BottomNavigationDrawable setResourcesColor(int colorRes) {
        super.setResourcesColor(colorRes);
        return this;
    }

    @Override
    public BottomNavigationDrawable setResourcesSize(int dimenRes) {
        super.setResourcesSize(dimenRes);
        return this;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        drawNewIcon(canvas);
    }

    private void drawNewIcon(Canvas canvas) {
        if (TextUtils.isEmpty(mTextSubIcon)) {
            return;
        }

        refreshTypefaceSubIcon();
        mPaintSubIcon.setTextSize(mSizeSubIcon);

        Rect textBounds = new Rect();
        mPaintSubIcon.getTextBounds(mTextSubIcon, 0, 1, textBounds);
        canvas.drawText(mTextSubIcon,
                getBounds().width() + textBounds.width() / 2,
                textBounds.height() / 2,
                mPaintSubIcon);
    }

    private void refreshTypefaceSubIcon() {
        if (mPaintSubIcon.getTypeface() != getZaloPayTypeface()) {
            setTypefaceSubIcon(getZaloPayTypeface());
        }
    }

    private void setTypefaceSubIcon(Typeface typeface) {
        if (typeface != null) {
            mPaintSubIcon.setTypeface(typeface);
        }
    }
}
