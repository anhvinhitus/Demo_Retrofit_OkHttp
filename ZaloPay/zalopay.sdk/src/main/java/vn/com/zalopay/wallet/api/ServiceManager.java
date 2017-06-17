package vn.com.zalopay.wallet.api;

import java.util.Map;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import vn.com.zalopay.wallet.api.interfaces.IRequest;
import vn.com.zalopay.wallet.api.task.BaseTask;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkNetworkEventMessage;
import vn.com.zalopay.wallet.helper.SchedulerHelper;

public class ServiceManager<T extends BaseResponse> extends SingletonBase {
    private static ServiceManager _object;
    private ITransService mDataSource;
    private boolean mIsRequesting = false;//prevent duplicate request
    private Subscription mSubscription;
    private BaseTask mTask;

    private final Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            releaseLock();
            mTask.onRequestFail(throwable);
            verifyException(throwable);
        }
    };
    private final Action1<T> doOnNextAction = new Action1<T>() {
        @Override
        public void call(T response) {
            mTask.onDoTaskOnResponse(response);
        }
    };
    private final Action1<T> nextAction = new Action1<T>() {
        @Override
        public void call(T response) {
            releaseLock();
            mTask.onRequestSuccess(response);
            if (mTask.mUserInfo != null) {
                mTask.mUserInfo.checkForUpdateAccessTokenToApp(response);//update access token if have new
            }
        }
    };
    private Action0 inProgress = () -> {
        mIsRequesting = true;
        if (mTask != null) {
            mTask.onRequestInProcess();
        }
    };

    public ServiceManager() {
        super();
        mDataSource = SDKApplication.getApplicationComponent().transService();
    }

    public static ServiceManager newInstance() {
        return new ServiceManager();
    }

    public static ServiceManager shareInstance() {
        if (ServiceManager._object == null) {
            ServiceManager._object = new ServiceManager();
        }
        return ServiceManager._object;
    }

    public static void dispose() {
        SingletonLifeCircleManager.disposeDataRepository();
    }

    public ServiceManager setTask(BaseTask mTask) {
        this.mTask = mTask;
        return this;
    }

    public void cancelRequest() {
        releaseLock();
    }

    private boolean verifyException(Throwable t) {
        if ((t instanceof SSLHandshakeException || t instanceof SSLPeerUnverifiedException)) {
            SdkNetworkEventMessage networkEventMessage = new SdkNetworkEventMessage();
            networkEventMessage.origin = Constants.API_ORIGIN;
            SDKApplication.getApplicationComponent().eventBus().post(networkEventMessage);
            return true;
        }
        return false;
    }

    public synchronized void loadData(IRequest pRequest, Map<String, String> pParams) {
        if (haveRequestRunning()) {
            Log.d(this, mTask.toString() + " there're a task is running...");
            return;
        }
        getData(pRequest, pParams);
    }

    protected synchronized void getData(IRequest pRequest, Map<String, String> pParams) {
        mSubscription = pRequest.getRequest(mDataSource, pParams)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(doOnNextAction)
                .compose(SchedulerHelper.applySchedulers())
                .doOnSubscribe(inProgress)
                .subscribe(nextAction, errorAction);
    }

    public synchronized void postData(final IRequest pRequest, Map<String, String> pParams) {
        if (haveRequestRunning()) {
            Log.d(this, mTask.toString() + "there're a task is running...");
            return;
        }
        mSubscription = pRequest.getRequest(mDataSource, pParams)
                .doOnNext(doOnNextAction)
                .compose(SchedulerHelper.applySchedulers())
                .doOnSubscribe(inProgress)
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
            Log.d(this, "un subscribe...");
        }
    }
}
