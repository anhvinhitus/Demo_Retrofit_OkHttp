package vn.com.zalopay.wallet.workflow;

import android.content.Context;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

public class WorkFlowFactoryCreator {
    public static AbstractWorkFlow create(Context pContext, ChannelPresenter presenter, MiniPmcTransType pPmcTransType,
                                          PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) {
        AbstractWorkFlow adapter = null;
        try {
            adapter = createByPmc(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
        } catch (Exception ex) {
            Log.e("create", ex);
        }
        return adapter;
    }

    public static AbstractWorkFlow createByPmc(Context pContext, ChannelPresenter presenter, MiniPmcTransType pPmcTransType,
                                               PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        AbstractWorkFlow adapter = null;
        switch (pPmcTransType.pmcid) {
            case BuildConfig.channel_zalopay:
                adapter = new ZaloPayWorkFlow(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_atm:
                adapter = new BankCardWorkFlow(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_credit_card:
                adapter = new CreditCardWorkFlow(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_bankaccount:
                if (pPmcTransType.isBankAccountMap()) {
                    adapter = new AccountMapWorkFlow(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                } else {
                    adapter = new AccountLinkWorkFlow(pContext, presenter, pPmcTransType, paymentInfoHelper);
                }
                break;
        }
        return adapter;
    }
}
