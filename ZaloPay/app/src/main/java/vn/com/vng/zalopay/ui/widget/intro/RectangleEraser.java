package vn.com.vng.zalopay.ui.widget.intro;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class RectangleEraser extends ShapeEraser {

    public RectangleEraser(Target target) {
        this(target, 0);
    }

    public RectangleEraser(Target target, int padding) {
        super(target, padding);
    }

    @Override
    public void draw(Canvas canvas, Paint eraser) {
        Rect rect = getTarget().getRect();
        int padding = getPadding();
        // rect.inset(padding, padding);
        rect.left -= padding;
        rect.top -= padding;
        rect.right += padding;
        rect.bottom += padding;

        canvas.drawRect(rect, eraser);
    }
}
