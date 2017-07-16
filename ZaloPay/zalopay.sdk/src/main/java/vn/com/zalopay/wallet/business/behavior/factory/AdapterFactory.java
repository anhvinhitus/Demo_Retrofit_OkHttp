package vn.com.zalopay.wallet.business.behavior.factory;

import android.content.Context;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankAccount;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

public class AdapterFactory {
    public static AdapterBase create(Context pContext, ChannelPresenter presenter, MiniPmcTransType pPmcTransType,
                                     PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) {
        AdapterBase adapter = null;
        try {
            adapter = createByPmc(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
        } catch (Exception ex) {
            Log.e("create", ex);
        }
        return adapter;
    }

    public static AdapterBase createByPmc(Context pContext, ChannelPresenter presenter, MiniPmcTransType pPmcTransType,
                                          PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        AdapterBase adapter = null;
        switch (pPmcTransType.pmcid) {
            case BuildConfig.channel_zalopay:
                adapter = new AdapterZaloPay(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_atm:
                adapter = new AdapterBankCard(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_credit_card:
                adapter = new AdapterCreditCard(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_bankaccount:
                if (pPmcTransType.isBankAccountMap()) {
                    adapter = new AdapterBankAccount(pContext, presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                } else {
                    adapter = new AdapterLinkAcc(pContext, presenter, pPmcTransType, paymentInfoHelper);
                }
                break;
        }
        Log.d("createByPmc", "create adapter", adapter.getClass().getSimpleName());
        return adapter;
    }
}
