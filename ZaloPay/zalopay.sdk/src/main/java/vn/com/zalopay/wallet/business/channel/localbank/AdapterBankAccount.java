package vn.com.zalopay.wallet.business.channel.localbank;

<<<<<<< HEAD
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
=======
>>>>>>> 9fd9a35... [SDK] Apply app info v1
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterBankAccount extends AdapterBankCard {
    public AdapterBankAccount(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType) throws Exception {
        super(pOwnerActivity, pMiniPmcTransType);
    }

<<<<<<< HEAD
    @Override
    public String getChannelID() {
        if (mConfig != null) {
            return String.valueOf(mConfig.pmcid);
        }
        return String.valueOf(BuildConfig.channel_bankaccount);
=======
    protected int getDefaultChannelId() {
        return Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount));
>>>>>>> 9fd9a35... [SDK] Apply app info v1
    }

    @Override
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
    }
}
