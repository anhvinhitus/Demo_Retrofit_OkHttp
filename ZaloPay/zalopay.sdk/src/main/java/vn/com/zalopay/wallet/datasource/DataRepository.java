package vn.com.zalopay.wallet.datasource;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.SaveCardResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.implement.GetPlatformInfoImpl;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;
import vn.com.zalopay.wallet.datasource.request.BaseTask;
import vn.com.zalopay.wallet.datasource.task.TPaymentTask;
import vn.com.zalopay.wallet.eventmessage.NetworkEventMessage;
import vn.com.zalopay.wallet.eventmessage.PaymentEventBus;
import vn.com.zalopay.wallet.listener.IDataSourceListener;
import vn.com.zalopay.wallet.listener.IPaymentApiCallBack;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class DataRepository<T extends BaseResponse> extends SingletonBase {
    private static DataRepository _object;
    protected InjectionWrapper mInjectionWrapper;
    private IData mDataSource;
    private boolean mIsRequesting = false;
    private IDataSourceListener mDataSourceLitener;
    private Call mCallable;//keep the request to retry
    private Call mCallableGetStatus;//keep the request to retry get status
    private int retryCount = 1;
    private WeakReference<ITask> mCurrentTask = null;
    private WeakReference<IRequest> mCurrentRequest = null;
    private Subscription mSubscription;
    protected final Action0 completeAction = new Action0() {
        @Override
        public void call() {
            Log.d(this, "onCompleted");
            releaseLock();
        }
    };
    private BaseTask mTask;
    protected final Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            mTask.onRequestFail(throwable);
            releaseLock();
        }
    };
    protected final Action1<Response<T>> doOnNextAction = new Action1<Response<T>>() {
        @Override
        public void call(Response<T> response) {
            mTask.onDoTaskOnResponse(response.body());
            if (mCurrentRequest != null && mCurrentRequest.get() != null && PaymentPermission.allowUseTrackingTiming()) {
                Long timeRequest = response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis();
                ZPAnalytics.trackTiming(mCurrentRequest.get().getRequestEventId(), timeRequest);//send tracking timing to tracking source, for example : GA,vvv
                Log.d(this, "===ZPAnalytics.trackTiming===" + mCurrentRequest.get().getRequestEventId() + " timing(ms)=" + (timeRequest));
            }
        }
    };
    protected final Action1<Response<T>> nextAction = new Action1<Response<T>>() {
        @Override
        public void call(Response<T> response) {
            mTask.onRequestSuccess(response.body());
        }
    };

    public DataRepository() {
        super();
        mInjectionWrapper = new InjectionWrapper();
        SDKApplication.getApplicationComponent().inject(mInjectionWrapper);
        mDataSource = mInjectionWrapper.getRetrofit().create(IData.class);
        setRetryCountNumber();
    }

    public DataRepository(Object... params) {
        super();
        mInjectionWrapper = new InjectionWrapper();
        SDKApplication.getApplicationComponent().inject(mInjectionWrapper);
        mDataSource = mInjectionWrapper.getRetrofitDownloadResource().create(IData.class);
        setRetryCountNumber();
    }

    public static DataRepository newInstance() {
        return new DataRepository();
    }

    public static DataRepository shareInstance() {
        if (DataRepository._object == null) {
            DataRepository._object = new DataRepository();
        }
        return DataRepository._object;
    }

    /***
     * httpclient for download resouce with httpclient
     * @return
     */
    public static DataRepository getInstanceDownloadResource() {
        return new DataRepository(true);
    }

    public static void dispose() {
        SingletonLifeCircleManager.disposeDataRepository();
    }

    public DataRepository setTask(BaseTask mTask) {
        this.mTask = mTask;
        return this;
    }

    public void cancelRequest() {
        if (mCallable != null && !mCallable.isCanceled() && mCallable.isExecuted()) {
            mCallable.cancel();
            Log.d(this, "canceling request " + mCallable.toString());
        }
        if (mCallableGetStatus != null && !mCallableGetStatus.isCanceled() && mCallableGetStatus.isExecuted()) {
            mCallableGetStatus.cancel();
            Log.d(this, "canceling request get status " + mCallableGetStatus.toString());
        }
    }

    private void setRetryCountNumber() {
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
            NetworkEventMessage networkEventMessage = new NetworkEventMessage();
            networkEventMessage.origin = Constants.API_ORIGIN;
            PaymentEventBus.shared().post(networkEventMessage);
            return true;
        }
        return false;
    }

    private boolean retry(final retrofit2.Callback pCallback, Throwable t) {
        if (retryCount <= Constants.API_MAX_RETRY && needRetry(t)) {
            try {
                retryCount++;
                if (mCallable == null) {
                    Log.d(this, "retry request but mCallable = NULL, retryCount" + retryCount);
                    return false;
                }
                mCallable.clone().enqueue(pCallback);
                Log.d(this, mCallable.toString() + ",retryCount=" + (retryCount - 1));
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

    protected void onRequest(Call pCall, final IPaymentApiCallBack pCallback) {
        if (pCall != null) {
            pCall.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        //send tracking timing to tracking source, for example : GA,vvv
                        if (mCurrentTask != null && mCurrentTask.get() != null && PaymentPermission.allowUseTrackingTiming()) {
                            Long timeRequest = response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis();
                            ZPAnalytics.trackTiming(mCurrentTask.get().getTaskEventId(), timeRequest);
                            Log.d(this, "===ZPAnalytics.trackTiming===" + mCurrentTask.get().getTaskEventId() + " timing(ms)=" + (timeRequest));
                        }
                        if (pCallback != null) {
                            pCallback.onFinish(call, response);
                        }
                        //send log to server if http code is error
                        //sendErrorLogHttpErrorCode(response);
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
                        //sendErrorLogResponseNULL(t);
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
            mCallable = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, pParams);
            onRequest(mCallable, new IPaymentApiCallBack<T>() {
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

    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public synchronized void loadData(IRequest pRequest, HashMap<String, String> pParams) {
        if (haveRequestRunning()) {
            Log.d(this, mTask.toString() + " there're a task is running...");
            return;
        }
        inProgress();
        mCurrentRequest = new WeakReference<IRequest>(pRequest);
        try {
            mSubscription = pRequest.getRequest(mDataSource, pParams)
                    .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                    .doOnNext(doOnNextAction)
                    .compose(applySchedulers())
                    .subscribe(nextAction, errorAction, completeAction);
        } catch (Exception ex) {
            Log.e(this, ex);
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
            mCallable = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, params);
            onRequest(mCallable, new IPaymentApiCallBack<T>() {
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
            mCurrentTask = new WeakReference<ITask>(pTask);

            if (!pIsRetry) {
                mCallableGetStatus = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, params);
                Log.d(this, "the first request...create new callable");
            } else {

                if (mCallableGetStatus == null) {
                    mCallableGetStatus = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, params);
                    Log.d(this, "the retry request...create new callable");
                } else {
                    mCallableGetStatus = mCallableGetStatus.clone();
                    Log.d(this, "the retry request...reuse callable");
                }
            }

            onRequest(mCallableGetStatus, new IPaymentApiCallBack<T>() {
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
            mCallable = TPaymentTask.newInstance().setTask(task).doTask(mDataSource, params);
            onRequest(mCallable, new IPaymentApiCallBack<T>() {
                @Override
                public void onFinish(Call call, Response<T> response) {
                    onSuccessRequest(response.isSuccessful(), response.body());
                }

                @Override
                public void onFail(Callback pCall, Throwable t) {
                    if (!retry(pCall, t)) {
                        Log.d("getPlatformInfo.onFailure", t != null ? t.getMessage() : "error");

                        //save this request for retrying later
                        RequestKeeper.requestPlatformInfo = mCallable.clone();

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
            mCurrentTask = new WeakReference<ITask>(pTask);
            mCallable = TPaymentTask.newInstance().setTask(pTask).doTask(mDataSource, params);
            onRequest(mCallable, new IPaymentApiCallBack<SaveCardResponse>() {
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
        return mIsRequesting;
    }

    protected void releaseLock() {
        mIsRequesting = false;
        unSubscribe();
        dispose();
        Log.d(this, "released lock requesting...");
    }

    protected void unSubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    /***
     * mark as request is running
     */
    private void inProgress() {
        mIsRequesting = true;
        if (mDataSourceLitener != null) {
            mDataSourceLitener.onRequestAPIProgress();
        }

        if(mTask != null)
        {
            mTask.onRequestInProcess();
        }
    }

    /**
     * callback error request
     *
     * @param pError
     */
    private void onErrorRequest(String pError) {
        mIsRequesting = false;
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
        //update access token if have new
        GlobalData.checkForUpdateAccessTokenToApp(pResponse);

        if (mDataSourceLitener != null) {
            mDataSourceLitener.onRequestAPIComplete(pIsSuccess, null, pResponse);
        }
    }
}
