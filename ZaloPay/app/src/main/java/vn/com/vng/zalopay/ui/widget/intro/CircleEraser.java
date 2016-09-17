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
        this(target, 0);
    }

    public CircleEraser(Target target, int padding) {
        super(target, padding);
        this.calRadius(padding);
        circlePoint = target.getPoint();
    }

    public int getRadius() {
        return this.radius;
    }

    public Point getPoint() {
        return this.circlePoint;
    }

    @Override
    public void draw(Canvas canvas, Paint eraser) {
        canvas.drawCircle((float) this.circlePoint.x, (float) this.circlePoint.y, (float) this.radius, eraser);
    }

    private void calRadius(int padding) {
        int side = Math.max(this.target.getRect().width() / 2, this.target.getRect().height() / 2);
        this.radius = side + padding;
    }
}
