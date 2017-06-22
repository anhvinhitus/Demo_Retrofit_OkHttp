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
import vn.com.zalopay.wallet.ui.channel.PaymentChannelActivity;

public class AdapterFactory {
    /***
     *
     * @param owner
     * @param pPmcTransType
     * @return
     */
    public static AdapterBase produce(PaymentChannelActivity owner, MiniPmcTransType pPmcTransType,
                                      PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) {
        AdapterBase adapter = null;
        try {
            adapter = produceChannelByPmc(owner, pPmcTransType, paymentInfoHelper, statusResponse);
        } catch (Exception ex) {
            Log.e("produce", ex);
        }
        return adapter;
    }

    public static AdapterBase produceChannelByPmc(PaymentChannelActivity owner, MiniPmcTransType pPmcTransType,
                                                  PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        AdapterBase adapter = null;
        switch (pPmcTransType.pmcid) {
            case BuildConfig.channel_zalopay:
                adapter = new AdapterZaloPay(owner, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_atm:
                adapter = new AdapterBankCard(owner, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_credit_card:
                adapter = new AdapterCreditCard(owner, pPmcTransType, paymentInfoHelper, statusResponse);
                break;
            case BuildConfig.channel_bankaccount:
                if (pPmcTransType.isBankAccountMap()) {
                    adapter = new AdapterBankAccount(owner, pPmcTransType, paymentInfoHelper, statusResponse);
                } else {
                    adapter = new AdapterLinkAcc(owner, pPmcTransType, paymentInfoHelper);
                }
                break;
        }
        Log.d("produceChannelByPmc", "create adapter", adapter.getClass().getSimpleName());
        return adapter;
    }
}
