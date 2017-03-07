package vn.com.zalopay.wallet.business.validation;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.utils.Log;

public class CPaymentValidation implements IPaymentValidate {
    /***
     * validate app user and zalopay user.
     * tranfer money channel: zalopay user and app user must same
     * topup + withdraw: zalopay user and app user must be different
     *
     * @return
     */
    @Override
    public String onValidateUser() {
        String alertWarrning = null;

        try {
            //tranfer money
            if (GlobalData.isTranferMoneyChannel() && GlobalData.getPaymentInfo().userInfo.zaloPayUserId.equals(GlobalData.getPaymentInfo().appUser)) {
                alertWarrning = GlobalData.getStringResource(RS.string.zpw_string_alert_app_user_invalid_tranfer);
            }
            //topup or withdraw
            else if (!GlobalData.getPaymentInfo().userInfo.zaloPayUserId.equals(GlobalData.getPaymentInfo().appUser) &&
                    (GlobalData.isTopupChannel() || GlobalData.isWithDrawChannel())) {
                alertWarrning = GlobalData.getStringResource(RS.string.zingpaysdk_invalid_user_id_user_name);
            }
        } catch (Exception e) {
            Log.e("onValidateUser", e);
        }

        return alertWarrning;
    }

    @Override
    public String onValidateOrderInfo(ZPWPaymentInfo pParams) {
        if (pParams == null || pParams.userInfo == null) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_pmt_info);
        }

        if (TextUtils.isEmpty(pParams.userInfo.zaloPayUserId)) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user);
        }

        if (TextUtils.isEmpty(pParams.userInfo.zaloUserId)) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user);
        }

        if (TextUtils.isEmpty(pParams.userInfo.accessToken)) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_invalid_token);
        }

        if (pParams.appID <= 0) {
            return GlobalData.getStringResource(RS.string.zalopay_invalid_app_id);
        }

        if (TextUtils.isEmpty(pParams.appTransID)) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_invalid_app_trans);
        }

        if (pParams.appTime <= 0) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_invalid_app_time);
        }

        if (pParams.amount <= 0) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_invalid_app_amount);
        }

        if (TextUtils.isEmpty(pParams.mac)) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_missing_mac_info);
        }

        return null;
    }
}
