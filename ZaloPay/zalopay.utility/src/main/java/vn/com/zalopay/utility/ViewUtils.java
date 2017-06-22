package vn.com.zalopay.utility;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

public class ViewUtils {

    public static void correctTextView(TextView textView, int desiredWidth) {
        Paint paint = new Paint();
        Rect bounds = new Rect();
        paint.setTypeface(textView.getTypeface());
        float textSize = textView.getTextSize();
        paint.setTextSize(textSize);
        String text = textView.getText().toString();
        paint.getTextBounds(text, 0, text.length(), bounds);
        while (bounds.width() < desiredWidth) {
            textSize++;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }
        while (bounds.width() > desiredWidth) {
            textSize--;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    /***
     * @param pContext
     * @param pView
     * @param pPercent
     */
    public static int resizeViewByPercent(Context pContext, View pView, float pPercent) {
        try {
            Display display = ((WindowManager) pContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int densityDpi = display.getWidth();
            ViewGroup.LayoutParams params = pView.getLayoutParams();
            params.width = (int) (densityDpi * pPercent);
            params.height = getHeightByRate(params.width);
            pView.setLayoutParams(params);
            return params.width;
        } catch (Exception e) {
        }
        return 0;
    }

    /***
     * set Height view with (width and height old)
     * @param pNewWidth
     * @return
     */
    public static int getHeightByRate(int pNewWidth) {
        float rate = 1.6f;
        return (int) (pNewWidth / rate);
    }
}
