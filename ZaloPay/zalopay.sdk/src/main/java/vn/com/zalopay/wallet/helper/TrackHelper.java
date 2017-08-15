package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

/*
 * Created by chucvv on 8/3/17.
 */

public class TrackHelper {
    public static void trackPaymentResult(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper == null) {
            return;
        }
        if (paymentInfoHelper.isLinkTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.LINKBANK_ADD_RESULT);
        } else if (paymentInfoHelper.isTopupTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_ADDCASH_RESULT);
        } else if (paymentInfoHelper.isWithDrawTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_WITHDRAW_RESULT);
        } else if (paymentInfoHelper.isMoneyTranferTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_RESULT);
        }
    }

    public static void trackingTransactionEvent(int pResult, StatusResponse pResponse, String bankCode) throws Exception {
        if (pResponse == null) {
            return;
        }
        int returnCode = pResponse.returncode;
        if (TextUtils.isEmpty(bankCode)) {
            bankCode = "";
        }
        Long transId;
        try {
            transId = Long.parseLong(pResponse.zptransid);
        } catch (Exception e) {
            transId = -1L;
        }
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper
                    .step(ZPPaymentSteps.OrderStep_OrderResult)
                    .transId(transId)
                    .bankCode(bankCode)
                    .server_result(returnCode)
                    .step_result(pResult)
                    .track();
        }
    }
    public static void trackEventLaunch(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper == null) {
            return;
        }
        if (paymentInfoHelper.isTopupTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_ADDCASH_PAYMENT_METHOD_LAUNCH);
        } else if (paymentInfoHelper.isWithDrawTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_WITHDRAW_PAYMENT_METHOD_LAUNCH);
        } else if (paymentInfoHelper.isMoneyTranferTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_PAYMENT_METHOD_LAUNCH);
        }
    }

    public static void trackEventBack(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper == null) {
            return;
        }
        if (paymentInfoHelper.isTopupTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_ADDCASH_PAYMENT_METHOD_CANCEL);
        } else if (paymentInfoHelper.isWithDrawTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_WITHDRAW_PAYMENT_METHOD_CANCEL);
        } else if (paymentInfoHelper.isMoneyTranferTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_PAYMENT_METHOD_CANCEL);
        }
    }

    public static void trackEventConfirm(PaymentInfoHelper paymentInfoHelper) {
        if (paymentInfoHelper == null) {
            return;
        }
        if (paymentInfoHelper.isTopupTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_ADDCASH_PAYMENT_METHOD_CONFIRM);
        } else if (paymentInfoHelper.isWithDrawTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.BALANCE_WITHDRAW_PAYMENT_METHOD_CONFIRM);
        } else if (paymentInfoHelper.isWithDrawTrans()) {
            ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_PAYMENT_METHOD_CONFIRM);
        }

    }
}
