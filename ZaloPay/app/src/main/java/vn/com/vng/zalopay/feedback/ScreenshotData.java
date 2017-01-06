package vn.com.vng.zalopay.feedback;

import android.graphics.Bitmap;

/**
 * Created by hieuvm on 1/6/17.
 */

final class ScreenshotData {
    public String mUrl;
    public Bitmap mBitmap;

    public ScreenshotData(String mUrl) {
        this.mUrl = mUrl;
        this.mBitmap = null;
    }

    public ScreenshotData(Bitmap bitmap) {
        this.mBitmap = bitmap;
        this.mUrl = null;
    }
}
