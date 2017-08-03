package vn.com.zalopay.wallet.helper;

import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

/**
 * Created by chucvv on 8/3/17.
 */

public class TrackHelper {
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
