package vn.com.vng.zalopay.utils;

/**
 * Created by AnhHieu on 9/15/16.
 * *
 */

import android.support.annotation.DrawableRes;
import android.widget.ImageView;

public interface ImageLoader<T extends ImageView> {

    void loadImage(T target, String url, @DrawableRes int placeHolder, @DrawableRes int error, ScaleType scaleType);

    void loadImage(T target, String url);

    void loadImage(T target, @DrawableRes int resourceId);

    enum ScaleType {
        MATRIX(0),
        FIT_XY(1),
        FIT_START(2),
        FIT_CENTER(3),
        FIT_END(4),
        CENTER(5),
        CENTER_CROP(6),
        CENTER_INSIDE(7);

        ScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }
}
