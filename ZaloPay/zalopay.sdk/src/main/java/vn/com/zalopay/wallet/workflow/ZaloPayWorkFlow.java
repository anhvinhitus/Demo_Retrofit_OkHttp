package vn.com.zalopay.wallet.workflow;

import android.content.Context;

import timber.log.Timber;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

public class ZaloPayWorkFlow extends AbstractWorkFlow {
    public ZaloPayWorkFlow(Context pContext, ChannelPresenter pPresenter, MiniPmcTransType pMiniPmcTransType,
                           PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pContext, Constants.SCREEN_ZALOPAY, pPresenter, pMiniPmcTransType, paymentInfoHelper, statusResponse);
    }

    private int getDefaultChannelId() {
        return BuildConfig.channel_zalopay;
    }

    @Override
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
    }

    @Override
    public void onProcessPhrase() {
        if (isBalanceErrorPharse()) {
            try {
                getPresenter().setPaymentStatusAndCallback(PaymentStatus.ERROR_BALANCE);
            } catch (Exception e) {
                Timber.w(e);
            }
        }
    }
}
