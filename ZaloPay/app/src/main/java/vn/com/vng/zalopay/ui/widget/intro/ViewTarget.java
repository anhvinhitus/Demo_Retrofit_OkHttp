package vn.com.vng.zalopay.ui.widget.intro;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class ViewTarget implements Target {

    private WeakReference<View> mView;

    public ViewTarget(View view) {
        this.mView = new WeakReference<>(view);
    }

    public Point getPoint() {
        View view = this.mView.get();
        if (view == null) {
            return new Point();
        }

        int[] location = new int[2];
        view.getLocationInWindow(location);
        return new Point(location[0] + view.getWidth() / 2, location[1] + view.getHeight() / 2);
    }

    public Rect getRect() {
        View view = this.mView.get();
        if (view == null) {
            return new Rect();
        }

        int[] location = new int[2];
        view.getLocationInWindow(location);
        return new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
    }

    public View getView() {
        return mView.get();
    }
}
