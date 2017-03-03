package vn.com.zalopay.wallet.business.validation;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;

public class CLinkCardValidation implements IPaymentValidate {
    @Override
    public String onValidateUser() {
        return null;
    }

    @Override
    public String onValidateOrderInfo(ZPWPaymentInfo pParams) {
        if (pParams == null || pParams.userInfo == null) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_pmt_info);
        }

        if (TextUtils.isEmpty(pParams.userInfo.zaloPayUserId))
            return GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user);

        if (TextUtils.isEmpty(pParams.userInfo.zaloUserId))
            return GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user);

        if (TextUtils.isEmpty(pParams.userInfo.accessToken))
            return GlobalData.getStringResource(RS.string.zingpaysdk_invalid_token);

        if (pParams.appID <= 0)
            return GlobalData.getStringResource(RS.string.zalopay_invalid_app_id);

        return null;
    }
}
