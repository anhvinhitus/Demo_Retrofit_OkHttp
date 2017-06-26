package vn.com.zalopay.wallet.business.channel.localbank;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

public class AdapterBankAccount extends AdapterBankCard {
    public AdapterBankAccount(ChannelPresenter pPresenter, MiniPmcTransType pMiniPmcTransType,
                              PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pPresenter, pMiniPmcTransType, paymentInfoHelper, statusResponse);
    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_bankaccount;
    }

    @Override
    public MiniPmcTransType getConfig(String pBankCode) {
        return mMiniPmcTransType;
    }
}
