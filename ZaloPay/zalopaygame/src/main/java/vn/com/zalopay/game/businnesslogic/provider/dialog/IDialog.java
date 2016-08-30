package vn.com.zalopay.game.businnesslogic.provider.dialog;

import android.content.Context;

import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;

/**
 * Created by admin on 8/30/16.
 */
public interface IDialog
{
    void showInfoDialog(Context pContext, String pMessage,String pButtonText,int pDialogType,IDialogListener pListener);
    void showLoadingDialog(Context pContext,ITimeoutLoadingListener pListener);
    void hideLoadingDialog();
}
