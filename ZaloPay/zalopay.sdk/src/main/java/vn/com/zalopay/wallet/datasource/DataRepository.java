package vn.com.zalopay.wallet.datasource;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.SaveCardResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.implement.GetPlatformInfoImpl;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;
import vn.com.zalopay.wallet.datasource.request.SDKReport;
import vn.com.zalopay.wallet.datasource.task.TPaymentTask;
import vn.com.zalopay.wallet.listener.IDataSourceListener;
import vn.com.zalopay.wallet.listener.IPaymentApiCallBack;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class DataRepository<T extends BaseResponse> extends SingletonBase {
    private static DataRepository _object;
    private IData mDataSource;
    private boolean mIsRequesting = false;
    private IDataSourceListener mDataSourceLitener;
    private Call mCallBack;
    private ArrayList<Call> mTempCallBack;
    private long startTimeRequest = 0;
    private long totalTimeRequest = 0;
    private int retryCount = 1;
    private WeakReference<ITask> mCurrentTask = null;

    public DataRepository(OkHttpClient pHttpClient) {
        super();
        createRetrofitService(pHttpClient);
        mTempCallBack = new ArrayList<>();
        resetCountRetry();
    }

    public static DataRepository shareInstance() {
        if (DataRepository._object == null) {
            DataRepository._object = new DataRepository(SDKApplication.getHttpClient());
        }
        DataRepository._object.resetCountRetry();
        return DataRepository._object;
    }

    public static DataRepository newInstance() {
        return new DataRepository(SDKApplication.getHttpClient());
    }

    /***
     * httpclient for download resouce with longer timeout
     *
     * @return
     */
    public static DataRepository getInstanceForDownloadResource() {
        return new DataRepository(SDKApplication.getHttpClientTimeoutLonger());
    }

    public static void dispose() {
        SingletonLifeCircleManager.disposeDataRepository();
    }

    private void createRetrofitService(OkHttpClient pHttpClient) {
        mDataSource = RetrofitSetup.createService(pHttpClient, IData.class);
    }

    public void cancelRequest() {
        if (mCallBack != null && !mCallBack.isCanceled() && mCallBack.isExecuted()) {
            mCallBack.cancel();
        }
        for (int i = 0; i < mTempCallBack.size(); i++) {
            Call call = mTempCallBack.get(i);

            if (call != null && !call.isCanceled() && call.isExecuted()) {
                call.cancel();
            }
        }
    }

    private void resetCountRetry() {
        if (retryCount >= Constants.API_MAX_RETRY) {
            retryCount = 1;
        }
    }

    protected boolean needRetry(Throwable t) {
        Log.d(this, GsonUtils.toJsonString(t));
        verifyException(t);
        return true;
    }


    protected boolean verifyException(Throwable t) {
        if ((t instanceof SSLHandshakeException || t instanceof SSLPeerUnverifiedException)) {
            Intent messageIntent = new Intent();
            messageIntent.setAction(Constants.FILTER_ACTION_NETWORKING_CHANGED);
            messageIntent.putExtra(Constants.NETWORKING_NOT_STABLE, true);
            LocalBroadcastManager.getInstance(GlobalData.getAppContext()).sendBroadcast(messageIntent);

            //if(GlobalData.getAppContext() != null)
            //NotificationUtils.shareInstance(GlobalData.getAppContext()).notify(ZPWUtils.getAppLable(GlobalData.getAppContext()),"Mạng dữ liệu cần đăng nhập!",true);
            return true;
        }
        return false;
    }

    private boolean retry(final retrofit2.Callback pCallback, Throwable t) {
        if (retryCount <= Constants.API_MAX_RETRY && needRetry(t)) {
            try {
                retryCount++;
                if (mCallBack == null) {
                    Log.d(this, "retry request but mCallBack = NULL, retryCount" + retryCount);
                    return false;
                }
                mCallBack.clone().enqueue(pCallback);
                Log.d(this, mCallBack.toString() + ",retryCount=" + (retryCount - 1));
                return true;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        return false;
    }

    public DataRepository setDataSourceListener(IDataSourceListener pListener) {
        this.mDataSourceLitener = pListener;

        return this;
    }

    protected void sendErrorLogHttpErrorCode(Response pResponse) {
        if (mCurrentTask != null && mCurrentTask.get() != null && mCurrentTask.get().getTaskEventId() == ZPEvents.API_V001_TPE_SDKERRORREPORT)
        {
            return;
        }
        if (pResponse != null && pResponse.code() >= 400) {
            String paymentError = getErrorMessageFormat(pResponse.code(), GsonUtils.toJsonString(pResponse));
            if (!TextUtils.isEmpty(paymentError)) {
                SDKReport.makeReportError(SDKReport.API_ERROR, paymentError);
            }
        } else if (pResponse == null) {
            String paymentError = getErrorMessageFormat(-1, "pResponse=NULL");
            if (!TextUtils.isEmpty(paymentError)) {
                SDKReport.makeReportError(SDKReport.API_ERROR, paymentError);
            }
        }
    }

    protected void sendErrorLogResponseNULL(Throwable pError) {
        String paymentError = getErrorMessageFormat(-1, GsonUtils.toJsonString(pError));
        if (!TextUtils.isEmpty(paymentError)) {
            SDKReport.makeReportError(paymentError);
        }
    }

    protected String getErrorMessageFormat(int pCode, String pErrorMessage) {
        try {
            String paymentError = GlobalData.getStringResource(RS.string.zpw_sdkreport_error_message);
            if (!TextUtils.isEmpty(paymentError)) {
                int apitrackingID = 0;
                if (mCurrentTask != null && mCurrentTask.get() != null) {
                    apitrackingID = mCurrentTask.get().getTaskEventId();
                }
                paymentError = String.format(paymentError, apitrackingID, pCode, pErrorMessage);

                return paymentError;
            }

        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return null;
    }

    protected void onRequest(Call pCall, final IPaymentApiCallBack pCallback) {
        if (pCall != null) {
            pCall.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        //send tracking timing to tracking source, for example : GA,vvv
                        if (mCurrentTask != null && mCurrentTask.get() != null && PaymentPermission.allowUseTrackingTiming()) {
                            totalTimeRequest = System.currentTimeMillis() - startTimeRequest;
                            ZPAnalytics.trackTiming(mCurrentTask.get().getTaskEventId(), totalTimeRequest);
                            Log.d(this, "===ZPAnalytics.trackTiming===" + mCurrentTask.get().getTaskEventId() + " timing(ms)=" + (totalTimeRequest));
                        }
                        if (pCallback != null) {
                            pCallback.onFinish(call, response);
                        }
                        //send log to server if http code is error
                        sendErrorLogHttpErrorCode(response);
                    } catch (Exception ex) {
                        Log.e(this, ex);
                        if (pCallback != null) {
                            pCallback.onFinish(call, response);
                        }
                    }
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    try {
                        if (pCallback != null) {
                            pCallback.onFail(this, t);
                        }
                        sendErrorLogResponseNULL(t);
                    } catch (Exception ex) {
                        Log.e(this, ex);
                        if (pCallback != null) {
                            pCallback.onFail(this, t);
                        }
                    }
                }
            });
        } else {
            Log.e(this, "==onRequest==pCall=NULL");
        }
    }

    /***
     * get data api
     *
     * @param pTask
     * @param pParams
     */
    public synchronized void getData(final ITask pTask, HashMap<String, String> pParams) {
        try {
            inProgress();
            mCurrentTask = new WeakReference<ITask>(pTask);
            mCallBack = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, pParams);
            onRequest(mCallBack, new IPaymentApiCallBack<T>() {
                @Override
                public void onFinish(Call call, Response<T> response) {
                    onSuccessRequest(response.isSuccessful(), response.body());
                }

                @Override
                public void onFail(Callback pCall, Throwable t) {
                    if (!retry(pCall, t)) {
                        Log.d(pTask.toString() + ".onFailure." + pTask.toString(), t != null ? t.getMessage() : "error");
                        onErrorRequest(t != null ? t.getMessage() : "");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(this, e);
            onErrorRequest(e.getMessage());
        }
    }

    /***
     * push data to server
     *
     * @param pTask
     * @param params
     */
    public synchronized void pushData(final ITask pTask, HashMap<String, String> params) {
        if (haveRequestRunning()) {
            Log.d(this, pTask.toString() + "there're a task is running...");
            return;
        }

        try {
            inProgress();
            mCurrentTask = new WeakReference<ITask>(pTask);
            mCallBack = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, params);
            onRequest(mCallBack, new IPaymentApiCallBack<T>() {
                @Override
                public void onFinish(Call call, Response<T> response) {
                    onSuccessRequest(response.isSuccessful(), response.body());
                }

                @Override
                public void onFail(Callback pCall, Throwable t) {
                    Log.d(pTask.toString() + ".pushData.onFailure", t != null ? t.getMessage() : "error");

                    verifyException(t);
                    onErrorRequest(t != null ? t.getMessage() : "");
                }
            });

        } catch (Exception e) {
            Log.e(this, e);
            onErrorRequest(e.getMessage());
        }
    }

    /***
     * use cache request to request again
     *
     * @param pTask
     * @param params
     * @param pIsRetry
     */
    public synchronized void getDataReuseRequest(final ITask pTask, HashMap<String, String> params, boolean pIsRetry) {
        if (haveRequestRunning()) {
            Log.d("getDataReuseRequest", pTask.toString() + "there're a task is running...");
            return;
        }

        try {
            inProgress();

            Call callBack = null;
            mCurrentTask = new WeakReference<ITask>(pTask);

            if (!pIsRetry) {
                mCallBack = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, params);
            } else {

                if (mCallBack == null) {
                    mCallBack = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, params);
                } else {
                    callBack = mCallBack.clone();
                }

                mTempCallBack.add(callBack);
            }

            Call callExe = (callBack != null) ? callBack : mCallBack;

            onRequest(callExe, new IPaymentApiCallBack<T>() {
                @Override
                public void onFinish(Call call, Response<T> response) {
                    onSuccessRequest(response.isSuccessful(), response.body());
                }

                @Override
                public void onFail(Callback pCall, Throwable t) {
                    Log.d(pTask.toString() + "getDataReuseRequest.onFailure", t != null ? t.getMessage() : "error");
                    verifyException(t);
                    onSuccessRequest(false, null);
                }
            });

        } catch (Exception e) {
            Log.e(this, e);

            onErrorRequest(e.getMessage());
        }
    }


    /***
     * download resource file
     *
     * @param pUrl
     * @return
     */
    public synchronized Response<ResponseBody> getBundleResource(String pUrl) {
        try {
            Call<ResponseBody> callBack = mDataSource.getFile(pUrl);

            return callBack.execute();

        } catch (Exception e) {
            Log.e(this, e);

            return null;
        }
    }

    public synchronized void getPlatformInfo(HashMap<String, String> params) {
        if (haveRequestRunning()) {
            Log.d(this, "there're a task get platforminfo is running...");
            return;
        }

        try {
            inProgress();
            ITask task = new GetPlatformInfoImpl();
            mCurrentTask = new WeakReference<ITask>(task);
            mCallBack = TPaymentTask.newInstance().setTask(task).doTask(mDataSource, params);
            onRequest(mCallBack, new IPaymentApiCallBack<T>() {
                @Override
                public void onFinish(Call call, Response<T> response) {
                    onSuccessRequest(response.isSuccessful(), response.body());
                }

                @Override
                public void onFail(Callback pCall, Throwable t) {
                    if (!retry(pCall, t)) {
                        Log.d("getPlatformInfo.onFailure", t != null ? t.getMessage() : "error");

                        //save this request for retrying later
                        RequestKeeper.requestPlatformInfo = mCallBack.clone();

                        onErrorRequest(t != null ? t.getMessage() : "");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(this, e);
            onErrorRequest(e.getMessage());
        }
    }

    public synchronized void retryPlatformInfo(Call pRequest) {
        if (haveRequestRunning()) {
            Log.d(this, "there're a task retry platforminfo is running...");
            return;
        }

        try {

            inProgress();
            onRequest(pRequest.clone(), new IPaymentApiCallBack<DPlatformInfo>() {
                @Override
                public void onFinish(Call call, Response<DPlatformInfo> response) {
                    onSuccessRequest(response.isSuccessful(), response.body());
                }

                @Override
                public void onFail(Callback pCall, Throwable t) {
                    Log.d("getPlatformInfo.onFailure", t != null ? t.getMessage() : "error");
                    onErrorRequest(t != null ? t.getMessage() : "");
                }
            });

        } catch (Exception e) {
            Log.e(this, e);
            onErrorRequest(e.getMessage());
        }
    }

    public synchronized void pushDataNoCheckDuplicate(final ITask pTask, HashMap<String, String> params) {

        try {
            startTimeRequest = System.currentTimeMillis();
            Log.d("===STARTING REQUEST at time=", String.valueOf(startTimeRequest));

            mCurrentTask = new WeakReference<ITask>(pTask);
            mCallBack = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, params);
            onRequest(mCallBack, new IPaymentApiCallBack<SaveCardResponse>() {
                @Override
                public void onFinish(Call call, Response<SaveCardResponse> response) {
                }

                @Override
                public void onFail(Callback pCall, Throwable t) {
                    verifyException(t);
                    Log.d(pTask.toString() + ".pushDataNoCheckDuplicate.onFailure====", t != null ? t.getMessage() : "error");
                }
            });

        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    /**
     * duplicate request
     *
     * @return
     */
    private boolean haveRequestRunning() {
        if (mIsRequesting) {
            Log.d(this, "There're a task request api have already run");
        }
        return mIsRequesting;
    }

    /***
     * mark as request is running
     */
    private void inProgress() {
        mIsRequesting = true;

        startTimeRequest = System.currentTimeMillis();
        Log.d("===STARTING REQUEST at time=", String.valueOf(startTimeRequest));

        if (mDataSourceLitener != null) {
            mDataSourceLitener.onRequestAPIProgress();
        }
    }

    /**
     * callback error request
     *
     * @param pError
     */
    private void onErrorRequest(String pError) {
        mIsRequesting = false;
        Log.d("===REQUEST ERROR total request time= ", String.format("%d ms", totalTimeRequest));
        if (mDataSourceLitener != null) {
            mDataSourceLitener.onRequestAPIComplete(false, pError, null);
        }
    }

    /***
     * callback success request
     *
     * @param pIsSuccess
     * @param pResponse
     */
    private void onSuccessRequest(boolean pIsSuccess, BaseResponse pResponse) {
        mIsRequesting = false;
        Log.d("=====REQUEST SUCESS total request time= ", String.format("%d MS", totalTimeRequest));
        //update access token if have new
        GlobalData.checkForUpdateAccessTokenToApp(pResponse);

        if (mDataSourceLitener != null) {
            mDataSourceLitener.onRequestAPIComplete(pIsSuccess, null, pResponse);
        }
    }
}