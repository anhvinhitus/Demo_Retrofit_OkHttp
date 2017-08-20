package vn.com.zalopay.wallet.transaction;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.api.AbstractRequest;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

/**
 * Created by chucvv on 6/17/17.
 */

public class TransStatus extends AbstractRequest<StatusResponse> {
    private long mAppId;
    private UserInfo mUserInfo;
    private String mTransId;
    private int retryCount = 0;
    private long intervalRetry = Constants.TRANS_STATUS_DELAY_RETRY;

    public TransStatus(ITransService transService, long appId, UserInfo userInfo, String transId) {
        super(transService);
        this.mAppId = appId;
        this.mUserInfo = userInfo;
        this.mTransId = transId;
        intervalRetry = (mAppId == BuildConfig.channel_zalopay) ? Constants.TRANS_STATUS_DELAY_RETRY / 2 : Constants.TRANS_STATUS_DELAY_RETRY;
    }

    private boolean shouldStop(StatusResponse statusResponse) {
        Timber.d("start check should stop check trans status [retry time : %s]", retryCount);
        ZPAnalyticsTrackerWrapper.trackApiCall(ZPEvents.CONNECTOR_V001_TPE_GETTRANSSTATUS, startTime, statusResponse);
        boolean stop = shouldStopCheckStatus(statusResponse);
        running = !stop;
        return stop;
    }

    private boolean shouldStopCheckStatus(StatusResponse pResponse) {
        if (retryCount >= Constants.TRANS_STATUS_MAX_RETRY) {
            return true;
        }
        if (pResponse == null) {
            return false;
        }
        return TransactionHelper.isSecurityFlow(pResponse) || !pResponse.isprocessing;
    }

    @Override
    protected void doOnError(Throwable throwable) {
        super.doOnError(throwable);
        ZPAnalyticsTrackerWrapper.trackApiError(ZPEvents.CONNECTOR_V001_TPE_GETTRANSSTATUS, startTime, throwable);
    }

    @Override
    public Map<String, String> buildParams() {
        Map<String, String> map = getMapTable();
        DataParameter.prepareGetStatusParams(String.valueOf(mAppId), mUserInfo, map, mTransId);
        return map;
    }

    private StatusResponse getDefaultResponse() {
        StatusResponse response = new StatusResponse(-1, null);
        response.isprocessing = true;
        return response;
    }

    public Observable<StatusResponse> getStatus(Map<String, String> params) {
        return mTransService.getStatus(params)
                .doOnSubscribe(() -> {
                    retryCount++;
                    running = true;
                    startTime = System.currentTimeMillis();
                })
                .doOnError(this::doOnError)
                .onErrorReturn(throwable -> getDefaultResponse())
               /* .map(statusResponse -> {
                    statusResponse.isprocessing = true;
                    statusResponse.data = "{\"actiontype\":1,\"redirecturl\":\"ac2pl\"}";
                    return statusResponse;
                })*/
                .repeatWhen(observable -> observable.delay(intervalRetry, TimeUnit.MILLISECONDS))
                .takeUntil(this::shouldStop)
                .filter(this::shouldStopCheckStatus);
    }

    @Override
    public Observable<StatusResponse> getObserver() {
        return getStatus(buildParams());
    }
}
