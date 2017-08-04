package vn.com.zalopay.wallet.helper;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import vn.com.zalopay.wallet.R;

/**
 * Created by SinhTT on 8/4/17.
 */

public class ToastHelper {
    public static void showToast(Context context, int layout) {
        if (context != null) {
            Toast toast = new Toast(context);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(View.inflate(context, layout, null));
            toast.show();
        }
    }

    public static void showToastUpdatePassword(Context context) {
        showToast(context, R.layout.layout_update_password_toast);
    }
}
