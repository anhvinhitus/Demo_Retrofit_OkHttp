package vn.com.zalopay.wallet.ui.channellist;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.IContract;

/**
 * Created by chucvv on 6/12/17.
 */

public interface ChannelListContract extends IContract {
    interface IView extends IContract {
        void setTitle(String title);

        void renderAppInfo(AppInfo pAppInfo);

        void renderOrderInfo(UserInfo userInfo, AbstractOrder order, @TransactionType int transtype);

        void onBindingChannel(ChannelListAdapter pChannelAdapter);

        void showAppInfoNotFoundDialog();

        void showForceUpdateLevelDialog();

        void showUpdateLevelDialog(String message, String btnCloseText, ZPWOnEventConfirmDialogListener pListener);

        void showWarningLinkCardBeforeWithdraw();

        void showOpenSettingNetwokingDialog(ZPWPaymentOpenNetworkingDialogListener pListener);

        void showSupportBankVersionDialog(String pMessage);

        void showSelectionBankAccountDialog();
    }
}
