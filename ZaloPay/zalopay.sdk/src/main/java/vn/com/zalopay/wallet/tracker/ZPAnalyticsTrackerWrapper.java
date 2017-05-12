package vn.com.zalopay.wallet.tracker;

import java.util.Date;

import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.utils.GsonUtils;


/**
 * Created by lytm on 04/05/2017.
 */

public class ZPAnalyticsTrackerWrapper extends SingletonBase {

    private static ZPAnalyticsTrackerWrapper mZPTrackerWrapper ;
    private int mStep = -1;
    private boolean isFinish = false;
    public ZPAnalyticsTrackerWrapper() {
        super();
    }

    public static ZPAnalyticsTrackerWrapper getInstance() {
        if (mZPTrackerWrapper == null) {
            mZPTrackerWrapper = new ZPAnalyticsTrackerWrapper();
        }
        return mZPTrackerWrapper;
    }


    public void ZPApptransIDLog(int step, int step_result, int pcmid, long transid, int server_result, int status) {
        ZPApptransidLog mZpApptransidLog = startZPApptransidLog();
        if (step == mStep) {
            Log.d(this,"LogTransID two times");
            return;
        }

        mZpApptransidLog.step = step;
        this.mStep = step;
        mZpApptransidLog.step_result = step_result;
        mZpApptransidLog.pcmid = pcmid;
        mZpApptransidLog.transid = transid;
        mZpApptransidLog.server_result = server_result;
        mZpApptransidLog.finish_time = new Date().getTime();
        mZpApptransidLog.status = status;
        if(status == 1)
        {
            isFinish = true;
        }
        ZPAnalytics.trackApptransidEvent(mZpApptransidLog);
        Log.d(this,"LogTransID==" +GsonUtils.toJsonString(mZpApptransidLog));
    }

    public void ZPApptransIDLog(int step, int step_result, int pcmid) {
        ZPApptransidLog mZpApptransidLog = startZPApptransidLog();
        if (step == mStep) {
            Log.d(this,"LogTransID two times");
            return;
        }

        mZpApptransidLog.step = step;
        this.mStep = step;
        mZpApptransidLog.step_result = step_result;
        mZpApptransidLog.pcmid = pcmid;
        ZPAnalytics.trackApptransidEvent(mZpApptransidLog);
        Log.d(this,"LogTransID==" +GsonUtils.toJsonString(mZpApptransidLog));
    }
    public void ZPApptransIDLog(int step, int step_result) {
        ZPApptransidLog mZpApptransidLog = startZPApptransidLog();
        if (step == mStep) {
            Log.d(this,"LogTransID two times");
            return;
        }

        mZpApptransidLog.step = step;
        this.mStep = step;
        mZpApptransidLog.step_result = step_result;
        ZPAnalytics.trackApptransidEvent(mZpApptransidLog);
        Log.d(this,"LogTransID==" +GsonUtils.toJsonString(mZpApptransidLog));
    }
    public void ZPApptransIDLog(int step, int step_result, int pcmid,int status) {
        ZPApptransidLog mZpApptransidLog = startZPApptransidLog();
        if (step == mStep || isFinish) {
            return;
        }
        mZpApptransidLog.step = step;
        this.mStep = step;
        mZpApptransidLog.step_result = step_result;
        mZpApptransidLog.pcmid = pcmid;
        mZpApptransidLog.status = status;
        mZpApptransidLog.finish_time = new Date().getTime();
        ZPAnalytics.trackApptransidEvent(mZpApptransidLog);
        Log.d(this,"LogTransID==" +GsonUtils.toJsonString(mZpApptransidLog));
    }

    public void ZPApptransIDLog(int step, int step_result, int pcmid,String bank_code ) {
        ZPApptransidLog mZpApptransidLog = startZPApptransidLog();
        if (step == mStep) {
            Log.d(this,"LogTransID two times");
            return;
        }

        mZpApptransidLog.step = step;
        this.mStep = step;
        mZpApptransidLog.step_result = step_result;
        mZpApptransidLog.pcmid = pcmid;
        mZpApptransidLog.bank_code = bank_code;
        ZPAnalytics.trackApptransidEvent(mZpApptransidLog);
        Log.d(this,"LogTransID==" +GsonUtils.toJsonString(mZpApptransidLog));
    }


    public ZPApptransidLog startZPApptransidLog() {
        ZPApptransidLog mZpApptransidLog = new ZPApptransidLog();
        if (GlobalData.getPaymentInfo() != null) {
            mZpApptransidLog.apptransid = GlobalData.getPaymentInfo().appTransID;
            mZpApptransidLog.appid = GlobalData.getPaymentInfo().appID;
        }
        mZpApptransidLog.transtype = GlobalData.getTransactionType();
        return mZpApptransidLog;
    }

    public static ZPApptransidLog newZPApptransidLog() {

        return new ZPApptransidLog();
    }

    public static void setTrakerLog(ZPApptransidLog mZPApptransidLog) {
        ZPAnalytics.trackApptransidEvent(mZPApptransidLog);
    }
}
