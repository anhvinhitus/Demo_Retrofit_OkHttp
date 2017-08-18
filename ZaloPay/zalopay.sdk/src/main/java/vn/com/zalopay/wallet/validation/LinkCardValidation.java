package vn.com.zalopay.wallet.validation;

import android.text.TextUtils;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.GlobalData;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;

public class LinkCardValidation implements IValidate {

    @Override
    public String onValidateUser(UserInfo userInfo) {
        if (userInfo == null || TextUtils.isEmpty(userInfo.zalo_userid) || TextUtils.isEmpty(userInfo.zalopay_userid) || TextUtils.isEmpty(userInfo.accesstoken)) {
            return GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_linkaccInfo_mess);
        } else {
            return null;
        }
    }

    @Override
    public String onValidateOrder(AbstractOrder order) {
        return null;
    }

    @Override
    public String onValidate(IPaymentInfo paymentInfo) {
        return onValidateUser(paymentInfo.getUser());
    }
}
