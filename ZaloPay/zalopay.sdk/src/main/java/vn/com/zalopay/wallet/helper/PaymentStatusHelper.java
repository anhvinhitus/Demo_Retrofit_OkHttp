package vn.com.zalopay.wallet.helper;

import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.base.SecurityResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.constants.PaymentActionStatus;

public class PaymentStatusHelper {
    public static boolean isTransactionNotSubmit(StatusResponse pResponse) {
        return pResponse != null && pResponse.returncode == Constants.TRANSACTION_NOT_SUBMIT;
    }

    public static boolean isServerInMaintenance(StatusResponse pResponse) {
        return pResponse != null && isServerInMaintenance(pResponse.returncode);
    }

    public static boolean isServerInMaintenance(int pResponseCode) {
        return pResponseCode == Constants.SERVER_MAINTENANCE_CODE;
    }

    public static boolean isWrongOtpResponse(StatusResponse pResponse) {
        return pResponse != null && pResponse.returncode == Constants.AUTHEN_PAYER_OTP_WRONG_CODE;

    }

    public static boolean isNeedToGetStatusAfterAuthenPayer(StatusResponse pResponse) {
        if (pResponse == null) {
            return false;
        }

        return pResponse.isprocessing || Constants.GET_STATUS_AUTHEN_PAYER_CODE.contains(pResponse.returncode);
    }

    public static boolean isNeedToUpgradeLevelUser(int pResponseCode) {
        return pResponseCode == Constants.UPGRADE_LEVEL_CODE;
    }

    public static boolean isNeedToChargeMoreMoney(int pResponseCode) {
        return Constants.MONEY_NOT_ENOUGH_CODE.contains(pResponseCode);
    }

    public static boolean isTransactionProcessing(int pResponseCode) {
        return pResponseCode == Constants.TRANSACTION_PROCESSING;
    }

    public static boolean is3DSResponse(SecurityResponse pResponse) {
        return pResponse != null && pResponse.actiontype == PaymentActionStatus.THREE3DS;
    }

    public static boolean isOtpResponse(SecurityResponse pResponse) {
        return pResponse != null && pResponse.actiontype == PaymentActionStatus.OTP;
    }
}
