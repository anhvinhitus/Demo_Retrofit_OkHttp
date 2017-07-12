package vn.com.zalopay.wallet.tracker;

import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPApptransidLogApiCall;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.constants.TransactionType;


/**
 * Created by lytm on 04/05/2017.
 */

public class ZPAnalyticsTrackerWrapper extends SingletonBase {
    ZPApptransidLog mZPApptransidLog;

    public ZPAnalyticsTrackerWrapper(long pAppId, String pAppTransID, @TransactionType int transactionType, int ordersource) {
        super();
        initialize(pAppId, pAppTransID, transactionType, ordersource);
    }

    public ZPAnalyticsTrackerWrapper step(int step) {
        mZPApptransidLog.step = step;
        return this;
    }

    public ZPAnalyticsTrackerWrapper pmcId(int pmcId) {
        mZPApptransidLog.pcmid = pmcId;
        return this;
    }

    public ZPAnalyticsTrackerWrapper bankCode(String bankCode) {
        mZPApptransidLog.bank_code = bankCode;
        return this;
    }

    public ZPAnalyticsTrackerWrapper transId(long transId) {
        mZPApptransidLog.transid = transId;
        return this;
    }

    public ZPAnalyticsTrackerWrapper step_result(int step_result) {
        mZPApptransidLog.step_result = step_result;
        return this;
    }

    public ZPAnalyticsTrackerWrapper server_result(int server_result) {
        mZPApptransidLog.server_result = server_result;
        return this;
    }

    protected void initialize(long pAppId, String pAppTransId, int pTransType, int ordersource) {
        mZPApptransidLog = new ZPApptransidLog(pAppTransId, pAppId, ZPPaymentSteps.OrderStep_GetAppInfo,
                ZPPaymentSteps.OrderStepResult_None, 0, pTransType, 0, 0, 0, ordersource,
                System.currentTimeMillis(), System.currentTimeMillis(), "", 0);
    }

    public void trackApiTiming(int apiId, long startTime, long endTime, int returnCode) {
        if (TextUtils.isEmpty(mZPApptransidLog.apptransid)) {
            Timber.d("skip track api call app trans id");
            return;
        }
        ZPApptransidLogApiCall apiTrack = new ZPApptransidLogApiCall();
        apiTrack.apptransid = mZPApptransidLog.apptransid;
        apiTrack.apiid = apiId;
        apiTrack.time_begin = startTime;
        apiTrack.time_end = endTime;
        apiTrack.return_code = returnCode;
        ZPAnalytics.trackApptransidApiCall(apiTrack);
        Timber.d("tracking call api timing api (apiid, returncode) - (%s , %s) time %s(ms)", apiId, returnCode, ((endTime - startTime)));
    }

    public void trackUserCancel() {
        mZPApptransidLog.step = ZPPaymentSteps.OrderStep_OrderResult;
        mZPApptransidLog.step_result = ZPPaymentSteps.OrderStepResult_UserCancel;
        track();
    }

    public void track() {
        if (TextUtils.isEmpty(mZPApptransidLog.apptransid)) {
            Timber.d("skip track app trans id");
            return;
        }
        if (mZPApptransidLog.step == ZPPaymentSteps.OrderStep_OrderResult) {
            mZPApptransidLog.status = 1;
        }
        mZPApptransidLog.finish_time = System.currentTimeMillis();
        ZPAnalytics.trackApptransidEvent(mZPApptransidLog);
        Timber.d("track trans %s", GsonUtils.toJsonString(mZPApptransidLog));
    }
}
