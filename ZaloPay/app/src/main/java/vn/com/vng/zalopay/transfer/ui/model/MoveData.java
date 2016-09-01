package vn.com.vng.zalopay.transfer.ui.model;

import android.view.View;

/**
 * Created by AnhHieu on 8/31/16.
 * *
 */
public class MoveData {
    public int left;
    public int top;
    public float width;
    public float height;
    public int duration = 300;

    public MoveData() {
    }

    public MoveData(View view) {
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        left = screenLocation[0];
        top = screenLocation[1];
        width = view.getWidth();
        height = view.getHeight();
    }
}
