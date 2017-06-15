package vn.com.zalopay.wallet.helper;

import android.content.Context;

import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.exception.RequestException;

/**
 * Created by chucvv on 6/13/17.
 */

public class TransactionHelper {
    public static String getMessage(Throwable throwable) {
        String message = null;
        if (throwable instanceof RequestException) {
            RequestException requestException = (RequestException) throwable;
            message = requestException.getMessage();
            switch (requestException.code) {
                case RequestException.NULL:
                    message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
                    break;
            }
        } else if (throwable instanceof NetworkConnectionException) {
            message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }
        return message;
    }

    public static String getAppNameByTranstype(Context context, @TransactionType int transtype) {
        String appName = null;
        switch (transtype) {
            case TransactionType.MONEY_TRANSFER:
                appName = context.getString(R.string.sdk_tranfer_money_service);
                break;
            case TransactionType.WITHDRAW:
                appName = context.getString(R.string.sdk_withdraw_service);
                break;
            case TransactionType.TOPUP:
                appName = context.getString(R.string.sdk_topup_service);
                break;
        }
        return appName;
    }
}
