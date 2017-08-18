package vn.com.zalopay.wallet.validation;

import android.text.TextUtils;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.GlobalData;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.IPaymentInfo;

public class PayValidation implements IValidate {
    @Override
    public String onValidateUser(UserInfo userInfo) {
        String error = null;
        if (userInfo == null) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_appuser_mess);
        } else if (TextUtils.isEmpty(userInfo.zalopay_userid)) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_appuser_mess);
        } else if (TextUtils.isEmpty(userInfo.zalo_userid)) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_appuser_mess);
        } else if (TextUtils.isEmpty(userInfo.accesstoken)) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_token_mess);
        }
        return error;
    }

    @Override
    public String onValidateOrder(AbstractOrder order) {
        String error = null;
        if (order == null) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_order_notfound_mess);
        } else if (order.appid <= 0) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_appid_mess);
        } else if (TextUtils.isEmpty(order.apptransid)) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_app_trans_mess);
        } else if (order.apptime <= 0) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_apptime_mess);
        } else if (order.amount <= 0) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_app_amount_mess);
        } else if (TextUtils.isEmpty(order.mac)) {
            error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_error_mac_mess);
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
                    paymentInfo.getUser().zalopay_userid.equals(paymentInfo.getOrder().appuser)) {
                error = GlobalData.getAppContext().getResources().getString(R.string.sdk_invalid_user_transfer_mess);
            } else if ((paymentInfo.getTranstype() == TransactionType.TOPUP || paymentInfo.getTranstype() == TransactionType.WITHDRAW) &&
                    !paymentInfo.getUser().zalopay_userid.equals(paymentInfo.getOrder().appuser)) {
                error = GlobalData.getAppContext().getResources().getString(R.string.sdk_paymentinfo_invalid_user_id_mess);
            }
        }
        return error;
    }
}
