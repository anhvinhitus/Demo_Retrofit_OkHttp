package vn.com.zalopay.wallet.business.channel.localbank;

import android.text.TextUtils;

import java.util.ArrayList;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptOutput;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentReturnCode;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.business.transaction.SDKTransactionAdapter;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;

public class AdapterBankAccount extends AdapterBankCard {
    public AdapterBankAccount(PaymentChannelActivity pOwnerActivity) throws Exception {
        super(pOwnerActivity);
    }

    @Override
    public String getChannelID() {
        if (mConfig != null)
        {
            return String.valueOf(mConfig.pmcid);
        }
        return GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount);
    }

    @Override
    public DPaymentChannel getChannelConfig() throws Exception {
        return GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankAccountChannelConfig(), DPaymentChannel.class);
    }
    @Override
    public void onProcessPhrase() throws Exception {
        super.onProcessPhrase();
    }

    @Override
    public Object onEvent(EEventType pEventType, Object... pAdditionParams) {

        super.onEvent(pEventType, pAdditionParams);
        return null;
    }

}
