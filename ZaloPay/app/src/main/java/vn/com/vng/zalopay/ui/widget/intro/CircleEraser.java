package vn.com.vng.zalopay.ui.widget.intro;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class CircleEraser extends ShapeEraser {

    private int radius;
    private Point circlePoint;

    public CircleEraser(Target target) {
        super(target);
    }

    public int getRadius() {
        return this.radius;
    }

    public Point getPoint() {
        return this.circlePoint;
    }

    @Override
    public void draw(Canvas canvas, Paint eraser) {

    }
}
