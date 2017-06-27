package vn.com.zalopay.wallet.transaction;

import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import vn.com.zalopay.wallet.api.AbstractRequest;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static vn.com.zalopay.wallet.constants.Constants.TRANS_STATUS_DELAY_RETRY;
import static vn.com.zalopay.wallet.constants.Constants.TRANS_STATUS_MAX_RETRY;

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
    private Func1<StatusResponse, Boolean> shouldStop = statusResponse -> {
        boolean stop = shouldStop(statusResponse);
        running = !stop;
        return stop;
    };

    public StatusByAppTrans(ITransService pTransService, long appId, String userId, String appTransId) {
        super(pTransService);
        this.mTransService = pTransService;
        this.appId = appId;
        this.userId = userId;
        this.appTransId = appTransId;
    }

    private boolean shouldStop(StatusResponse pResponse) {
        Log.d(this, "start check trans status by app trans");
        if (pResponse == null) {
            return false;
        }
        if (!PaymentStatusHelper.isTransactionNotSubmit(pResponse)) {
            return true;
        }
        if (retryCount >= TRANS_STATUS_MAX_RETRY) {
            return true;
        }
        return false;
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
                    retryCount++;
                    running = true;
                })
                /* .map(statusResponse -> {
                    statusResponse.isprocessing = true;
                    statusResponse.returncode = -49;
                    return statusResponse;
                })*/
                .repeatWhen(observable -> observable.delay(TRANS_STATUS_DELAY_RETRY, MILLISECONDS))
                .takeUntil(shouldStop)
                .filter(this::shouldStop);
    }
}
