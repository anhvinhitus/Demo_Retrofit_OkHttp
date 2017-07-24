package vn.com.zalopay.wallet.ui.channellist;

import java.util.List;

import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.IContract;

/**
 * Created by chucvv on 6/12/17.
 */

public interface ChannelListContract extends IContract {
    interface IView extends IContract {
        void setTitle(String title);

        void updateDefaultTitle();

        void renderAppInfo(String appName);

        void renderOrderInfo(AbstractOrder order);

        void renderTotalAmountAndFee(double total_amount, double fee);

        void renderDynamicItemDetail(List<NameValuePair> nameValuePair);

        void onBindingChannel(ChannelListAdapter pChannelAdapter);

        void showAppInfoNotFoundDialog();

        void showWarningLinkCardBeforeWithdraw();

        void showSupportBankVersionDialog(String pMessage);

        void disableConfirmButton();

        void showQuitConfirm();

        void scrollToPos(int position);

        void enablePaymentButton(int buttonTextId, int bgResourceId);

        void switchToResultScreen(StatusResponse pResponse, boolean pShouldShowFingerPrintToast) throws Exception;

        ChannelListAdapter initChannelListAdapter(long amount, UserInfo userInfo, int userLevel, int transtype);
    }
}
