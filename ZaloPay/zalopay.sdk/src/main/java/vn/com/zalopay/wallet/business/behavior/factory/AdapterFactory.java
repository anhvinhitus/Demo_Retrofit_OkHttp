package vn.com.zalopay.wallet.business.behavior.factory;

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
    public static AdapterBase create(ChannelPresenter presenter, MiniPmcTransType pPmcTransType,
                                     PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) {
        AdapterBase adapter = null;
        try {
            adapter = createByPmc(presenter, pPmcTransType, paymentInfoHelper, statusResponse);
        } catch (Exception ex) {
            Log.e("create", ex);
        }
        return adapter;
    }

    public static AdapterBase createByPmc(ChannelPresenter presenter, MiniPmcTransType pPmcTransType,
                                          PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        AdapterBase adapter = null;
        switch (pPmcTransType.pmcid) {
            case BuildConfig.channel_zalopay:
                adapter = new AdapterZaloPay(presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_atm:
                adapter = new AdapterBankCard(presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_credit_card:
                adapter = new AdapterCreditCard(presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_bankaccount:
                if (pPmcTransType.isBankAccountMap()) {
                    adapter = new AdapterBankAccount(presenter, pPmcTransType, paymentInfoHelper, statusResponse);
                } else {
                    adapter = new AdapterLinkAcc(presenter, pPmcTransType, paymentInfoHelper);
                }
                break;
        }
        Log.d("createByPmc", "create adapter", adapter.getClass().getSimpleName());
        return adapter;
    }
}
