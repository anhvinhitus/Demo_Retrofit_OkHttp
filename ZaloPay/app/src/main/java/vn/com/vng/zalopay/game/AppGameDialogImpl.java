package vn.com.vng.zalopay.game;

import android.content.Context;

import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.ITimeoutLoadingListener;
import vn.com.zalopay.game.businnesslogic.provider.dialog.IDialog;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

/**
 * Created by admin on 8/30/16.
 */
public class AppGameDialogImpl implements IDialog
{
    @Override
    public void showInfoDialog(Context pContext, String pMessage, String pButtonText, int pDialogType, final IDialogListener pListener) {
    }

    @Override
    public void showLoadingDialog(Context pContext, ITimeoutLoadingListener pListener) {

    }

    @Override
    public void hideLoadingDialog() {

    }
}
