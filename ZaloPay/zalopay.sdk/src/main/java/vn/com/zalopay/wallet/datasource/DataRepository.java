package vn.com.zalopay.wallet.datasource;

import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.message.PaymentEventBus;
import vn.com.zalopay.wallet.message.SdkNetworkEventMessage;

public class DataRepository<T extends BaseResponse> extends SingletonBase {
    private static DataRepository _object;
    protected InjectionWrapper mInjectionWrapper;
    private IData mDataSource;
    private boolean mIsRequesting = false;//prevent duplicate request
    private Subscription mSubscription;
    private BaseTask mTask;

    protected final Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            releaseLock();
            mTask.onRequestFail(throwable);
            verifyException(throwable);
        }
    };
    protected final Action1<T> doOnNextAction = new Action1<T>() {
        @Override
        public void call(T response) {
            mTask.onDoTaskOnResponse(response);
        }
    };
    protected final Action1<T> nextAction = new Action1<T>() {
        @Override
        public void call(T response) {
            releaseLock();
            mTask.onRequestSuccess(response);
            GlobalData.checkForUpdateAccessTokenToApp(response); //update access token if have new
        }
    };

    public DataRepository() {
        super();
        mInjectionWrapper = new InjectionWrapper();
        SDKApplication.getApplicationComponent().inject(mInjectionWrapper);
        mDataSource = mInjectionWrapper.getRetrofit().create(IData.class);
    }

    public DataRepository(Object... params) {
        super();
        mInjectionWrapper = new InjectionWrapper();
        SDKApplication.getApplicationComponent().inject(mInjectionWrapper);
        mDataSource = mInjectionWrapper.getRetrofitDownloadResource().create(IData.class);
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
        releaseLock();
    }

    protected boolean verifyException(Throwable t) {
        if ((t instanceof SSLHandshakeException || t instanceof SSLPeerUnverifiedException)) {
            SdkNetworkEventMessage networkEventMessage = new SdkNetworkEventMessage();
            networkEventMessage.origin = Constants.API_ORIGIN;
            PaymentEventBus.shared().post(networkEventMessage);
            return true;
        }
        return false;
    }

<<<<<<< HEAD
    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected Observable<Response<ResponseBody>> createObservableDownloadFile(String pUrlFile) {
        return Observable.defer(() -> {
            try {
                return Observable.just(mDataSource.getFile(pUrlFile).execute());
            } catch (IOException e) {
                return Observable.error(e);
=======
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
>>>>>>> 9fd9a35... [SDK] Apply app info v1
            }
        });
    }

<<<<<<< HEAD
    public synchronized void downloadResource(String pUrl) {
=======
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
                            Log.d(this, "ZPAnalytics.trackTiming " + mCurrentTask.get().getTaskEventId() + " timing(ms)=" + (timeRequest));
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
            Log.e(this, "onRequest pCall=NULL");
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

    /***
     * push data to server
     *
     * @param pTask
     * @param params
     */
    public synchronized void pushData(final ITask pTask, HashMap<String, String> params) {
>>>>>>> 9fd9a35... [SDK] Apply app info v1
        if (haveRequestRunning()) {
            Log.d(this, mTask.toString() + " there're a task is running...");
            return;
        }
        inProgress();
        mSubscription = createObservableDownloadFile(pUrl)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(responseBodyResponse -> mTask.onDoTaskOnResponse(responseBodyResponse.body()))
                .compose(applySchedulers())
                .doOnError(errorAction)
                .subscribe();

    }

    public synchronized void loadData(IRequest pRequest, Map<String, String> pParams) {
        if (haveRequestRunning()) {
            Log.d(this, mTask.toString() + " there're a task is running...");
            return;
        }
        inProgress();
        getData(pRequest, pParams);
    }

    public synchronized void loadDataParallel(IRequest pRequest, Map<String, String> pParams) {
        getData(pRequest, pParams);
    }

    protected synchronized void getData(IRequest pRequest, Map<String, String> pParams) {
        mSubscription = pRequest.getRequest(mDataSource, pParams)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(doOnNextAction)
                .compose(applySchedulers())
                .subscribe(nextAction, errorAction);
    }

    public synchronized void postData(final IRequest pRequest, Map<String, String> pParams) {
        if (haveRequestRunning()) {
            Log.d(this, mTask.toString() + "there're a task is running...");
            return;
        }
        inProgress();
        mSubscription = pRequest.getRequest(mDataSource, pParams)
                .doOnNext(doOnNextAction)
                .compose(applySchedulers())
                .subscribe(nextAction, errorAction);
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
        Log.d(this, "released lock requesting...");
    }

    protected void unSubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            Log.d(this, "unsubscribe...");
        }
    }

    /***
     * mark as request is running
     */
    private void inProgress() {
        mIsRequesting = true;
        if (mTask != null) {
            mTask.onRequestInProcess();
        }
    }
}
