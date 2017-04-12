package vn.com.zalopay.wallet.business.channel.localbank;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterBankAccount extends AdapterBankCard {
    public AdapterBankAccount(PaymentChannelActivity pOwnerActivity) throws Exception {
        super(pOwnerActivity);
    }

    @Override
    public String getChannelID() {
        if (mConfig != null) {
            return String.valueOf(mConfig.pmcid);
        }
        return GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount);
    }

    @Override
    public DPaymentChannel getChannelConfig() throws Exception {
        return GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankAccountChannelConfig(), DPaymentChannel.class);
    }
}
