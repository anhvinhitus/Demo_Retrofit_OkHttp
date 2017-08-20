package vn.com.zalopay.wallet.transaction;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.api.AbstractRequest;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

/**
 * in case submit order return fail as networking - request timeout
 * then call this api to ask for server about submited order status
 * Created by chucvv on 6/16/17.
 */

public class StatusByAppTrans extends AbstractRequest<StatusResponse> {
    private long appId;
    private String userId;
    private String appTransId;
    private int retryCount = 1;

    public StatusByAppTrans(ITransService pTransService, long appId, String userId, String appTransId) {
        super(pTransService);
        this.mTransService = pTransService;
        this.appId = appId;
        this.userId = userId;
        this.appTransId = appTransId;
    }

    private boolean shouldStop(StatusResponse statusResponse) {
        Timber.d("start check trans status by app trans [retry time: %s]", retryCount);
        ZPAnalyticsTrackerWrapper.trackApiCall(ZPEvents.CONNECTOR_V001_TPE_GETSTATUSBYAPPTRANSIDFORCLIENT, startTime, statusResponse);
        boolean stop = shouldStopCheckStatus(statusResponse);
        running = !stop;
        return stop;
    }

    private boolean shouldStopCheckStatus(StatusResponse pResponse) {
        if (retryCount >= Constants.TRANS_STATUS_MAX_RETRY) {
            return true;
        }
        return pResponse != null
                && !PaymentStatusHelper.isTransactionNotSubmit(pResponse);
    }

    @Override
    protected void doOnError(Throwable throwable) {
        super.doOnError(throwable);
        ZPAnalyticsTrackerWrapper.trackApiError(ZPEvents.CONNECTOR_V001_TPE_GETSTATUSBYAPPTRANSIDFORCLIENT, startTime, throwable);
    }

    @Override
    public Map<String, String> buildParams() {
        Map<String, String> map = getMapTable();
        DataParameter.prepareGetStatusByAppStransParams(String.valueOf(appId), userId, appTransId, map);
        return map;
    }

    @Override
    public Observable<StatusResponse> getObserver() {
        return mTransService.getStatusByAppTransClient(buildParams())
                .doOnSubscribe(() -> {
                    startTime = System.currentTimeMillis();
                    retryCount++;
                    running = true;
                })
                .doOnError(this::doOnError)
                /*.map(statusResponse -> {
                    statusResponse.isprocessing = true;
                    statusResponse.returncode = -49;
                    return statusResponse;
                })*/
                .repeatWhen(observable -> observable.delay(Constants.TRANS_STATUS_DELAY_RETRY, TimeUnit.MILLISECONDS))
                .takeUntil(this::shouldStop)
                .filter(this::shouldStopCheckStatus);
    }
}
