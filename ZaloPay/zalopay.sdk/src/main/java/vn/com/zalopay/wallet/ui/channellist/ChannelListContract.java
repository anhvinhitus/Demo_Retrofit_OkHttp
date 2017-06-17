package vn.com.zalopay.wallet.ui.channellist;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.IContract;

/**
 * Created by chucvv on 6/12/17.
 */

public interface ChannelListContract extends IContract {
    interface IView extends IContract {
        void setTitle(String title);

        void updateDefaultTitle();

        void enableConfirmButton(boolean pEnable);

        void renderAppInfo(String appName);

        void renderOrderInfo(AbstractOrder order);

        void renderOrderFee(double total_amount, double fee);

        void renderDynamicItemDetail(List<NameValuePair> nameValuePair);

        void onBindingChannel(ChannelListAdapter pChannelAdapter);

        void showAppInfoNotFoundDialog();

        void showForceUpdateLevelDialog();

        void showUpdateLevelDialog(String message, String btnCloseText, ZPWOnEventConfirmDialogListener pListener);

        void showWarningLinkCardBeforeWithdraw();

        void showOpenSettingNetwokingDialog(ZPWPaymentOpenNetworkingDialogListener pListener);

        void showSupportBankVersionDialog(String pMessage);

        void showSelectionBankAccountDialog();

        void showRetryDialog(String pMessage, ZPWOnEventConfirmDialogListener pListener);

        void showSnackBar(String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener);
    }
}
