package vn.com.zalopay.wallet.business.behavior.factory;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankAccount;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
<<<<<<< HEAD
import vn.com.zalopay.wallet.business.data.Log;
=======
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.utils.Log;
>>>>>>> 9fd9a35... [SDK] Apply app info v1
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
<<<<<<< HEAD
            int channel = owner.getIntent().getIntExtra(PaymentChannelActivity.PMCID_EXTRA, -1);
            adapter = produceChannelByID(owner, channel);
=======
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

>>>>>>> 9fd9a35... [SDK] Apply app info v1
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
<<<<<<< HEAD
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

=======
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
>>>>>>> 9fd9a35... [SDK] Apply app info v1
        }
        return adapter;
    }
}
