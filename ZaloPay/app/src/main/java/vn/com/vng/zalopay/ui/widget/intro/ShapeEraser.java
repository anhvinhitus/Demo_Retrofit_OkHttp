package vn.com.vng.zalopay.ui.widget.intro;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
class ShapeEraser {

    protected Target target;
    private int padding;

    public ShapeEraser(Target target) {
        this.target = target;
    }

    public ShapeEraser(Target target, int padding) {
        this(target);
        this.padding = padding;
    }

    public void draw(Canvas canvas, Paint eraser) {

    }

    public Target getTarget() {
        return target;
    }

    public int getPadding() {
        return padding;
    }


}
