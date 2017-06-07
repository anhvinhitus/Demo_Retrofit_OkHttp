package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;

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
}
