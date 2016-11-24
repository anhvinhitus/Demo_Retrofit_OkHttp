package vn.com.vng.zalopay.ui.widget.intro;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class RectangleEraser extends ShapeEraser {

    private Rect mRect;

    public RectangleEraser(Target target) {
        this(target, 0);
    }

    public RectangleEraser(Target target, Rect padding) {
        this(target);
        mRect = padding;
    }

    public RectangleEraser(Target target, int padding) {
        super(target, padding);
    }

    @Override
    public void draw(Canvas canvas, Paint eraser) {
        Rect rect = getTarget().getRect();
        int padding = getPadding();
        if (mRect != null) {
            rect.left -= mRect.left;
            rect.top -= mRect.top;
            rect.right += mRect.right;
            rect.bottom += mRect.bottom;
        } else {
            rect.left -= padding;
            rect.top -= padding;
            rect.right += padding;
            rect.bottom += padding;
        }
        canvas.drawRect(rect, eraser);
    }
}
