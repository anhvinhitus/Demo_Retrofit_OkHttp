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
            String channelId = pPmcTransType != null ? String.valueOf(pPmcTransType.pmcid) : null;
            if (GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm).equals(channelId)) {
                Log.i("sdk", "start create AdapterBankCard");
                adapter = new AdapterBankCard(owner, pPmcTransType);
            } else if (GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay).equals(channelId)) {
                Log.i("sdk", "start create AdapterZaloPay");
                adapter = new AdapterZaloPay(owner, pPmcTransType);
            } else if (GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card).equals(channelId)) {
                Log.i("sdk", "start create AdapterCreditCard");
                adapter = new AdapterCreditCard(owner, pPmcTransType);
            } else if (GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount).equals(channelId) && GlobalData.isBankAccountLink()) {
                Log.i("sdk", "start create AdapterLinkAcc");
                adapter = new AdapterLinkAcc(owner, pPmcTransType);
            } else if (GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount).equals(channelId)) {
                Log.i("sdk", "start create AdapterBankAccount");
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
     * @return
     * @throws Exception
     */
    public static AdapterBase produceChannelByID(PaymentChannelActivity owner, MiniPmcTransType pPmcTransType) throws Exception {
        AdapterBase adapter = null;
        try {
            String channelId = pPmcTransType != null ? String.valueOf(pPmcTransType.pmcid) : null;
            if (GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm).equals(channelId)) {
                Log.i("sdk", "start create AdapterBankCard");
                adapter = new AdapterBankCard(owner, pPmcTransType);
            } else if (GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card).equals(channelId)) {
                Log.i("sdk", "start create AdapterCreditCard");
                adapter = new AdapterCreditCard(owner, pPmcTransType);
            }
            Log.e("sdk", "AdapterFactory.produce adapter=null");

        } catch (Exception ex) {
            Log.e("produce", ex);
        }
        return adapter;
    }
}
