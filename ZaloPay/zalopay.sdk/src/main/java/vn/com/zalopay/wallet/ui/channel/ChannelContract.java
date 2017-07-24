package vn.com.zalopay.wallet.ui.channel;

import android.os.Bundle;

import com.zalopay.ui.widget.dialog.listener.OnProgressDialogTimeoutListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import vn.com.zalopay.wallet.dialog.ZPWResultCallBackListener;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.IContract;

/*
 * Created by chucvv on 6/12/17.
 */

public interface ChannelContract extends IContract {
    interface IView extends IContract {

        void showLoading(String pTitle, OnProgressDialogTimeoutListener timeoutListener);

        void showDialogManyOption(ZPWOnSweetDialogListener pListener);

        void showQuitConfirm(String message, ZPWOnEventConfirmDialogListener pListener);

        void showConfirmDialog(String pMessage, final String pButtonLeftText, final String pButtonRightText, ZPWOnEventConfirmDialogListener pListener);

        void showInfoDialog(String pMessage, String pLeftButton, ZPWOnEventDialogListener zpwOnEventDialogListener);

        String getFailMess();

        void renderOrderInfo(AbstractOrder order);

        void renderTotalAmountAndFee(double total_amount, double fee);

        void disablePaymentButton();

        void updateCardNumberFont();

        void changeBgPaymentButton(boolean finalStep);

        void visibleOrderInfo(boolean visible);

        void showMaintenanceServiceDialog(String message);

        void showMapBankDialog(Bundle args, ZPWResultCallBackListener pZpwResultCallBackListener);
    }
}
