package vn.com.zalopay.wallet.utils;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

/**
 * Created by cpu11326 on 26/10/2016.
 */

public class LayoutUtils {
    private static int keyboardHeightPrevious;

    /***
     * check Keyboard above view
     *
     * @param pView
     * @return
     */
    private static Boolean isSoftKeyBoardAboveView(View pView, int pKeyBoardCoordinateTop) {
        Point point = getPointOfView(pView);
        Log.i("ButtonXY", String.valueOf(point.x) + "-" + String.valueOf(point.y));
        if ((pView.getHeight() + point.y) < pKeyBoardCoordinateTop)
            return false;
        return true;
    }

    /***
     * @param pView
     * @return
     */
    private static Point getPointOfView(View pView) {
        int[] location = new int[2];
        pView.getLocationInWindow(location);
        return new Point(location[0], location[1]);
    }

    /***
     * @param parentLayout
     */
    public static void setButtonAlwaysAboveKeyboards(final View parentLayout, final View childAboveView, final View childBelowView) {
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();

                parentLayout.getWindowVisibleDisplayFrame(r);

                int screenHeight = parentLayout.getRootView().getHeight();
                int keyboardHeight = screenHeight - (r.bottom);

                vn.com.zalopay.wallet.utils.Log.i("keyboardHeight", String.valueOf(keyboardHeight));

                vn.com.zalopay.wallet.utils.Log.i("keyboardY", String.valueOf(r.bottom));

                Point point = getPointOfView(childBelowView);
                vn.com.zalopay.wallet.utils.Log.i("ButtonX,Y+h", String.valueOf(point.x) + "-" + String.valueOf(point.y + childBelowView.getHeight()));

                if (keyboardHeight != keyboardHeightPrevious) {
                    // default. None keyboard.
                    LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f);
                    childAboveView.setLayoutParams(scrollParams);
                    keyboardHeightPrevious = keyboardHeight;
                    return;
                }

                if (keyboardHeight > 200 && isSoftKeyBoardAboveView(childBelowView, screenHeight - keyboardHeight)) {
                    // init SoftKeyBoard
                    LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    childAboveView.setLayoutParams(scrollParams);
                    return;
                }
            }
        });
    }
}
