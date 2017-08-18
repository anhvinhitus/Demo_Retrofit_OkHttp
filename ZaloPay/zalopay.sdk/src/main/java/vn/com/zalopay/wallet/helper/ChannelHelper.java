package vn.com.zalopay.wallet.helper;

import android.support.annotation.LayoutRes;
import android.text.TextUtils;

import java.util.Collection;
import java.util.List;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.RS;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.entity.MultiValueMap;
import vn.com.zalopay.wallet.entity.gatewayinfo.PaymentChannel;

public class ChannelHelper {
    public static
    @LayoutRes
    int getLayout(int channelId, boolean isLinkAccount) {
        switch (channelId) {
            case BuildConfig.channel_zalopay:
                return R.layout.screen__zalopay;
            case BuildConfig.channel_atm:
            case BuildConfig.channel_credit_card:
                return R.layout.screen__card;
            case BuildConfig.channel_bankaccount:
                if (isLinkAccount) {
                    return R.layout.screen__link__acc;
                } else {
                    return R.layout.screen__card;
                }
            default:
                return R.layout.screen__card;
        }
    }

    public static void createChannelIcon(PaymentChannel channel, String pIconName) {
        if (channel == null) {
            return;
        }
        if (channel.pmcid == Constants.DEFAULT_LINK_ID) {
            channel.channel_icon = RS.drawable.ic_add_card;
        } else if (!TextUtils.isEmpty(pIconName)) {
            channel.channel_icon = makeCardIconNameFromBankCode(pIconName);
        } else {
            channel.channel_icon = String.format("ic_%d.png", channel.pmcid);
        }
        channel.channel_next_icon = RS.drawable.ic_next;
    }

    public static String makeCardIconNameFromBankCode(String pBankCode) {
        return String.format("%s.png", pBankCode);
    }

    public static int btnConfirmText(@TransactionType int transtype) {
        int btnTextId;
        switch (transtype) {
            case TransactionType.MONEY_TRANSFER:
                btnTextId = R.string.sdk_transfer_button_confirm_text;
                break;
            case TransactionType.TOPUP:
                btnTextId = R.string.sdk_topup_button_confirm_text;
                break;
            case TransactionType.WITHDRAW:
                btnTextId = R.string.sdk_withdraw_button_confirm_text;
                break;
            case TransactionType.PAY:
                btnTextId = R.string.sdk_pay_button_confirm_txt;
                break;
            default:
                btnTextId = R.string.sdk_generic_button_confirm_txt;
        }
        return btnTextId;
    }

    public static int btnConfirmDrawable(PaymentChannel channel) {
        if (channel != null
                && (channel.isZaloPayChannel() || channel.isMapCardChannel() || channel.isBankAccountMap() || channel.isLinkChannel())) {
            return R.drawable.bg_btn_green_border_selector;
        } else {
            return R.drawable.bg_btn_blue_border_selector;
        }
    }

    public static List<PaymentChannel> getChannels(String pKey, MultiValueMap<String, Object> pMap) throws Exception {
        if (pMap == null || pMap.size() <= 0) {
            return null;
        }
        List<PaymentChannel> channelList = null;
        Object objects = pMap.get(pKey);
        if (objects instanceof Collection) {
            channelList = (List<PaymentChannel>) objects;
        }
        return channelList;
    }

}
