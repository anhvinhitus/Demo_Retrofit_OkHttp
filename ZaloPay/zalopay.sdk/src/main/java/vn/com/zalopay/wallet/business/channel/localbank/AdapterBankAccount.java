package vn.com.zalopay.wallet.business.channel.localbank;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterBankAccount extends AdapterBankCard {
    public AdapterBankAccount(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType,
                              PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType, paymentInfoHelper, statusResponse);
    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_bankaccount;
    }

    @Override
    public MiniPmcTransType getConfig(String pBankCode) {
        return mMiniPmcTransType;
    }
}
