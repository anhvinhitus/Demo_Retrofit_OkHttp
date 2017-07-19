package vn.com.zalopay.wallet.api;

import android.content.Context;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.task.SDKReportTask;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

/**
 * Created by huuhoa on 7/19/17.
 * Reporter for SDK errors
 */

public class SdkErrorReporter {
    private Context mContext;

    public SdkErrorReporter(Context context) {
        mContext = context;
    }

    public void sdkReportErrorOnPharse(ISdkErrorContext errorContext, int pPharse, String pMessage) {
        String paymentError = mContext.getResources().getString(R.string.sdk_report_error_format);
        if (TextUtils.isEmpty(paymentError) || !ConnectionUtil.isOnline(mContext)) {
            return;
        }
        try {
            paymentError = String.format(paymentError, String.valueOf(pPharse), String.valueOf(200), pMessage);
            sdkReportError(errorContext, SDKReportTask.TRANSACTION_FAIL, paymentError);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    public void sdkReportErrorOnTransactionFail(ISdkErrorContext errorContext, String responseStatus) throws Exception {
        if (!PaymentPermission.allowSendLogOnTransactionFail() && !ConnectionUtil.isOnline(mContext)) {
            return;
        }
        String paymentError = mContext.getResources().getString(R.string.sdk_report_error_format);
        if (!TextUtils.isEmpty(paymentError)) {
            paymentError = String.format(paymentError, Constants.RESULT_PHARSE, 200, responseStatus);
            sdkReportError(errorContext, SDKReportTask.TRANSACTION_FAIL, paymentError);
        }
    }

    public void sdkReportError(ISdkErrorContext errorContext, int pErrorCode, String pMessage) {
        if (errorContext == null || !errorContext.hasCardGuiProcessor() || !ConnectionUtil.isOnline(mContext)) {
            return;
        }
        try {
            String bankCode = errorContext.getDetectedBankCode();
            SDKReportTask.makeReportError(errorContext.getUserInfo(), pErrorCode, errorContext.getTransactionId(), pMessage, bankCode);
        } catch (Exception ex) {
            Timber.d(ex.getMessage());
        }
    }
}
