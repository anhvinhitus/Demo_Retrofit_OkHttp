package vn.com.zalopay.wallet.business.channel.localbank;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterBankAccount extends AdapterBankCard {
    public AdapterBankAccount(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType);
    }

    protected int getDefaultChannelId() {
        return Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount));
    }

    @Override
    public MiniPmcTransType getConfig(String pBankCode) {
        return mMiniPmcTransType;
    }
}
