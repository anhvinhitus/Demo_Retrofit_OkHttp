package vn.com.zalopay.wallet.business.channel.zalopay;

import android.content.Context;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

import static vn.com.zalopay.wallet.constants.Constants.SCREEN_ZALOPAY;

public class AdapterZaloPay extends AdapterBase {
    public AdapterZaloPay(Context pContext, ChannelPresenter pPresenter, MiniPmcTransType pMiniPmcTransType,
                          PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pContext, SCREEN_ZALOPAY, pPresenter, pMiniPmcTransType, paymentInfoHelper, statusResponse);
    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_zalopay;
    }

    @Override
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
    }

    /***
     * if this is redpackage,then close sdk
     * @return
     */
    @Override
    public boolean processResultRedPacket() {
        boolean isReqPackage = mPaymentInfoHelper != null && mPaymentInfoHelper.isRedPacket();
        if (isReqPackage) {
            onClickSubmission();
        }
        return isReqPackage;
    }

    @Override
    public void onProcessPhrase() {
        if (isBalanceErrorPharse()) {
            try {
                getPresenter().setPaymentStatusAndCallback(PaymentStatus.ERROR_BALANCE);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }
}
