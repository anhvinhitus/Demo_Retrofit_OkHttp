package vn.com.zalopay.wallet.datasource;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.message.NetworkEventMessage;
import vn.com.zalopay.wallet.message.PaymentEventBus;
import vn.com.zalopay.wallet.utils.Log;

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
            NetworkEventMessage networkEventMessage = new NetworkEventMessage();
            networkEventMessage.origin = Constants.API_ORIGIN;
            PaymentEventBus.shared().post(networkEventMessage);
            return true;
        }
        return false;
    }

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
            }
        });
    }

    public synchronized void downloadResource(String pUrl) {
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

    public synchronized void loadData(IRequest pRequest, HashMap<String, String> pParams) {
        if (haveRequestRunning()) {
            Log.d(this, mTask.toString() + " there're a task is running...");
            return;
        }
        inProgress();
        mSubscription = pRequest.getRequest(mDataSource, pParams)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(doOnNextAction)
                .compose(applySchedulers())
                .subscribe(nextAction, errorAction);
    }

    public synchronized void postData(final IRequest pRequest, HashMap<String, String> pParams) {
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
