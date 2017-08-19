package vn.com.zalopay.wallet.tracker;

import android.text.TextUtils;

import retrofit2.adapter.rxjava.HttpException;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPApptransidLogApiCall;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.entity.response.BaseResponse;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;


/*
 * Created by lytm on 04/05/2017.
 */

public class ZPAnalyticsTrackerWrapper extends SingletonBase {
    ZPApptransidLog mZPApptransidLog;
    String mUserId;

    public ZPAnalyticsTrackerWrapper(String pUserId, long pAppId, String pAppTransID, @TransactionType int transactionType, int ordersource) {
        super();
        mUserId = pUserId;
        initialize(pAppId, pAppTransID, transactionType, ordersource);
    }

    public static void trackApiError(int apiId, long startTime, Throwable throwable) {
        long endTime = System.currentTimeMillis();
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.trackApiTiming(apiId, startTime, endTime, throwable);
        }
    }

    public static void trackApiCall(int apiId, long startTime, BaseResponse response) {
        long endTime = System.currentTimeMillis();
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.trackApiTiming(apiId, startTime, endTime, response);
        }
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
        long currentTime = System.currentTimeMillis();
        mZPApptransidLog = new ZPApptransidLog(pAppTransId,
                pAppId,
                ZPPaymentSteps.OrderStep_GetAppInfo,
                ZPPaymentSteps.OrderStepResult_None,
                -1,
                pTransType,
                -1,
                0,
                0,
                ordersource,
                currentTime,
                currentTime,
                "",
                0);
    }

    private void trackApiTiming(int apiId, long startTime, long endTime, int returncode) {
        try {
            if (TextUtils.isEmpty(mZPApptransidLog.apptransid)) {
                Timber.d("skip track api call app trans id");
                return;
            }
            ZPApptransidLogApiCall apiTrack = new ZPApptransidLogApiCall();
            apiTrack.apptransid = mZPApptransidLog.apptransid;
            apiTrack.apiid = apiId;
            apiTrack.time_begin = startTime;
            apiTrack.time_end = endTime;
            apiTrack.return_code = returncode;
            ZPAnalytics.trackApptransidApiCall(apiTrack);
            Timber.d("tracking call api timing api (apiid, returncode) - (%s , %s) time %s(ms)", apiId, apiTrack.return_code, ((endTime - startTime)));
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    private void trackApiTiming(int apiId, long startTime, long endTime, Throwable throwable) {
        try {
            int returnCode = -100;
            if (TransactionHelper.timeoutException(throwable)) {
                returnCode = -1010;
                Timber.w(throwable, "Exception apiId %s PC response null user id %s", apiId, mUserId);
            } else if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                returnCode = httpException.code();
                Timber.w(throwable, "Exception apiId %s http error %s user id %s", apiId, httpException.getMessage(), mUserId);
            }
            trackApiTiming(apiId, startTime, endTime, returnCode);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    private void trackApiTiming(int apiId, long startTime, long endTime, BaseResponse response) {
        int returnCode = response != null ? response.returncode : -100;
        trackApiTiming(apiId, startTime, endTime, returnCode);
    }

    public void trackUserCancel() {
        try {
            mZPApptransidLog.step = ZPPaymentSteps.OrderStep_OrderResult;
            mZPApptransidLog.step_result = ZPPaymentSteps.OrderStepResult_UserCancel;
            track();
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    public void track() {
        try {
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
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }
}
