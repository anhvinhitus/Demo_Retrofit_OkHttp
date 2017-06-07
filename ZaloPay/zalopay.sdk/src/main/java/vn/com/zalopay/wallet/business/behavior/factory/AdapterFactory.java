package vn.com.zalopay.wallet.business.behavior.factory;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankAccount;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterFactory {
    /***
     *
     * @param owner
     * @param pPmcTransType
     * @return
     */
    public static AdapterBase produce(PaymentChannelActivity owner, MiniPmcTransType pPmcTransType, PaymentInfoHelper paymentInfoHelper) {
        AdapterBase adapter = null;
        try {
            adapter = produceChannelByPmc(owner, pPmcTransType, paymentInfoHelper);
        } catch (Exception ex) {
            Log.e("produce", ex);
        }
        return adapter;
    }

    public static AdapterBase produceChannelByPmc(PaymentChannelActivity owner, MiniPmcTransType pPmcTransType, PaymentInfoHelper paymentInfoHelper) throws Exception {
        AdapterBase adapter = null;
        switch (pPmcTransType.pmcid) {
            case BuildConfig.channel_zalopay:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterZaloPay");
                adapter = new AdapterZaloPay(owner, pPmcTransType, paymentInfoHelper);
                break;
            case BuildConfig.channel_atm:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterBankCard");
                adapter = new AdapterBankCard(owner, pPmcTransType, paymentInfoHelper);
                break;
            case BuildConfig.channel_credit_card:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterCreditCard");
                adapter = new AdapterCreditCard(owner, pPmcTransType, paymentInfoHelper);
                break;
            case BuildConfig.channel_bankaccount:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterBankAccount");
                adapter = new AdapterBankAccount(owner, pPmcTransType, paymentInfoHelper);
                break;
            case BuildConfig.channel_link_acc:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterLinkAcc");
                adapter = new AdapterLinkAcc(owner, pPmcTransType, paymentInfoHelper);
                break;
            default:
                Log.d("Zmp", "AdapterFactory.produce adapter=null");
        }
        return adapter;
    }
}
