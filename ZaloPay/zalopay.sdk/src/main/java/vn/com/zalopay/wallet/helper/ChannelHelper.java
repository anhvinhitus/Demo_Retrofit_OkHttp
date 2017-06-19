package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.constants.TransactionType;

public class ChannelHelper {
    /***
     * get icon for channel
     *
     * @param channel
     * @param pIconName
     */
    public static void inflatChannelIcon(PaymentChannel channel, String pIconName) {
        if (channel != null) {
            if (!TextUtils.isEmpty(pIconName))
                channel.channel_icon = makeCardIconNameFromBankCode(pIconName);
            else
                channel.channel_icon = String.format("ic_%d.png", channel.pmcid);

            channel.channel_next_icon = RS.drawable.ic_next;
        }
    }

    public static String makeCardIconNameFromBankCode(String pBankCode) {
        return String.format("%s.png", pBankCode);
    }

    public static int btnConfirmText(PaymentChannel channel, @TransactionType int transtype) {
        int btnTextId;
        switch (transtype) {
            case TransactionType.PAY:
                btnTextId = R.string.sdk_pay_button_confirm_txt;
                break;
            default:
                btnTextId = R.string.sdk_generic_button_confirm_txt;
        }
        if (!channel.isZaloPayChannel() && !channel.isMapCardChannel() && !channel.isBankAccountMap()) {
            btnTextId = R.string.sdk_next_button_confirm_txt;
        }
        return btnTextId;
    }

    public static int btnConfirmDrawable(PaymentChannel channel) {
        if (channel.isZaloPayChannel() || channel.isMapCardChannel() || channel.isBankAccountMap()) {
            return R.drawable.bg_btn_green_border_selector;
        } else {
            return R.drawable.bg_btn_blue_border_selector;
        }
    }
}
