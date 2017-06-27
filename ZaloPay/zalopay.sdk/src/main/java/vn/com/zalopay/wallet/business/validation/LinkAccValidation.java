package vn.com.zalopay.wallet.business.validation;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;

/**
 * Created by SinhTT on 21/11/2016.
 */

public class LinkAccValidation implements IValidate {

    @Override
    public String onValidateUser(UserInfo userInfo) {
        if (userInfo == null || TextUtils.isEmpty(userInfo.zalo_userid) || TextUtils.isEmpty(userInfo.zalopay_userid) || TextUtils.isEmpty(userInfo.accesstoken)) {
            return GlobalData.getStringResource(RS.string.sdk_invalid_missing_linkaccInfo);
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
        String error = onValidateUser(paymentInfo.getUser());
        if (TextUtils.isEmpty(error)) {
            if (paymentInfo.getLinkAccountInfo() == null || TextUtils.isEmpty(paymentInfo.getLinkAccountInfo().getBankCode())) {
                error = GlobalData.getStringResource(RS.string.sdk_invalid_missing_linkaccInfo);
            }
        }
        return error;
    }
}
