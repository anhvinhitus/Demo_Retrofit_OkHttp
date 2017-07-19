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

    public void sdkReportErrorOnPharse(ISdkErrorContext cardProcessor, int pPharse, String pMessage) {
        String paymentError = mContext.getResources().getString(R.string.sdk_report_error_format);
        if (TextUtils.isEmpty(paymentError) || !ConnectionUtil.isOnline(mContext)) {
            return;
        }
        try {
            paymentError = String.format(paymentError, String.valueOf(pPharse), String.valueOf(200), pMessage);
            sdkReportError(cardProcessor, SDKReportTask.TRANSACTION_FAIL, paymentError);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    public void sdkReportErrorOnTransactionFail(ISdkErrorContext cardProcessor, String responseStatus) throws Exception {
        if (!PaymentPermission.allowSendLogOnTransactionFail() && !ConnectionUtil.isOnline(mContext)) {
            return;
        }
        String paymentError = mContext.getResources().getString(R.string.sdk_report_error_format);
        if (!TextUtils.isEmpty(paymentError)) {
            paymentError = String.format(paymentError, Constants.RESULT_PHARSE, 200, responseStatus);
            sdkReportError(cardProcessor, SDKReportTask.TRANSACTION_FAIL, paymentError);
        }
    }

    public void sdkReportError(ISdkErrorContext cardProcessor, int pErrorCode, String pMessage) {
        if (cardProcessor == null || !cardProcessor.hasCardGuiProcessor() || !ConnectionUtil.isOnline(mContext)) {
            return;
        }
        try {
            String bankCode = cardProcessor.getDetectedBankCode();
            SDKReportTask.makeReportError(cardProcessor.getUserInfo(), pErrorCode, cardProcessor.getTransactionId(), pMessage, bankCode);
        } catch (Exception ex) {
            Timber.d(ex.getMessage());
        }
    }
}
