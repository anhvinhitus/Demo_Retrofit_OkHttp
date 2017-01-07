package vn.com.vng.zalopay.feedback;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by hieuvm on 1/6/17.
 */

final class ScreenshotData {
    public Uri mUrl;
    public Bitmap mBitmap;

    public ScreenshotData(Uri uri) {
        this.mUrl = uri;
        this.mBitmap = null;
    }

    public ScreenshotData(Bitmap bitmap) {
        this.mBitmap = bitmap;
        this.mUrl = null;
    }
}
