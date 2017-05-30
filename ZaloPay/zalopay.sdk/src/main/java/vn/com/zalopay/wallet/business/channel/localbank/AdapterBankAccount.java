package vn.com.zalopay.wallet.business.channel.localbank;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterBankAccount extends AdapterBankCard {
    public AdapterBankAccount(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType);
    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_bankaccount;
    }

    @Override
    public MiniPmcTransType getConfig(String pBankCode) {
        return mMiniPmcTransType;
    }
}
