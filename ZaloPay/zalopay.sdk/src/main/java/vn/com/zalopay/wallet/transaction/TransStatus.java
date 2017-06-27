package vn.com.zalopay.wallet.transaction;

import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.zalopay.wallet.api.AbstractRequest;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.helper.TransactionHelper;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static vn.com.zalopay.wallet.constants.Constants.TRANS_STATUS_DELAY_RETRY;
import static vn.com.zalopay.wallet.constants.Constants.TRANS_STATUS_MAX_RETRY;

/**
 * Created by chucvv on 6/17/17.
 */

public class TransStatus extends AbstractRequest<StatusResponse> {
    private long mAppId;
    private UserInfo mUserInfo;
    private String mTransId;
    private int retryCount = 0;
    private long intervalRetry = TRANS_STATUS_DELAY_RETRY;

    private Func1<StatusResponse, Boolean> shouldStop = statusResponse -> {
        boolean stop = shouldStop(statusResponse);
        running = !stop;
        return stop;
    };

    public TransStatus(ITransService transService, long appId, UserInfo userInfo, String transId) {
        super(transService);
        this.mAppId = appId;
        this.mUserInfo = userInfo;
        this.mTransId = transId;
        intervalRetry = GlobalData.isZalopayChannel(mAppId) ? TRANS_STATUS_DELAY_RETRY / 2 : TRANS_STATUS_DELAY_RETRY;
    }

    private boolean shouldStop(StatusResponse pResponse) {
        Timber.d("start check should stop check trans status");
        if (pResponse == null) {
            return false;
        }
        if (retryCount >= TRANS_STATUS_MAX_RETRY) {
            return true;
        }
        if (TransactionHelper.isSecurityFlow(pResponse)) {
            return true;
        }
        return !pResponse.isprocessing;
    }

    @Override
    public Map<String, String> buildParams() {
        Map<String, String> map = getMapTable();
        DataParameter.prepareGetStatusParams(String.valueOf(mAppId), mUserInfo, map, mTransId);
        return map;
    }

    public Observable<StatusResponse> getStatus(Map<String, String> params) {
        return mTransService.getStatus(params)
                .doOnSubscribe(() -> {
                    retryCount ++;
                    running = true;
                })
                /*.map(statusResponse -> {
                    statusResponse.isprocessing = true;
                    statusResponse.data = "{\"actiontype\":1,\"redirecturl\":\"ac2pl\"}";
                    return statusResponse;
                })*/
                .repeatWhen(observable -> observable.delay(intervalRetry, MILLISECONDS))
                .takeUntil(shouldStop)
                .filter(this::shouldStop);
    }

    @Override
    public Observable<StatusResponse> getObserver() {
        return getStatus(buildParams());
    }
}
