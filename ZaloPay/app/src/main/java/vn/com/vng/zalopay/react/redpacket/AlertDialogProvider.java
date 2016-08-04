package vn.com.vng.zalopay.react.redpacket;

import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by longlv on 20/07/2016.
 * Define methods using to show alert dialog
 */
public interface AlertDialogProvider {
    void showLoading(Context context);
    void hideLoading();
    void showWarningAlertDialog(Context context, String contentText, String cancelText, String confirmText, DialogInterface.OnCancelListener cancelClick, DialogInterface.OnClickListener confirmClick);
}
