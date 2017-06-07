package vn.com.zalopay.wallet.business.validation;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;

public class PayValidation implements IValidate {
    @Override
    public String onValidateUser(UserInfo userInfo) {
        String error = null;
        if (userInfo == null) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user);
        } else if (TextUtils.isEmpty(userInfo.zalopay_userid)) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user);
        } else if (TextUtils.isEmpty(userInfo.zalo_userid)) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_user);
        } else if (TextUtils.isEmpty(userInfo.accesstoken)) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_invalid_token);
        }
        return error;
    }

    @Override
    public String onValidateOrder(AbstractOrder order) {
        String error = null;
        if (order == null) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_missing_app_pmt_info);
        } else if (order.appid <= 0) {
            error = GlobalData.getStringResource(RS.string.zalopay_invalid_app_id);
        } else if (TextUtils.isEmpty(order.apptransid)) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_invalid_app_trans);
        } else if (order.apptime <= 0) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_invalid_app_time);
        } else if (order.amount <= 0) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_invalid_app_amount);
        } else if (TextUtils.isEmpty(order.mac)) {
            error = GlobalData.getStringResource(RS.string.zingpaysdk_missing_mac_info);
        }
        return error;
    }

    /***
     * validate app user and zalopay user.
     * tranfer money channel: zalopay user and app user must same
     * topup + withdraw: zalopay user and app user must be different
     * @return
     */
    @Override
    public String onValidate(IPaymentInfo paymentInfo) {
        String error = onValidateUser(paymentInfo.getUser());
        if (TextUtils.isEmpty(error)) {
            error = onValidateOrder(paymentInfo.getOrder());
        }
        if (TextUtils.isEmpty(error)) {

            if (paymentInfo.getTranstype() == TransactionType.MONEY_TRANSFER &&
                    paymentInfo.getUser().zalopay_userid.equals(paymentInfo.getDestinationUser().zalopay_userid)) {
                error = GlobalData.getStringResource(RS.string.zpw_string_alert_app_user_invalid_tranfer);
            } else if ((paymentInfo.getTranstype() == TransactionType.TOPUP || paymentInfo.getTranstype() == TransactionType.WITHDRAW) &&
                    paymentInfo.getUser().zalopay_userid.equals(paymentInfo.getDestinationUser().zalopay_userid)) {
                error = GlobalData.getStringResource(RS.string.zingpaysdk_invalid_user_id_user_name);
            }
        }
        return error;
    }
}
