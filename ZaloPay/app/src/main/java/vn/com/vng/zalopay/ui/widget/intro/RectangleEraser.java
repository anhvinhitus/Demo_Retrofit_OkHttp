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
        super(target);
    }

    @Override
    public void draw(Canvas canvas, Paint eraser) {
        Rect rect = getTarget().getRect();
        canvas.drawRect(rect, eraser);
    }
}
