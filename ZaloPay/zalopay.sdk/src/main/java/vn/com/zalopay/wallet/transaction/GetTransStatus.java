package vn.com.zalopay.wallet.transaction;

import android.support.annotation.NonNull;

import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import vn.com.zalopay.wallet.api.AbstractRequest;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static vn.com.zalopay.wallet.constants.Constants.TRANS_STATUS_DELAY_RETRY;
import static vn.com.zalopay.wallet.constants.Constants.TRANS_STATUS_MAX_RETRY;

/**
 * Created by chucvv on 6/17/17.
 */

public class GetTransStatus extends AbstractRequest<StatusResponse> {
    private long mAppId;
    private UserInfo mUserInfo;
    private String mTransId;
    private int retryCount = 0;

    private Func1<StatusResponse, Boolean> shouldStop = pResponse -> shouldStop(pResponse);

    private boolean shouldStop(StatusResponse pResponse){
        if (pResponse == null) {
            return false;
        }
        if (retryCount >= TRANS_STATUS_MAX_RETRY) {
            return true;
        }
        retryCount++;
        return !pResponse.isprocessing;
    }

    public GetTransStatus(ITransService transService, long appId, UserInfo userInfo, String transId) {
        super(transService);
        this.mAppId = appId;
        this.mUserInfo = userInfo;
        this.mTransId = transId;
    }

    @Override
    public Map<String, String> buildParams() {
        Map<String, String> map = getMapTable();
        DataParameter.prepareGetStatusParams(String.valueOf(mAppId), mUserInfo, map, mTransId);
        return map;
    }

    public Observable<StatusResponse> getStatus(Map<String, String> params) {
        final long intervalRetry = GlobalData.isZalopayChannel(mAppId) ? TRANS_STATUS_DELAY_RETRY / 2 : TRANS_STATUS_DELAY_RETRY;
        return mTransService.getStatus(params)
                .repeatWhen(o -> o.flatMap(v -> Observable.timer(intervalRetry, MILLISECONDS)))
                .takeUntil(shouldStop);
    }

   /* public Observable<StatusResponse> createObservable(Map<String, String> params, StatusResponse response) {
        final long intervalRetry = GlobalData.isZalopayChannel(mAppId) ? TRANS_STATUS_DELAY_RETRY / 2 : TRANS_STATUS_DELAY_RETRY;
        return shouldStop(response)
                ? Observable.empty() : mTransService.getStatus(params)
                .delaySubscription(intervalRetry, MILLISECONDS)
                .concatMap(response1 -> createObservable(params, response1)
                        .startWith(response1)
                        .takeUntil(shouldStop)
                );
    }*/

    @Override
    public Observable<StatusResponse> getObserver() {
        //return createObservable(buildParams(), null);
        return getStatus(buildParams());
    }
}
