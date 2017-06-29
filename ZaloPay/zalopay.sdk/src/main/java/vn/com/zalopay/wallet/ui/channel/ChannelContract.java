package vn.com.zalopay.wallet.ui.channel;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.IContract;

/**
 * Created by chucvv on 6/12/17.
 */

public interface ChannelContract extends IContract {
    interface IView extends IContract {
        void setTitle(String title);

        void showDialogManyOption(ZPWOnSweetDialogListener pListener);

        void showQuitConfirm(String message, ZPWOnEventConfirmDialogListener pListener);

        void showConfirmDialog(String pMessage, final String pButtonLeftText, final String pButtonRightText, ZPWOnEventConfirmDialogListener pListener);

        void showInfoDialog(String pMessage, String pLeftButton, ZPWOnEventDialogListener zpwOnEventDialogListener);

        boolean visualSupportView();

        void closeSupportView();

        String getFailMess();

        void renderAppInfo(String appName);

        void renderOrderInfo(AbstractOrder order);

        void renderTotalAmountAndFee(double total_amount, double fee);

        void renderByResource(String screenName);

        void renderByResource(String screenName, DStaticViewGroup pAdditionStaticViewGroup, DDynamicViewGroup pAdditionDynamicViewGroup);

        void renderResourceAfterDelay(String screenName);

        void enableSubmitBtn();

        void setTextSubmitBtn(Long appID, String pText);

        void disableSubmitBtn();

        void updateCardNumberFont();

        void overrideFont();

        void changeBgSubmitButton(boolean finalStep);

        void marginSubmitButtonTop(boolean viewEnd);

        void visiableOrderInfo(boolean visible);

        void showMaintenanceServiceDialog(String message);
    }
}
