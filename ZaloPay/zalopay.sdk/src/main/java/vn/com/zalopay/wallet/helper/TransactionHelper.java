package vn.com.zalopay.wallet.helper;

import android.content.Context;
import android.text.TextUtils;

import retrofit2.adapter.rxjava.HttpException;
import timber.log.Timber;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.vng.zalopay.network.exception.HttpEmptyResponseException;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.SecurityResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentState;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransAuthenType;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.exception.SdkResourceException;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;

import static vn.com.zalopay.wallet.constants.Constants.PAGE_FAIL;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_FAIL_NETWORKING;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_FAIL_PROCESSING;

/**
 * Created by chucvv on 6/13/17.
 */

public class TransactionHelper {
    public static String getMessage(Context context, Throwable throwable) {
        if (throwable == null) {
            return context.getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
        }
        if (throwable instanceof RequestException || throwable instanceof SdkResourceException) {
            return throwable.getMessage();
        }
        if (throwable instanceof NetworkConnectionException) {
            return context.getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
        }
        if(throwable instanceof HttpEmptyResponseException){
            return context.getResources().getString(R.string.sdk_error_api_emptybody);
        }
        if(throwable instanceof HttpException){
            return context.getResources().getString(R.string.sdk_error_system);
        }
        Timber.w(throwable, "undefine exception");
        return context.getResources().getString(R.string.sdk_error_undefine);
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
            case TransactionType.LINK:
                appName = context.getString(R.string.sdk_link_card_service);
                break;
        }
        return appName;
    }

    public static boolean needUserPasswordPayment(MiniPmcTransType pChannel, AbstractOrder pOrder) {
        Log.d("needUserPasswordPayment", "start check require for using password", pChannel);
        if (pChannel == null || pOrder == null) {
            return false;
        }
        int transAuthenType = TransAuthenType.PIN;
        if (pChannel.isNeedToCheckTransactionAmount() && pOrder.amount_total > pChannel.amountrequireotp) {
            transAuthenType = pChannel.overamounttype;
        } else if (pChannel.isNeedToCheckTransactionAmount() && pOrder.amount_total < pChannel.amountrequireotp) {
            transAuthenType = pChannel.inamounttype;
        }
        return transAuthenType == TransAuthenType.PIN || transAuthenType == TransAuthenType.BOTH;
    }

    /**
     * Check transaction status
     *
     * @param pStatusResponse data response
     */
    public static
    @PaymentState
    int paymentState(StatusResponse pStatusResponse) {
        if (pStatusResponse != null && pStatusResponse.returncode == Constants.PIN_WRONG_RETURN_CODE) {
            return PaymentState.INVALID_PASSWORD;
        } else if (isSecurityFlow(pStatusResponse)) {
            return PaymentState.SECURITY;
        } else if (pStatusResponse != null && pStatusResponse.returncode < 0) {
            return PaymentState.FAILURE;
        }
        //transaction is success
        else if (isTransactionSuccess(pStatusResponse)) {
            return PaymentState.SUCCESS;
        }
        //order still need to continue processing
        else if (isOrderProcessing(pStatusResponse)) {
            return PaymentState.PROCESSING;
        } else {
            return PaymentState.FAILURE;
        }
    }

    public static boolean isSecurityFlow(StatusResponse pResponse) {
        if (pResponse != null && pResponse.isprocessing && !TextUtils.isEmpty(pResponse.data)) {
            SecurityResponse dataResponse = GsonUtils.fromJsonString(pResponse.data, SecurityResponse.class);
            if (dataResponse != null && PaymentStatusHelper.is3DSResponse(dataResponse) || PaymentStatusHelper.isOtpResponse(dataResponse)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOrderProcessing(StatusResponse pResponse) {
        return pResponse != null && pResponse.isprocessing;
    }

    public static String getSubmitExceptionMessage(Context context) {
        boolean online = ConnectionUtil.isOnline(context);
        return online ? context.getString(R.string.sdk_error_generic_submitorder) :
                context.getString(R.string.sdk_trans_network_onfline_warning_mess);
    }

    public static String getGenericExceptionMessage(Context context) {
        boolean online = ConnectionUtil.isOnline(context);
        return online ? context.getString(R.string.sdk_fail_trans_status) :
                context.getString(R.string.sdk_alert_networking_off_generic);
    }

    public static boolean isTransactionSuccess(StatusResponse pResponse) {
        return pResponse != null && !pResponse.isprocessing && pResponse.returncode == 1;
    }

    public static String getPageName(@PaymentStatus int status) {
        switch (status) {
            case PaymentStatus.SUCCESS:
                return Constants.PAGE_SUCCESS;
            case PaymentStatus.ERROR_BALANCE:
                return Constants.PAGE_BALANCE_ERROR;
            case PaymentStatus.FAILURE:
                return Constants.PAGE_FAIL;
            case PaymentStatus.PROCESSING:
                return Constants.PAGE_FAIL_PROCESSING;
            default:
                return null;
        }
    }

    public static boolean isTransFail(String pPageName) {
        switch (pPageName) {
            case PAGE_FAIL:
            case PAGE_FAIL_NETWORKING:
            case PAGE_FAIL_PROCESSING:
                return true;
            default:
                return false;
        }
    }

    public static boolean isTransNetworkError(Context context, String pMessage) {
        return pMessage.equalsIgnoreCase(context.getString(R.string.sdk_trans_fail_check_status_mess))
                || pMessage.equalsIgnoreCase(context.getString(R.string.sdk_trans_order_not_submit_mess))
                || pMessage.equalsIgnoreCase(context.getString(R.string.sdk_payment_generic_error_networking_mess))
                || pMessage.equalsIgnoreCase(context.getString(R.string.sdk_trans_networking_offine_mess))
                || pMessage.equalsIgnoreCase(context.getString(R.string.sdk_alert_networking_off_in_link_account))
                || pMessage.equalsIgnoreCase(context.getString(R.string.sdk_alert_networking_off_in_unlink_account))
                || pMessage.equalsIgnoreCase(context.getString(R.string.sdk_trans_network_onfline_warning_mess));
    }
}
