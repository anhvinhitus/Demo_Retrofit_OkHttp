package vn.com.zalopay.wallet.business.behavior.factory;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankAccount;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterFactory {
    /**
     * new channel adapter
     *
     * @param owner ownerActivity
     * @return adapter
     */
    public static AdapterBase produce(PaymentChannelActivity owner) {
        AdapterBase adapter = null;
        try {
            int channel = owner.getIntent().getIntExtra(PaymentChannelActivity.PMCID_EXTRA, -1);
            adapter = produceChannelByID(owner, channel);
        } catch (Exception ex) {
            Log.e("produce", ex);
        }
        return adapter;
    }

    /**
     * new channel adapter with ChannelID
     *
     * @param owner      ownerActivity
     * @param pChannelID ChannelID
     * @return adapter
     */
    public static AdapterBase produceChannelByID(PaymentChannelActivity owner, int pChannelID) throws Exception {
        AdapterBase adapter = null;
        switch (pChannelID) {
            case BuildConfig.channel_zalopay:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterZaloPay");
                adapter = new AdapterZaloPay(owner);
                break;
            case BuildConfig.channel_atm:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterBankCard");
                adapter = new AdapterBankCard(owner);
                break;
            case BuildConfig.channel_credit_card:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterCreditCard");
                adapter = new AdapterCreditCard(owner);
                break;
            case BuildConfig.channel_bankaccount:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterBankAccount");
                adapter = new AdapterBankAccount(owner);
                break;
            case BuildConfig.channel_link_acc:
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterLinkAcc");
                adapter = new AdapterLinkAcc(owner);
                break;
            default:
                Log.d("Zmp", "AdapterFactory.produce adapter=null");

        }
        return adapter;
    }
}
