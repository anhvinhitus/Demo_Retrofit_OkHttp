package vn.com.zalopay.game.businnesslogic.provider.dialog;

import android.app.Activity;
import android.content.Context;

import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;

/**
 * Created by admin on 8/30/16.
 */
public interface IDialog
{
    void showInfoDialog(Activity pActivity, String pMessage, String pButtonText, int pDialogType, IDialogListener pListener);
    void showLoadingDialog(Activity pActivity,ITimeoutLoadingListener pListener);
    void hideLoadingDialog();
}
