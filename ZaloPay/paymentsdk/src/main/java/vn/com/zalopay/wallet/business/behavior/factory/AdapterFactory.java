package vn.com.zalopay.wallet.business.behavior.factory;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankAccount;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.utils.Log;
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
            String channel = owner.getIntent().getStringExtra(GlobalData.getStringResource(RS.string.zingpaysdk_intent_key_channel));

            if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm))) {
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterBankCard");
                adapter = new AdapterBankCard(owner);
            } else if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay))) {
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterZaloPay");
                adapter = new AdapterZaloPay(owner);
            } else if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card))) {
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterCreditCard");
                adapter = new AdapterCreditCard(owner);
            } else if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_link_acc))) {
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterLinkAcc");
                adapter = new AdapterLinkAcc(owner);
            } else if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount))) {
                Log.i("Zmp", "AdapterFactory.produce adapter=AdapterBankAccount");
                adapter = new AdapterBankAccount(owner);
            } else {
                Log.e("Zmp", "AdapterFactory.produce adapter=null");
            }

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
    public static AdapterBase produceChannelByID(PaymentChannelActivity owner, String pChannelID) throws Exception {
        AdapterBase adapter = null;

        if (pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm))) {
            Log.d("ZPW", "AdapterFactory.produce adapter=AdapterBankCard");
            adapter = new AdapterBankCard(owner);
        } else if (pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay))) {
            Log.d("ZPW", "AdapterFactory.produce adapter=AdapterZaloPay");
            adapter = new AdapterZaloPay(owner);
        } else if (pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card))) {
            Log.d("ZPW", "AdapterFactory.produce adapter=AdapterCreditCard");
            adapter = new AdapterCreditCard(owner);
        } else if (pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_link_acc))) {
            Log.i("Zmp", "AdapterFactory.produce adapter=AdapterLinkAcc");
            adapter = new AdapterLinkAcc(owner);
        } else {
            Log.d("ZmZPWp", "AdapterFactory.produce adapter=null");
        }

        return adapter;
    }
}
