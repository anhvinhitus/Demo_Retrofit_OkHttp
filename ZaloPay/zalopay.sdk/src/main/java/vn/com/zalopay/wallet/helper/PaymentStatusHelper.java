package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.constants.PaymentActionStatus;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.SecurityResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;

public class PaymentStatusHelper {
    public static boolean isNetworkingErrorResponse(BaseResponse pResponse) {
        return pResponse == null;
    }

    public static boolean isErrorResponse(BaseResponse pResponse) {
        return pResponse != null && pResponse.returncode < 0 && !TextUtils.isEmpty(pResponse.getMessage());
    }

    public static boolean isCardMapByOtherAccount(BaseResponse pCardInfoResponse) {
        return pCardInfoResponse.returncode == Constants.CARD_ALREADY_MAP;
    }

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

    public static boolean isPaymentOverLimitPerDay(StatusResponse pResponse) {
        return pResponse != null && Constants.PAYMENT_LIMIT_PER_DAY_CODE.contains(pResponse.returncode);
    }
}
