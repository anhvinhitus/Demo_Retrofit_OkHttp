package vn.com.zalopay.wallet.business.behavior.factory;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankAccount;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterFactory {
    /***
     *
     * @param owner
     * @param pPmcTransType
     * @return
     */
    public static AdapterBase produce(PaymentChannelActivity owner, MiniPmcTransType pPmcTransType) {
        AdapterBase adapter = null;
        try {
            String channel = owner.getIntent().getStringExtra(GlobalData.getStringResource(RS.string.zingpaysdk_intent_key_channel));

            if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm))) {
                Log.i("sdk", "AdapterFactory.produce adapter=AdapterBankCard");
                adapter = new AdapterBankCard(owner, pPmcTransType);
            } else if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay))) {
                Log.i("sdk", "AdapterFactory.produce adapter=AdapterZaloPay");
                adapter = new AdapterZaloPay(owner, pPmcTransType);
            } else if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card))) {
                Log.i("sdk", "AdapterFactory.produce adapter=AdapterCreditCard");
                adapter = new AdapterCreditCard(owner, pPmcTransType);
            } else if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_link_acc))) {
                Log.i("sdk", "AdapterFactory.produce adapter=AdapterLinkAcc");
                adapter = new AdapterLinkAcc(owner, pPmcTransType);
            } else if (channel.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount))) {
                Log.i("sdk", "AdapterFactory.produce adapter=AdapterBankAccount");
                adapter = new AdapterBankAccount(owner, pPmcTransType);
            } else {
                Log.e("sdk", "AdapterFactory.produce adapter=null");
            }

        } catch (Exception ex) {
            Log.e("produce", ex);
        }
        return adapter;
    }

    /***
     * new channel adapter with ChannelID
     * @param owner
     * @param pPmcTransType
     * @param pChannelID
     * @return
     * @throws Exception
     */
    public static AdapterBase produceChannelByID(PaymentChannelActivity owner, MiniPmcTransType pPmcTransType, String pChannelID) throws Exception {
        AdapterBase adapter = null;
        if (pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm))) {
            Log.d("sdk", "AdapterFactory.produce adapter=AdapterBankCard");
            adapter = new AdapterBankCard(owner, pPmcTransType);
        } else if (pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay))) {
            Log.d("sdk", "AdapterFactory.produce adapter=AdapterZaloPay");
            adapter = new AdapterZaloPay(owner, pPmcTransType);
        } else if (pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card))) {
            Log.d("sdk", "AdapterFactory.produce adapter=AdapterCreditCard");
            adapter = new AdapterCreditCard(owner, pPmcTransType);
        } else if (pChannelID.equals(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_link_acc))) {
            Log.i("sdk", "AdapterFactory.produce adapter=AdapterLinkAcc");
            adapter = new AdapterLinkAcc(owner, pPmcTransType);
        } else {
            Log.d("sdk", "AdapterFactory.produce adapter=null");
        }
        return adapter;
    }
}
